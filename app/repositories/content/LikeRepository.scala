package repositories.content

import java.io.Serializable


import models.contents.Article
import reactivemongo.api.commands.WriteResult

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.authentication._
import mongo.MongoDBProxy
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

// BSON implementation of the count command
import reactivemongo.api.commands.bson.BSONCountCommand.{Count, CountResult}
// BSON serialization-deserialization for the count arguments and result
import reactivemongo.api.commands.bson.BSONCountCommandImplicits._

/**
  * Created by corpus on 11/02/2016.
  */
trait LikeRepository {

}

object LikeRepository extends LikeRepository {
  val collectionLikes: BSONCollection = MongoDBProxy.db.collection("likes")

  def create(userId: BSONObjectID, articleId: BSONObjectID): Future[String] = {

    val query = BSONDocument("user_id" -> userId, "article_id" -> articleId)
    val command = Count(query)
    val result: Future[CountResult] = collectionLikes.runCommand(command)

    result.map { res =>
      val numberOfDocs: Int = res.value

      //checking if like exits
      if (numberOfDocs == 0) {
        def future: Future[WriteResult] = collectionLikes.insert(query)
        future.onComplete {
          case Failure(e) => throw e
          case Success(writeResult) =>
        }
        "the user successfully liked the article"
      }
      else "Parbleu, you have already liked the article!"

      // do something with this number
    }

  }

  def getNumberLikes(article: Article): Future[Int] = {
    val futureArticleOptionId: Future[Option[BSONDocument]] = ArticleRepository.getId(article)

    val futureNumberArticles: Future[Int] = futureArticleOptionId.flatMap{
      case None => Future{0}

      case Some(articleId) => {

        val query = BSONDocument("article_id" -> articleId.getAs[BSONObjectID]("article_id"))
        val command = Count(query)
        val result: Future[CountResult] = collectionLikes.runCommand(command)

        result.map { res =>
          val numberArticles = res.value
          numberArticles
        }
      }
    }
    return futureNumberArticles
  }
}

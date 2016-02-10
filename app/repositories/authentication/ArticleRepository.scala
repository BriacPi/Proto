package repositories.authentication

import java.io.Serializable

import models.authentication._
import mongo.MongoDBProxy
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by corpus on 05/02/2016.
  */

trait ArticleRepository {

}


object ArticleRepository extends ArticleRepository {
  val collectionArticle: BSONCollection = MongoDBProxy.db.collection("articles")
  val collectionUser: BSONCollection = MongoDBProxy.db.collection("users")

  implicit object articleReader extends BSONDocumentReader[Article] {
    def read(doc: BSONDocument): Article = {
      //GetAs returns automatically an option of the type it wraps around
      val title = doc.getAs[String]("title").get
      val content = doc.getAs[String]("content").get
      //
      //      val q = BSONDocument("_id" -> doc.getAs[BSONObjectID]("userId").get)
      //      val userDoc = collection.find(q).cursor[BSONDocument]().collect[List]().value.get.get.head
      //      val author = UserRepository.userReader.read(userDoc)

      val creationDate = doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt.value)).get
      val lastUpdate = doc.getAs[BSONDateTime]("lastUpdate").map(dt => new DateTime(dt.value)).get
      val nbLikes = doc.getAs[Int]("nbLikes").get
      val nbComments = doc.getAs[Int]("nbComments").get

      Article(title, content, creationDate, lastUpdate, nbLikes, nbComments)
      //how to not to build them

    }
  }

  implicit object articleWriter extends BSONDocumentWriter[Article] {
    def write(a: Article): BSONDocument = {
      def doc: BSONDocument = BSONDocument()
      doc.++({
        "title" -> a.title
      })
      doc.++({
        "content" -> a.content
      })
      doc.++({
        "nbLikes" -> a.nbLikes
      })
      doc.++({
        "nbComments" -> a.nbComments
      })
      doc.++({
        "creationDate" -> BSONDateTime(a.creationDate.getMillis)
      })
      doc.++({
        "lastUpdate" -> BSONDateTime(a.lastUpdate.getMillis)
      })
      return doc
    }
  }

  //    def getAuthor(): User = {
  //
  //    }
  def getAllArticles(author: User): Future[List[Article]] = {

    val query = BSONDocument("email" -> author.email)
    val futureOption: Future[Option[BSONDocument]] = collectionUser.find(query).cursor[BSONDocument]().headOption

    val futureArticles: Future[List[Article]] = futureOption.flatMap {
      case Some(userDB) => {
        val queryId = BSONDocument("author_id" -> userDB.getAs[BSONObjectID]("_id"))
        val futureList: Future[List[BSONDocument]] = collectionArticle.find(queryId).cursor[BSONDocument]().collect[List]()
        val futureArticles: Future[List[Article]] = futureList.map(list =>
          list.map { doc =>
            articleReader.read(doc)
          }
        )
        futureArticles
      }
      case _ => Future {
        List()
      }
    }
    return futureArticles
  }

  def getByTitle(title: String): Future[List[Article]] = {
    val query = BSONDocument("title" -> title)
    val futureList: Future[List[BSONDocument]] = collectionUser.find(query).cursor[BSONDocument]().collect[List]()

    futureList.map { list =>
      list.map { doc =>
        articleReader.read(doc)
      }

    }
  }

  def createArticle(user: User, article: Article): Unit = {

  }

}









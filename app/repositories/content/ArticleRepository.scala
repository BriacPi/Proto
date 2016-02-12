package repositories.content

import java.io.Serializable
import models.contents.Article
import repositories.authentication._
import scala.util.{Failure, Success}
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

  implicit object articleReader extends BSONDocumentReader[Article] {
    def read(doc: BSONDocument): Article = {
      //GetAs returns automatically an option of the type it wraps around
      val title = doc.getAs[String]("title").get
      val content = doc.getAs[String]("content").get
      //
      //      val q = BSONDocument("_id" -> doc.getAs[BSONObjectID]("userId").get)
      //      val userDoc = collection.find(q).cursor[BSONDocument]().collect[List]().value.get.get.head
      //      val author = UserRepository.userReader.read(userDoc)

      val creationDate = doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt.value)).getOrElse(DateTime.now())
      val lastUpdate = doc.getAs[BSONDateTime]("lastUpdate").map(dt => new DateTime(dt.value)).getOrElse(DateTime.now())
      val nbLikes = doc.getAs[Int]("nbLikes").getOrElse(0)
      val nbComments = doc.getAs[Int]("nbComments").getOrElse(0)

      Article(title, content, creationDate, lastUpdate, nbLikes, nbComments)
      //how to not to build them

    }
  }

  implicit object articleWriter extends BSONDocumentWriter[Article] {
    def write(a: Article): BSONDocument = {
      def doc: BSONDocument = BSONDocument(
        "title" -> a.title,
        "content" -> a.content,
        "nbLikes" -> a.nbLikes,
        "nbComments" -> a.nbComments,
        "creationDate" -> BSONDateTime(a.creationDate.getMillis),
        "lastUpdate" -> BSONDateTime(a.lastUpdate.getMillis)
      )
      return doc
    }
  }


  def create(user: User, article: Article) = {
    val userOptionId: Future[Option[BSONDocument]] = UserRepository.getId(user)
    userOptionId.map {
      case None => Unit
      case Some(userId) => {
        val insertQuery: BSONDocument = ArticleRepository.articleWriter.write(article).add(BSONDocument(
          "author_id" -> userId.getAs[BSONObjectID]("_id"),
          "creationDate" -> BSONDateTime(DateTime.now().getMillis)
        ))
        val future = collectionArticle.insert(insertQuery)
        future.onComplete {
          case Failure(e) => throw e
          case Success(writeResult) =>
            println(s"successfully inserted article with result: $writeResult")
        }
      }
    }

  }


  def getByUser(author: User): Future[List[Article]] = {

    val query = BSONDocument("email" -> author.email)
    val futureOption: Future[Option[BSONDocument]] = UserRepository.getId(author)

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

  def getAllArticles(): Future[List[Article]] = {
    val query = BSONDocument()
    val futureList: Future[List[BSONDocument]] = collectionArticle.find(query).cursor[BSONDocument]().collect[List]()
    val futureArticles: Future[List[Article]] = futureList.map {
      list => {
        list.map {
          doc => articleReader.read(doc)
        }
      }
    }
    return futureArticles

  }

  def getByTitle(title: String): Future[List[Article]] = {
    val query = BSONDocument("title" -> title)
    val futureList: Future[List[BSONDocument]] = collectionArticle.find(query).cursor[BSONDocument]().collect[List]()

    futureList.map { list =>
      list.map { doc =>
        articleReader.read(doc)
      }

    }
  }

  def getId(article: Article): Future[Option[BSONDocument]] = {
    val title = article.title
    val creationDate = BSONDateTime(article.creationDate.getMillis)
    val query = BSONDocument(
      "title" -> title,
      "creationDate" -> creationDate
    )
    val futureOption: Future[Option[BSONDocument]] = collectionArticle.find(query).cursor[BSONDocument]().headOption

    val futureId: Future[Option[BSONDocument]] = futureOption.map {
      case None => None
      case Some(doc) => Some(BSONDocument("article_id" -> doc.getAs[BSONObjectID]("_id")))
    }

    return futureId
  }

  def getId(title: String, date: DateTime): Future[Option[BSONDocument]] = {
    val creationDate = BSONDateTime(date.getMillis)
    val query = BSONDocument(
      "title" -> title,
      "creationDate" -> creationDate
    )
    val futureOption: Future[Option[BSONDocument]] = collectionArticle.find(query).cursor[BSONDocument]().headOption

    val futureId: Future[Option[BSONDocument]] = futureOption.map {
      case None => None
      case Some(doc) => Some(BSONDocument("article_id" -> doc.getAs[BSONObjectID]("_id")))
    }

    return futureId
  }

  def getOneIdByTitle(title: String): Future[Option[BSONDocument]]  = {
    val query = BSONDocument("title" -> title)
    val futureOption: Future[Option[BSONDocument]] = collectionArticle.find(query).cursor[BSONDocument]().headOption

    val futureId: Future[Option[BSONDocument]] = futureOption.map {
      case None => None
      case Some(doc) => Some(BSONDocument("article_id" -> doc.getAs[BSONObjectID]("_id")))
    }

    return futureId
  }

}









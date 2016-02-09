package repositories.authentication

import models.authentication._
import mongo.MongoDBProxy
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

/**
  * Created by corpus on 05/02/2016.
  */

trait ArticleRepository {

}


object ArticleRepository extends ArticleRepository {
  val collection: BSONCollection = MongoDBProxy.db.collection("articles")

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
    def getByAuthor(name: String): List[Article] = {
      //IntelliJi doesn't solve BSONCollection type, has to state it

      val query = BSONDocument("author" -> name)
      var result: List[Article] = List()
      val futureList = collection.find(query).cursor[BSONDocument]().collect[List]()
      //collect returns a future type that needs to be unwrapped
      futureList.map { list =>
        list.foreach { doc =>
          println(BSONDocument.pretty(doc))
          result :+= articleReader.read(doc)
        }
        println("This is really worrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrking!!!!!!!!!!")
      }
      return result
    }

  }








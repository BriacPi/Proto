package model


import mongo._
import org.joda.time.DateTime
import play.api.data.Forms.{text, longNumber, mapping, nonEmptyText, optional}
import reactivemongo.bson._

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by corpus on 01/02/2016.
  */
case class Article(

                    title: String,
                    content: String,
                    author: User,
                    creationDate: Option[DateTime],
                    lastUpdate: Option[DateTime],
                    likes: Int,
                    listUsersLikes: List[User],
                    nbComments: Int,
                    listComments: List[Comment],
                    tags: List[Tag]

                  )


object Article {

  //  implicit object articleReader extends BSONDocumentReader[Article] {
  //    def read(doc: BSONDocument): Article = {
  //      Article(
  //        doc.getAs[String]("title").get,
  //        doc.getAs[String]("content").get,
  //        doc.getAs[User]("author").get,
  //        //GetAs returns automatically an option of the type it wraps around
  //        doc.getAs[DateTime]("creationDate"),
  //        doc.getAs[DateTime]("lastUpdate"),
  //        doc.getAs[Int]("Likes").get,
  //        doc.getAs[List[User]]("listUsersLikes").toList.flatten,
  //        doc.getAs[Int]("Comments").get,
  //        doc.getAs[List[Comment]]("listComments").toList.flatten,
  //        doc.getAs[List[Tag]]("Tags").toList.flatten)
  //    }
  //  }

  def findByAuthor(name: String): Unit = {
    //IntelliJi doesn't solve BSONCollection type
    val collection: BSONCollection = MongoDBProxy.db.collection("articles")
    val query = BSONDocument("author" -> name)

    val futureList = collection.find(query).cursor[BSONDocument]().collect[List]()
    futureList.map { list =>
      list.foreach { doc =>
        println(BSONDocument.pretty(doc))
      }

    }
  }
}









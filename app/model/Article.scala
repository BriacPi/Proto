package model


import mongo._
import org.joda.time.DateTime
import play.api.data.Forms.{text, longNumber, mapping, nonEmptyText, optional}
import reactivemongo.bson._

//import reactivemongo.bson.BSONDocument

import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by corpus on 01/02/2016.
  */

// The implicit conversion has to be done ia long for dateTime and BSONDateTime
case class Article(
                    id: Option[BSONObjectID],
                    title: String,
                    content: String,
                    author_id: Option[BSONObjectID],
                    creationDate: DateTime,
                    lastUpdate: DateTime,
                    likes: Int,
                    listUsersLikes_id: Option[List[BSONObjectID]],
                    nbComments: Int,
                    listComments_id: Option[List[BSONObjectID]],
                    tags_id: Option[List[BSONObjectID]]

                  )


object Article {

  implicit object articleReader extends BSONDocumentReader[Article] {
    def read(doc: BSONDocument): Article = {
      Article(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("title").get,
        doc.getAs[String]("content").get,
        doc.getAs[BSONObjectID]("author_id"),
        //GetAs returns automatically an option of the type it wraps around
        doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt.value)).get,
        doc.getAs[BSONDateTime]("lastUpdate").map(dt => new DateTime(dt.value)).get,
        doc.getAs[Int]("Likes").get,
        doc.getAs[List[BSONObjectID]]("listUsersLikes_id"),
        doc.getAs[Int]("Comments").get,
        doc.getAs[List[BSONObjectID]]("listComments_id"),
        doc.getAs[List[BSONObjectID]]("Tags_id"))
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
        "likes" -> a.likes
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

      if (a.author_id.isDefined) doc.++({
        "author_id" -> a.author_id.get
      })
      if (a.listUsersLikes_id.isDefined) doc.++({
        "listUsersLikes_id" -> a.listUsersLikes_id.get
      })
      if (a.listComments_id.isDefined) doc.++({
        "listComments_id" -> a.listComments_id.get
      })
      if (a.tags_id.isDefined) doc.++({
        "tags_id" -> a.tags_id.get
      })
      if (a.id.isDefined) doc.++({
        "_id" -> a.id.get
      })

      return doc
    }
  }

  def findByAuthor(name: String): List[Article] = {
    //IntelliJi doesn't solve BSONCollection type, has to state it
    val collection: BSONCollection = MongoDBProxy.db.collection("articles")
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








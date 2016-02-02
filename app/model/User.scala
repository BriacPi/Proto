package model


import mongo.MongoDBProxy

import org.joda.time.DateTime

import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.bson.BSONCollection


import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by corpus on 31/01/2016.
  */

case class User(
                 email: String,
                 name: String,
                 age: Long,
                 nbArticles: Int,
                 listArticles: List[Article],
                 nbComments: Int,
                 listComments: List[Comment],
                 nbLikesGiven: Int,
                 likedArticles: List[Article],
                 registrationDate: Option[DateTime],


                 private var password: String

               )

object User {
  import play.api.libs.json._
  val userCollection : BSONCollection = MongoDBProxy.db.collection("users")

  def addUser(document: BSONDocument) : Unit = {
  }

}

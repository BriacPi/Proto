package model

import org.joda.time.DateTime
import play.api.Play.current
import com.mongodb.MongoClient
import com.mongodb.MongoException
import com.mongodb.WriteConcern
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCursor
import com.mongodb.ServerAddress;
import mongo.MongoDBProxy

import play.api.libs.json.Json
import org.bson.Document
/**
  * Created by corpus on 31/01/2016.
  */

case class User(
                 name: String,
                 age: Long,
                 nbArticles: Int,
//                 listArticles: List[Article],
                 nbComments: Int,
//                 listComments: List[Comment],
                 nbLikesGiven: Int,
//                 likedArticles: List[Article],
                 dateRegisteration: DateTime

               )
object User {
  def getAll():Document = {
    MongoDBProxy.getCollection("users").find().first()
  }
  def getString():String = {

    //JSON String, ce n'est PAS un objet JSON.
  MongoDBProxy.getCollection("users").find().first().toJson()
  }
}
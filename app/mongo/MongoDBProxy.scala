package mongo

/**
  * Created by corpus on 31/01/2016.
  */

import com.mongodb.MongoClient
import com.mongodb.MongoException
import com.mongodb.WriteConcern
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCursor
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import org.bson.Document
;

object MongoDBProxy {

  //return Type: IMongoDatabase
  private val db = new MongoClient( "localhost" , 27017 ).getDatabase("test")

  def getCollection(name : String) : MongoCollection[Document] = {
    db.getCollection(name)
  }
}

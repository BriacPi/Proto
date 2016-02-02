
package mongo

/**
  * Created by corpus on 31/01/2016.
  */

import reactivemongo.api._

//import reactivemongo.bson.BSONCollection is deprecated
import reactivemongo.api.collections.bson.BSONCollection
import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global


object MongoDBProxy  {
  // gets an instance of the driver
  // (creates an actor system)
  private val driver = new MongoDriver
  private val connection = driver.connection(List("localhost:27017"))
  val db = connection("test")

  val collection: BSONCollection = db.collection("articles")


}

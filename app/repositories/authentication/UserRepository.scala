package repositories.authentication

import scala.util.{Failure, Success}
import models.authentication.{EditUser, TemporaryUser, Article, User}
import mongo.MongoDBProxy
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by corpus on 05/02/2016.
  */
trait UserRepository {

}

object UserRepository extends UserRepository {
  val collectionUser: BSONCollection = MongoDBProxy.db.collection("users")

  implicit object userReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      User(doc.getAs[String]("email").get,
        doc.getAs[String]("firstName").get,
        doc.getAs[String]("lastName").get,
        doc.getAs[String]("password").get,
        doc.getAs[BSONDateTime]("dateRegistration").map(dt => new DateTime(dt.value)).get
      )
    }
  }

  implicit object userWriter extends BSONDocumentWriter[TemporaryUser] {
    def write(user: TemporaryUser): BSONDocument = {
      def doc: BSONDocument = BSONDocument()
      doc.++("email" -> user.email)
      doc.++("firstName" -> user.firstName)
      doc.++("lastName" -> user.lastName)
      doc.++("password" -> user.password)
    }

    def write(user: EditUser): BSONDocument = {
      def doc: BSONDocument = BSONDocument()
      doc.++("email" -> user.email)
      doc.++("firstName" -> user.firstName)
      doc.++("lastName" -> user.lastName)
      doc.++("password" -> user.password)
    }

    def write(user: User): BSONDocument = {
      def doc: BSONDocument = BSONDocument()
      doc.++("email" -> user.email)
      doc.++("firstName" -> user.firstName)
      doc.++("lastName" -> user.lastName)
      doc.++("dateRegistration" -> BSONDateTime(user.dateRegistration.getMillis))
      doc.++("password" -> user.password)
    }
  }



  def create(user: TemporaryUser): Unit = {
    val doc = userWriter.write(user)
    val future: Future[WriteResult] = collectionUser.insert(doc)
    future.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }
  }

  def editUser(user: EditUser): Unit = {

    val selector = BSONDocument("email" -> user.email)
    val modifier = BSONDocument("$set" -> userWriter.write(user))
    val future = collectionUser.update(selector, modifier)
    future.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }
  }

  def editPassword(user: User): Unit = {

    val selector = BSONDocument("email" -> user.email)
    val modifier = BSONDocument("$set" -> BSONDocument("password" -> user.password))
    val future = collectionUser.update(selector, modifier)
    future.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) =>
        println(s"successfully updated document with result: $writeResult")
    }
  }

  def delete(email: String): Unit = {

    val selector = BSONDocument("email" -> email)
    val future = collectionUser.remove(selector)
    future.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) => println("successfully removed document")
    }
  }

  def delete(user: User): Unit = UserRepository.delete(user.email)


  def getByEmail(email: String): Future[Option[User]] = {
    val query = BSONDocument("email" -> email)
    val futureOption: Future[Option[BSONDocument]] = collectionUser.find(query).cursor[BSONDocument]().headOption

    val futureUser: Future[Option[User]] = futureOption.map {
      case Some(doc) => Some(userReader.read(doc))
      case _ => None
    }
    return futureUser
  }
  def getByName(name: String): Option[List[User]] = {
    //IntelliJi doesn't solve BSONCollection type, has to state it

    val query = BSONDocument("$or" -> BSONDocument("firstName" -> name, "lastName" -> name))

    //value gives option of try from future list, 2 gets fot option and try then head
    val listBSON = collectionUser.find(query).cursor[BSONDocument]().collect[List]().value.get.get
    //collect returns a future type that needs to be unwrapped
    if (listBSON.isEmpty) return None
    else Some((listBSON.map(doc => UserRepository.userReader.read(doc))))

  }


}

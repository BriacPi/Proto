package repositories.authentication

import models.authentication.{EditUser, TemporaryUser, Article, User}
import mongo.MongoDBProxy
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global

//
//case class User(
//                 email: String,
//                 firstName: String,
//                 lastName: String,
//                 dateBirth: DateTime,
//                 age: Long,
//                 nbArticles: Int,
//                 nbComments: Int,
//                 nbLikesGiven: Int,
//                 dateRegistration: DateTime,
//                 password: String
//
//               )

/**
  * Created by corpus on 05/02/2016.
  */
trait UserRepository {

}

object UserRepository extends UserRepository {
  val collection: BSONCollection = MongoDBProxy.db.collection("users")

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

  def getByName(name: String): Option[List[User]] = {
    //IntelliJi doesn't solve BSONCollection type, has to state it

    val query = BSONDocument("$or" -> BSONDocument("firstName" -> name, "lastName" -> name))

    //value gives option of try from future list, 2 gets fot option and try then head
    val listBSON = collection.find(query).cursor[BSONDocument]().collect[List]().value.get.get
    //collect returns a future type that needs to be unwrapped
    if (listBSON.isEmpty) return None
    else Some((listBSON.map(doc => UserRepository.userReader.read(doc))))
  }

  def getByEmail(email: String): Option[User] = {
    val query = BSONDocument("email" -> email)
    //value gives option of try from future list, 2 gets fot option and try then head
    val listBSON = collection.find(query).cursor[BSONDocument]().collect[List]().value.get.get
    //collect returns a future type that needs to be unwrapped
    if (listBSON.isEmpty) return None
    else Some(UserRepository.userReader.read(listBSON.head))
  }

  def create(user: TemporaryUser): Unit = {
//    val email = user.email
//    val firstName = user.firstName
//    val lastName = user.lastName
//
//    if (!getByEmail(email).isEmpty) {
//      "Error: this email has already been used."
//    }
    val doc = userWriter.write(user)
    collection.insert(doc)

  }

  def editUser(user: EditUser): Unit = {

    val selector = BSONDocument("email" -> user.email)
    val modifier = BSONDocument("$set" -> userWriter.write(user))
    collection.update(selector, modifier)
  }

  def editPassword(user: User): Unit = {

    val selector = BSONDocument("email" -> user.email)
    val modifier = BSONDocument("$set" -> BSONDocument("password" -> user.password))
    collection.update(selector, modifier)
  }

  def delet(email: String): Unit = {
    val selector = BSONDocument("email" -> email)

    collection.remove(selector)

//    futureRemove.onComplete {
//      case Failure(e) => throw e
//      case Success(lasterror) => {
//        println("successfully removed document")
//      }
  }

  def findByEmail (email: String): Option[User] = {
    val query = BSONDocument("email" -> email)
    val listUser = collection.find(query).cursor[BSONDocument]().collect[List]().value.get.get
    listUser match {
      case  x::r => Some(userReader.read(x))
      case _ => None
    }
  }


}

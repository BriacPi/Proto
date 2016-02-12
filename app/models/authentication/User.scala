package models.authentication

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by corpus on 31/01/2016.
  */

case class User(
                 email: String,
                 firstName: String,
                 lastName: String,
                 password: String,
                 dateRegistration: DateTime

               )

case class TemporaryUser(
                          email: String,
                          firstName: String,
                          lastName: String,
                          password: String
                        )


case class EditPassword(
                         oldPassword: String,
                         newPassword: String

                       )

case class EditUser(
                     email: String,
                     firstName: String,
                     lastName: String,
                     password: String

                   )

object User {

  implicit val userReader: Reads[User] = (
    (JsPath \ "email").read[String] and
      (JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "password").read[String] and
      (JsPath \ "dateRegistration").read[DateTime]
    ) (User.apply _)

  implicit val userWriter = new Writes[User] {
    def writes(user: User): JsObject = Json.obj(
      "email" -> user.email,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "password" -> user.password,
      "dateRegistration" -> user.dateRegistration
    )

  }
}
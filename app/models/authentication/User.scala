package models.authentication

import org.joda.time.DateTime

/**
  * Created by corpus on 31/01/2016.
  */

case class User(
                 email: String,
                 firstName: String,
                 lastName: String,
                 dateRegistration: DateTime,
                 password: String

               )

case class TemporaryUser(
                 email: String,
                 firstName: String,
                 lastName: String,
                 dateRegistration: DateTime,
                 password: String

               )

case class EditUser(
                 firstName: String,
                 lastName: String,
                 dateRegistration: DateTime,
                 password: String

               )

case class EditPassword(
                 oldPassword: String,
                 newPassword: String

               )

package models.authentication

import org.joda.time.DateTime

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
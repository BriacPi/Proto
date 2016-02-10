package controllers


import javax.inject.Inject

import scala.concurrent.Future
import components.mvc.AuthController
import components.user.{PasswordAuthentication, SessionManager}


import models.authentication._
import org.apache.http.protocol.ExecutionContext


import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories.authentication.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.functional.syntax._

//import from reactive mongo
import reactivemongo.api.gridfs.{// ReactiveMongo GridFS
DefaultFileToSave, FileToSave, GridFS, ReadFile
}

import play.modules.reactivemongo.{// ReactiveMongo Play2 plugin
MongoController,
ReactiveMongoApi,
ReactiveMongoComponents
}
import reactivemongo.api.Cursor
import reactivemongo.play.json._
import play.modules.reactivemongo.json.collection._


//(ws: WSClient

class UserController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends AuthController with ReactiveMongoComponents{
  val addUserForm: Form[TemporaryUser] = Form(
    mapping(
      "email" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "password" -> nonEmptyText
    )(TemporaryUser.apply)(TemporaryUser.unapply)
  )
  val editUserForm: Form[EditUser] = Form(
    mapping(
      "email" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "oldPassword" -> nonEmptyText
    )(EditUser.apply)(EditUser.unapply)
  )
  val editPasswordForm: Form[EditPassword] = Form(
    mapping(
      "oldPassword" -> nonEmptyText,
      "newPassword" -> nonEmptyText
    )(EditPassword.apply)(EditPassword.unapply)
  )

  val form: Form[LoginValues] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginValues.apply)(LoginValues.unapply)
  )

  //Need to be authenticated
  def myProfile() = AuthenticatedAction() { implicit request =>
    val user = request.user
    Ok(views.html.myaccount.designProfile(user))
  }

  def editUser() = AuthenticatedAction() { implicit request =>
    val user = models.authentication.EditUser(request.user.email, request.user.firstName, request.user.lastName, request.user.password)
    Ok(views.html.myaccount.designEdit(editUserForm.fill(user.copy(password = "")), request.user))
  }

  def editPassword() = AuthenticatedAction() { implicit request =>
    Ok(views.html.myaccount.designEditPassword(editPasswordForm, request.user))
  }

  def saveEditionUser() = AuthenticatedAction() { implicit request =>
    val cuser = models.authentication.EditUser(request.user.email, request.user.firstName, request.user.lastName, request.user.password)

    editUserForm.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable

        BadRequest(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser), request.user))

      },
      success => {

        val filledForm = editUserForm.fill(success.copy(password = ""))

        UserRepository.getByEmail(request.user.email) match {
          case Some(user) =>

            if (PasswordAuthentication.authenticate(success.password, user.password)) {
              val newUser = EditUser(user.email, success.firstName, success.lastName, PasswordAuthentication.passwordHash(success.password))

              repositories.authentication.UserRepository.editUser(newUser)
              val upDatedUser = UserRepository.getByEmail(newUser.email)
              Ok(views.html.myaccount.designProfile(upDatedUser.value.get.get.get))

            }
            else {


              Unauthorized(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser.copy(password = "")), request.user))

            }

          case None =>
            Unauthorized(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser.copy(password = "")), request.user))
        }
      }
    )

  }


  def saveEditionPassword() = AuthenticatedAction(){
    implicit request =>
      editPasswordForm.bindFromRequest.fold(
        error => {

          // Request payload is invalid.envisageable
         BadRequest(views.html.myaccount.designEditPassword(editPasswordForm.withGlobalError("error.invalidPassword"), request.user))
        },
        success =>  {

          val filledForm = editPasswordForm.fill(success)
          val futureOptionUser = UserRepository.getByEmail(request.user.email)

           futureOptionUser.map(optionUser =>
            optionUser match {
              case Some(user) =>
                if (PasswordAuthentication.authenticate(success.oldPassword, user.password)) {
                  val newUser = User(user.email, user.firstName, user.lastName, PasswordAuthentication.passwordHash(success.newPassword), user.dateRegistration)

                  repositories.authentication.UserRepository.editPassword(newUser)
                  Ok(views.html.myaccount.designProfile(newUser))

                }
                else {
                  Unauthorized(views.html.myaccount.designEditPassword(editPasswordForm.withGlobalError("error.invalidPassword"), request.user))
                }
              case None =>
                Unauthorized(views.html.myaccount.designEditPassword(editPasswordForm.withGlobalError("error.invalidPassword"), request.user))
            })
          }
      )

  }

}
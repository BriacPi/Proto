package controllers


import javax.inject.Inject

import components.mvc.AuthController
import components.user.{PasswordAuthentication, SessionManager}


import models.authentication._


import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}

import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories.authentication.UserRepository
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class UserController @Inject()(ws: WSClient)(val messagesApi: MessagesApi) extends AuthController with I18nSupport {
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

  def temp() = Action { implicit request =>
    Ok(views.html.users.addUser(addUserForm))
  }

  def addUser() = Action.async { implicit request =>
    addUserForm.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable
        Future.successful(BadRequest(views.html.authentication.authentication(form.withGlobalError("error.invalidUserOrPassword"))))
      },
      userData => {
        val refillForm = addUserForm.fill(userData)
        UserRepository.getByEmail(userData.email).map {
          case None =>
            val newUser: TemporaryUser = userData.copy(password = PasswordAuthentication.passwordHash(userData.password))
            repositories.authentication.UserRepository.create(newUser)
            Redirect(routes.UserController.editUser())
          case Some(u) => BadRequest(views.html.authentication.authentication(form.withGlobalError("error.invalidUserOrPassword")))
        }
        /* binding success, you get the actual value. */

      }
    )
  }

  def editUser() = AuthenticatedAction() { implicit request =>
    val user = models.authentication.EditUser(request.user.email, request.user.firstName, request.user.lastName, request.user.password)
    Ok(views.html.myaccount.designEdit(editUserForm.fill(user.copy(password = "")), request.user))
  }

  def editPassword() = AuthenticatedAction() { implicit request =>
    Ok(views.html.myaccount.designEditPassword(editPasswordForm, request.user))
  }

  def saveEditionUser() = AuthenticatedAction().async { implicit request =>
    val cuser = models.authentication.EditUser(request.user.email, request.user.firstName, request.user.lastName, request.user.password)

    editUserForm.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable

        Future.successful(BadRequest(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser), request.user)))

      },
      success => {

        val filledForm = editUserForm.fill(success.copy(password = ""))

        UserRepository.getByEmail(request.user.email).flatMap {
          case Some(user) =>

            if (PasswordAuthentication.authenticate(success.password, user.password)) {
              val newUser = EditUser(user.email, success.firstName, success.lastName, PasswordAuthentication.passwordHash(success.password))

              repositories.authentication.UserRepository.editUser(newUser)
              val upDatedUser: Future[Option[User]] = UserRepository.getByEmail(newUser.email)
              upDatedUser.map {
                case Some(user) => Ok(views.html.myaccount.designProfile(user))
                case None => BadRequest(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser), request.user))
              }
            }
            else {
              Future.successful(Unauthorized(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser.copy(password = "")), request.user)))
            }

          case None =>
            Future.successful(Unauthorized(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser.copy(password = "")), request.user)))
        }
      }
    )

  }

  def saveEditionPassword() = AuthenticatedAction().async { implicit request =>
    editPasswordForm.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable
        Future.successful(BadRequest(views.html.myaccount.designEditPassword(editPasswordForm.withGlobalError("error.invalidPassword"), request.user)))
      },
      success => {

        val filledForm = editPasswordForm.fill(success)

        UserRepository.getByEmail(request.user.email).map {
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

        }
      }
    )

  }

}
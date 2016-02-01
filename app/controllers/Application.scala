package controllers

import javax.inject.Inject

import akka.actor._
import components.mvc.AuthController
import models.authentication.User


import models._

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n.{MessagesApi, Messages, I18nSupport}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Writes, Json}
import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories._
import scala.concurrent.Future
import scala.util.{Success, Failure}

class Application @Inject()(ws: WSClient)(system: ActorSystem)(val messagesApi: MessagesApi) extends AuthController with I18nSupport {


  //Userform
  val userForm: Form[String] = Form("new value" -> of[String])



}
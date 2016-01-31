package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  def demo = Action {
    Ok(views.html.demo())
  }
  def error404 = Action {
    Ok(views.html.error404())
  }


}



package components.mvc

import components.user.SessionManager
import models.authentication.{LoginValues, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class RegularRequest[A](val user: Option[User], request: Request[A]) extends WrappedRequest[A](request)

trait RegularController extends Controller {

  def RegularAction[A]() = new RegularActionBuilder()

  class RegularActionBuilder() extends ActionBuilder[RegularRequest] {
    val form: Form[LoginValues] = Form(
      mapping(
        "email" -> nonEmptyText,
        "password" -> nonEmptyText
      )(LoginValues.apply)(LoginValues.unapply)
    )

    def invokeBlock[A](request: Request[A], block: RegularRequest[A] => Future[Result]) = {
      val futureOptionUser: Future[Option[User]] = SessionManager.fetch(request)
      futureOptionUser.flatMap { optionUser =>
        block(new RegularRequest(optionUser, request))
      }
    }
  }

}
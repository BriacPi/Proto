
package components.mvc

import components.user.SessionManager
import models.authentication.{LoginValues, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait Authorization {
  def accept(user: User): Boolean
}

object NeverAuthorize extends Authorization {
  def accept(user: User): Boolean = false
}

object AlwaysAuthorize extends Authorization {
  def accept(user: User): Boolean = true
}

class AuthenticatedRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

trait AuthController extends Controller {

  def AuthenticatedAction[A](authorization: Authorization = AlwaysAuthorize) = new AuthActionBuilder(authorization)

  class AuthActionBuilder(authorization: Authorization) extends ActionBuilder[AuthenticatedRequest] {
    val form: Form[LoginValues] = Form(
      mapping(
        "email" -> nonEmptyText,
        "password" -> nonEmptyText
      )(LoginValues.apply)(LoginValues.unapply)
    )

    def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
      val futureOptionUser: Future[Option[User]] = SessionManager.fetch(request)
      futureOptionUser.flatMap {
        case Some(user) if authorization.accept(user) => block(new AuthenticatedRequest(user, request))
        case _ =>
          Future.successful(Redirect(controllers.routes.AuthenticationController.welcome()))
      }
    }
  }
}
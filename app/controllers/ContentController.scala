package controllers


import javax.inject.Inject

import components.mvc.AuthController
import components.user.{PasswordAuthentication, SessionManager}


import models.authentication._
import models.contents.Article

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.json.{JsObject, JsValue}

import play.api.libs.ws.WSClient
import play.api.mvc._
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import repositories.authentication._
import repositories.content._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


import play.api.libs.json._

/**
  * Created by corpus on 11/02/2016.
  */

class ContentController @Inject()(ws: WSClient)(val messagesApi: MessagesApi) extends AuthController with I18nSupport {

  val articleTitleForm: Form[String] = Form(
      "title" -> nonEmptyText
  )

  def getAllArticles() = Action.async { implicit request => {
    val futureArticles: Future[List[Article]] = ArticleRepository.getAllArticles()
    val futureJson: Future[List[JsValue]] = futureArticles.map { list => list.map {
      article => Article.articleWriter.writes(article)
    }
    }
    futureJson.map { jsonList =>
      Ok(Json.obj("articles" -> jsonList))
    }
  }
  }

  def getArticlesByAuthor(): Action[AnyContent] = AuthenticatedAction().async { implicit request => {
    val futureArticles: Future[List[Article]] = ArticleRepository.getByUser(request.user)
    val futureJson: Future[List[JsValue]] = futureArticles.map { list => list.map {
      article => Article.articleWriter.writes(article)
    }
    }
    futureJson.map { jsonList =>
      Ok(Json.obj("articles" -> jsonList))
    }
  }
  }

  def articleToLike() = AuthenticatedAction() { implicit request => {
    Ok(views.html.demo(articleTitleForm))
  }
  }

  def likes(article: Article) = AuthenticatedAction().async { implicit request => {
    val userFutureOptionId: Future[Option[BSONDocument]] = UserRepository.getId(request.user)
    val articleFutureOptionId: Future[Option[BSONDocument]] = ArticleRepository.getId(article)

    userFutureOptionId.flatMap {
      case None => Future {
        Ok(Json.obj("error.messages:" -> "the user does not exist"))
      }
      case Some(userId) => {
        articleFutureOptionId.map {
          case None => Ok(Json.obj("error.messages:" -> "the article does not exist"))
          case Some(articleId) => {
            LikeRepository.create(
              userId.getAs[BSONObjectID]("_id").get, articleId.getAs[BSONObjectID]("_id").get)
            Ok(Json.obj("messages:" -> "the user successfully liked the article"))
          }
        }
      }
    }

  }

  }


  def likes(title: String) = AuthenticatedAction().async { implicit request => {
    val userFutureOptionId: Future[Option[BSONDocument]] = UserRepository.getId(request.user)
    val articleFutureOptionId: Future[Option[BSONDocument]] = ArticleRepository.getOneIdByTitle(title)

    userFutureOptionId.flatMap {
      case None => Future {
        Ok(Json.obj("error.messages:" -> "the user does not exist"))
      }
      case Some(userId) => {
        articleFutureOptionId.flatMap {
          case None => Future{Ok(Json.obj("error.messages:" -> "the article does not exist"))}
          case Some(articleId) => {
            val futureString = LikeRepository.create(
              userId.getAs[BSONObjectID]("user_id").get, articleId.getAs[BSONObjectID]("article_id").get)
            futureString.map (
              string =>  Ok(Json.obj("messages:" -> string))
            )

          }
        }
      }
    }

  }

  }
}
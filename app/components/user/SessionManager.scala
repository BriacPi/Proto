/**
 * Copyright (C) 2015 Captain Dash - All Rights Reserved
 */

package components.user

import models.authentication.User
import repositories.authentication.UserRepository
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.Try

import play.api.mvc.Result
import play.api.mvc.RequestHeader
import play.api.libs.Crypto

trait SessionManager {

  val SESSION_KEY = "user"

  def create(result: Result, user: User): Result = {
    result.withSession(SESSION_KEY -> Crypto.encryptAES(user.email))
  }

  def destroy(result: Result): Result = {
    result.withNewSession
  }

  def fetch(request: RequestHeader): Future[Option[User]] = {
    // TODO Use Hystrix

    get(request) match {
      case Some(email) => UserRepository.getByEmail(email)
      case None => Future{None}
    }
  }

  private[this] def get(request: RequestHeader): Option[String] = {
    Try(
      request.session.get(SESSION_KEY)
        .map(Crypto.decryptAES)

    )
    .toOption
    .flatten
  }

}

object SessionManager extends SessionManager
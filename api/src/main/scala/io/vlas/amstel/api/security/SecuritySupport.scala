package io.vlas.amstel.api.security

import akka.http.scaladsl.model.headers.HttpChallenges.basic
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.Provided
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Directive1}

import scala.concurrent.{ExecutionContext, Future}

object SecuritySupport {

  type UserProvider = User.Id => Future[Option[User.Password]]
}

trait SecuritySupport {

  import SecuritySupport.UserProvider

  def secureBasic(userProvider: UserProvider,
                  realm: String): Directive1[User.Id] =
    extractExecutionContext flatMap { ec =>
      authenticate(fetchValidatedUser(userProvider, ec), realm)
    }

  /**
    * If the check fails the route is rejected.
    */
  private def authenticate(authenticator: AsyncAuthenticator[User.Id], realm: String): Directive1[User.Id] =
    extractCredentials flatMap { credentialsMaybe =>
      onSuccess(authenticator(Credentials(credentialsMaybe))) flatMap {
        case Some(user) => provide(user)
        case None => reject(AuthenticationFailedRejection(CredentialsRejected, basic(realm)))
      }
    }

  private def fetchValidatedUser(userProvider: UserProvider, ec: ExecutionContext)
                                (credentials: Credentials): Future[Option[User.Id]] =
    credentials match {
      case provided @ Provided(userId) =>
        implicit val _ec: ExecutionContext = ec
        userProvider(userId) map {
          case Some(password) if provided verify password => Some(userId)
          case _ => None
        }
      case _ => Future.successful(None)
    }
}

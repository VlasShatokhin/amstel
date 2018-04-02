package io.vlas.amstel.api.security

import io.vlas.amstel.api.security.SecuritySupport.UserProvider

import scala.concurrent.Future

object User {

  type Id = String
  type Password = String

  def createUserProvider(securityConfig: SecurityConfiguration): UserProvider =
    userId => Future.successful {
      securityConfig.users.get(userId)
    }
}

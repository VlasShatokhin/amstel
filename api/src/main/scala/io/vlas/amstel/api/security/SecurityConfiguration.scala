package io.vlas.amstel.api.security

import com.typesafe.config.Config

import scala.collection.JavaConverters._

object SecurityConfiguration {

  def parseFrom(config: Config): SecurityConfiguration =
    SecurityConfiguration (
      config.getObjectList("security.users").asScala
        .map (_.toConfig)
        .map { userConf =>
          (userConf.getString("user"), userConf.getString("password"))
        }.toMap
    )
}

case class SecurityConfiguration(users: Map[User.Id, User.Password])

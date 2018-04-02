package io.vlas.amstel.api

import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.softwaremill.macwire._
import com.softwaremill.macwire.akkasupport._
import com.typesafe.config.ConfigFactory
import io.vlas.amstel.api.security.SecuritySupport.UserProvider
import io.vlas.amstel.api.security.{SecurityConfiguration, User}
import io.vlas.amstel.core.Context
import io.vlas.amstel.state.impl.ManagerActor
import io.vlas.amstel.state.{EventListener, StateService}

import scala.util.{Failure, Success}

object StatisticsApp extends App with StatisticsApi
  with Context {

  val config = ConfigFactory.load("security.conf")
    .withFallback(ConfigFactory.load("application.conf"))

  implicit val system: ActorSystem = ActorSystem("amstel")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout =
    Timeout(config.getDuration("http.timeout").getSeconds, SECONDS)

  lazy val stateManager: ActorRef = wireActor[ManagerActor]("state-manager-actor")
  lazy val stateService: StateService = wire[StateService]
  lazy val securityConfig: SecurityConfiguration = wireWith(SecurityConfiguration.parseFrom _)
  lazy val userProvider: UserProvider = wireWith(User.createUserProvider _)

  EventListener.subscribe(stateService)

  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  Http().bindAndHandle(route, host, port) onComplete {
    case Success(binding) =>
      system.log.info(s"Listening to events on ${binding.localAddress}")
    case Failure(x) =>
      system.log.error(x.getMessage)
  }
}

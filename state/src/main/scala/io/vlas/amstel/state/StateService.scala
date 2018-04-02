package io.vlas.amstel.state

import akka.actor.ActorRef
import akka.http.scaladsl.model.DateTime
import akka.pattern.ask
import akka.util.Timeout
import io.vlas.amstel.core.model.{Event, Statistics}
import io.vlas.amstel.state.StateService.Created
import io.vlas.amstel.state.impl.ManagerActor.{GetStatistics, PostEvent}

import scala.concurrent.Future

object StateService {

  def apply(manager: ActorRef)
           (implicit timeout: Timeout): StateService =
    new StateService(manager)

  case class Created(timestamp: Long)
}

class StateService(manager: ActorRef)
                  (implicit timeout: Timeout) {

  def insertEvent(groupId: String, deviceId: String, event: Event): Future[Created] =
    (manager ? PostEvent(groupId, deviceId, event)).mapTo[Created]

  def fetchStatistics(groupId: String,
                      from: DateTime, to: DateTime): Future[Option[Statistics]] =
    (manager ? GetStatistics(groupId, None, from, to)).mapTo[Option[Statistics]]

  def fetchStatistics(groupId: String, deviceIds: List[String],
                      from: DateTime, to: DateTime): Future[Option[Statistics]] =
    (manager ? GetStatistics(groupId, Some(deviceIds), from, to)).mapTo[Option[Statistics]]
}

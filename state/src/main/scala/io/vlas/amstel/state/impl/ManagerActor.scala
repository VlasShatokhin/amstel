package io.vlas.amstel.state.impl

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.DateTime
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import io.vlas.amstel.core.model.Event
import io.vlas.amstel.state.impl.ManagerActor.{GetStatistics, PostEvent}

import scala.concurrent.ExecutionContext

object ManagerActor {

  def props()(implicit timeout: Timeout): Props =
    Props(new ManagerActor())

  case class PostEvent(groupId: String, deviceId: String, event: Event)
  case class GetStatistics(groupId: String, deviceIds: Option[List[String]],
                           from: DateTime, to: DateTime)
}

class ManagerActor(implicit timeout: Timeout) extends Actor {

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {

    case PostEvent(groupId, deviceId, event) =>
      val group = context.child(groupId) getOrElse {
        context.actorOf(GroupActor.props(groupId), groupId)
      }
      (group ? GroupActor.PostEvent(deviceId, event)) pipeTo sender()

    case GetStatistics(groupId, deviceIds, from, to) =>
      context.child(groupId).map {
        _ ? GroupActor.GetStatistics(deviceIds, from, to)
      } match {
        case Some(future) => future pipeTo sender()
        case None => sender() ! None
      }
  }
}

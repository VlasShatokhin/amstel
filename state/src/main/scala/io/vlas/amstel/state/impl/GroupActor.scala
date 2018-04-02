package io.vlas.amstel.state.impl

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.DateTime
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import io.vlas.amstel.core.model.{Event, Statistics}
import io.vlas.amstel.state.impl.GroupActor._
import io.vlas.amstel.state.impl.model.Preaggregated

import scala.concurrent.{ExecutionContext, Future}

object GroupActor {

  type StatisticsMaybe = Iterable[Option[Preaggregated]]

  def props(id: String)(implicit timeout: Timeout): Props =
    Props(new GroupActor(id))

  case class PostEvent(deviceId: String, event: Event)
  case class GetStatistics(deviceIdsMaybe: Option[List[String]],
                           from: DateTime, to: DateTime)
}

class GroupActor(groupId: String)
                (implicit timeout: Timeout) extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher

  override def preStart(): Unit =
    log.info(s"Group $groupId registered")

  override def receive: Receive = {

    case PostEvent(deviceId, event) =>
      val device = context.child(deviceId) getOrElse {
        context.actorOf(DeviceActor.props(groupId, deviceId), deviceId)
      }
      (device ? DeviceActor.PostEvent(event)) pipeTo sender()

    case GetStatistics(deviceIdsMaybe, from, to) =>
      val devices = deviceIdsMaybe match {
        case Some(deviceIds) => deviceIds flatMap { context.child }
        case None => context.children
      }

      fetchStatistics(devices, from, to).map(aggregateStatistics) pipeTo sender()
  }

  private def fetchStatistics(devices: Iterable[ActorRef],
                              from: DateTime, to: DateTime): Future[StatisticsMaybe] =
    Future.sequence {
      devices map { device => (device ? DeviceActor.GetStats(from, to)).mapTo[Option[Preaggregated]] }
    }

  private def aggregateStatistics(iterable: StatisticsMaybe): Option[Statistics] =
    iterable
      .flatten
      .reduceOption(_ + _)
      .map(Preaggregated.toStatistics)
}

package io.vlas.amstel.state.impl

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.DateTime
import io.vlas.amstel.core.model.Event
import io.vlas.amstel.state.StateService.Created
import io.vlas.amstel.state.impl.model.Preaggregated

import scala.collection.SortedMap
import scala.collection.immutable.TreeMap

object DeviceActor {

  def props(groupId: String, deviceId: String): Props =
    Props(new DeviceActor(groupId, deviceId))

  case class PostEvent(event: Event)
  case class GetStats(from: DateTime, to: DateTime)

}

class DeviceActor(groupId: String, deviceId: String) extends Actor with ActorLogging {
  import DeviceActor._

  private var state: SortedMap[DateTime, Preaggregated] = TreeMap.empty {
    implicitly[Ordering[DateTime]].reverse
  }

  override def preStart(): Unit =
    log.info(s"Device $deviceId registered in group $groupId")

  override def receive: Receive = {

    case PostEvent(Event(timestamp, value)) =>
      val second = DateTime(timestamp)
      val updated = state.updated(second, state.get(second) match {
        case Some(preaggregated) => preaggregated + value
        case None => Preaggregated(value)
      })
      state = updated
      log.info(s"Event posted by device $deviceId from group $groupId")
      sender() ! Created(timestamp)

    case GetStats(from, to) =>
      val statistics = state
        .filterKeys { dateTime => dateTime >= from && dateTime <= to }
        .values.reduceOption(_ + _)
      sender ! statistics
  }

}

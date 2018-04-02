package io.vlas.amstel.state

import akka.actor.{Actor, ActorSystem, Props}
import io.vlas.amstel.core.model.EventEnvelope

object EventListener {

  def props(stateService: StateService): Props =
    Props(new EventListener(stateService))

  def subscribe(stateService: StateService)
               (implicit system: ActorSystem): Boolean = {
    val eventListener = system.actorOf(EventListener.props(stateService))
    system.eventStream.subscribe(eventListener, classOf[EventEnvelope])
  }
}

class EventListener(stateService: StateService) extends Actor {

  override def receive: Receive = {

    case EventEnvelope(groupId, deviceId, event) =>
      stateService.insertEvent(groupId, deviceId, event)
  }
}

package io.vlas.amstel.publisher

import io.vlas.amstel.core.Context
import io.vlas.amstel.core.model.{Event, EventEnvelope}

import scala.concurrent.Future

trait PublisherService {
  this: Context =>

  def publishDeviceEvent(groupId: String, deviceId: String, event: Event): Future[Unit] =
    publishEvent {
      EventEnvelope(groupId, deviceId, event)
    }

  private def publishEvent(envelope: EventEnvelope): Future[Unit] = Future {
    system.eventStream.publish(envelope)
  }
}

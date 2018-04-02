package io.vlas.amstel.core.marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.vlas.amstel.core.model.{EventEnvelope, Event, Statistics}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport
  with DefaultJsonProtocol {

  implicit val statisticsFormat: RootJsonFormat[Statistics] = jsonFormat5(Statistics.apply)
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat2(Event)
  implicit val envelopeFormat: RootJsonFormat[EventEnvelope] = jsonFormat3(EventEnvelope)

}

package io.vlas.amstel.publisher

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.vlas.amstel.core.Context
import io.vlas.amstel.core.marshalling.JsonSupport
import io.vlas.amstel.core.model.Event

import scala.util.{Failure, Success}

trait PublisherApi extends PublisherService with JsonSupport {
  this: Context =>

  def mutationRoute: Route =
    path("events" / "group" / Segment / "device" / Segment) {
      (groupId, deviceId) =>
        (post & entity(as[Event])) { event =>
          onComplete {
            publishDeviceEvent(groupId, deviceId, event).mapTo[Unit]
          } {
            case Success(_) => complete(StatusCodes.Created)
            case Failure(x) => complete((StatusCodes.InternalServerError, x.getMessage))
          }
        }
    }
}

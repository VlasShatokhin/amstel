package io.vlas.amstel.statistics_reader

import akka.http.scaladsl.model.{DateTime, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.vlas.amstel.core.Context
import io.vlas.amstel.core.marshalling.JsonSupport

trait ReaderApi extends ReaderService with JsonSupport {
  this: Context =>

  def readingRoute: Route =
    pathPrefix("statistics" / "group" / Segment) { groupId =>
      get {
        parameters(('from.as[Long], 'to.as[Long])) { (from, to) =>
          onSuccess {
            fetchStatistics(groupId, DateTime(from), DateTime(to))
          } {
            case Some(stats) => complete((StatusCodes.OK, stats))
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
      path("device" / Segments) { deviceIds =>
        get {
          parameters(('from.as[Long], 'to.as[Long])) { (from, to) =>
            onSuccess {
              fetchStatistics(groupId, deviceIds, DateTime(from), DateTime(to))
            } {
              case Some(stats) => complete((StatusCodes.OK, stats))
              case None => complete(StatusCodes.NotFound)
            }
          }
        }
      }
    }
}

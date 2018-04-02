package io.vlas.amstel.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.vlas.amstel.api.security.SecuritySupport
import io.vlas.amstel.api.security.SecuritySupport.UserProvider
import io.vlas.amstel.core.Context
import io.vlas.amstel.publisher.PublisherApi
import io.vlas.amstel.statistics_reader.ReaderApi

trait StatisticsApi extends ReaderApi with PublisherApi with SecuritySupport {
  this: Context =>

  def userProvider: UserProvider

  def route: Route =
    mutationRoute ~ secureBasic(userProvider, "statistics-api") { _ =>
      readingRoute
    }

}

package io.vlas.amstel.statistics_reader

import akka.http.scaladsl.model.DateTime
import io.vlas.amstel.core.Context
import io.vlas.amstel.core.model.Statistics
import io.vlas.amstel.state.StateService

import scala.concurrent.Future

trait ReaderService {
  this: Context =>

  def stateService: StateService

  def fetchStatistics(groupId: String,
                      from: DateTime, to: DateTime): Future[Option[Statistics]] =
    stateService.fetchStatistics(groupId, from, to)

  def fetchStatistics(groupId: String, deviceIds: List[String],
                      from: DateTime, to: DateTime): Future[Option[Statistics]] =
    stateService.fetchStatistics(groupId, deviceIds, from, to)

}

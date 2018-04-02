package io.vlas.amstel.state

import akka.actor.ActorSystem
import akka.http.scaladsl.model.DateTime
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import io.vlas.amstel.core.model.{Event, Statistics}
import io.vlas.amstel.state.StateService.Created
import io.vlas.amstel.state.impl.ManagerActor
import org.scalatest.{AsyncWordSpecLike, BeforeAndAfterAll, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration._

object StateServiceSpec {

  implicit val timeout: Timeout = Timeout(10.seconds)

  val someGroupId = "group1"
  val someOtherGroupId = "group2"

  val someDeviceId = "device1"
  val someOtherDeviceId = "device2"

  val someValue = 100
  val someOtherValue = 200

  val someTimestamp: Long = System.currentTimeMillis()
  val someOtherTimestamp: Long = someTimestamp + 1000

  val someDateTime: DateTime = DateTime(someTimestamp)
  val someOtherDateTime: DateTime = DateTime(someOtherTimestamp)
}

class StateServiceSpec extends TestKit(ActorSystem("StateServiceSpec"))
  with ImplicitSender with AsyncWordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  import StateServiceSpec._

  "StateService" must {

    "insert an event" in {

      val stateService = StateService(system.actorOf(ManagerActor.props()))

      stateService
        .insertEvent(someGroupId, someDeviceId, Event(someTimestamp, someValue))
        .map {
          _ shouldBe Created(someTimestamp)
        }
    }

    "return stats for a single device" in {

      val stateService = StateService(system.actorOf(ManagerActor.props()))

      val insertsFuture = Future.sequence {
        List(
          stateService
            .insertEvent(someGroupId, someDeviceId, Event(someTimestamp, someValue)),
          stateService
            .insertEvent(someGroupId, someDeviceId, Event(someOtherTimestamp, someOtherValue)))
      }

      for {
        _ <- insertsFuture
        stats <- stateService.fetchStatistics(someGroupId, someDeviceId :: Nil, someDateTime, someOtherDateTime)
      } yield {
        stats shouldBe Some(Statistics(
          sum = someValue + someOtherValue,
          avg = (someValue + someOtherValue) / 2,
          max = someOtherValue,
          min = someValue,
          count = 2))
      }
    }

    "return stats for multiple devices within one group" in {

      val stateService = StateService(system.actorOf(ManagerActor.props()))

      val insertsFuture = Future.sequence {
        List(
          stateService
            .insertEvent(someGroupId, someDeviceId, Event(someTimestamp, someValue)),
          stateService
            .insertEvent(someGroupId, someOtherDeviceId, Event(someOtherTimestamp, someOtherValue)))
      }

      for {
        _ <- insertsFuture
        stats <- stateService
          .fetchStatistics(someGroupId, List(someDeviceId, someOtherDeviceId), someDateTime, someOtherDateTime)
      } yield {
        stats shouldBe Some(Statistics(
          sum = someValue + someOtherValue,
          avg = (someValue + someOtherValue) / 2,
          max = someOtherValue,
          min = someValue,
          count = 2))
      }
    }

    "return stats for one device within a group" in {

      val stateService = StateService(system.actorOf(ManagerActor.props()))

      val insertsFuture = Future.sequence {
        List(
          stateService
            .insertEvent(someGroupId, someDeviceId, Event(someTimestamp, someValue)),
          stateService
            .insertEvent(someGroupId, someOtherDeviceId, Event(someOtherTimestamp, someOtherValue)))
      }

      for {
        _ <- insertsFuture
        stats <- stateService
          .fetchStatistics(someGroupId, List(someDeviceId), someDateTime, someOtherDateTime)
      } yield {
        stats shouldBe Some(Statistics(
          sum = someValue,
          avg = someValue,
          max = someValue,
          min = someValue,
          count = 1))
      }
    }

    "not return stats for one device if no events posted" in {

      val stateService = StateService(system.actorOf(ManagerActor.props()))

      val insertFuture = stateService
        .insertEvent(someGroupId, someDeviceId, Event(someTimestamp, someValue))

      for {
        _ <- insertFuture
        stats <- stateService
          .fetchStatistics(someGroupId, List(someOtherDeviceId), someDateTime, someOtherDateTime)
      } yield {
        stats shouldBe None
      }
    }

    "return stats for whole group" in {

      val stateService = StateService(system.actorOf(ManagerActor.props()))

      val insertsFuture = Future.sequence {
        List(
          stateService
            .insertEvent(someGroupId, someDeviceId, Event(someTimestamp, someValue)),
          stateService
            .insertEvent(someGroupId, someOtherDeviceId, Event(someOtherTimestamp, someOtherValue)))
      }

      for {
        _ <- insertsFuture
        stats <- stateService.fetchStatistics(someGroupId, someDateTime, someOtherDateTime)
      } yield {
        stats shouldBe Some(Statistics(
          sum = someValue + someOtherValue,
          avg = (someValue + someOtherValue) / 2,
          max = someOtherValue,
          min = someValue,
          count = 2))
      }
    }

    "return group stats only within valid time range" in {

      val stateService = StateService(system.actorOf(ManagerActor.props()))

      val insertsFuture = Future.sequence {
        List(
          stateService
            .insertEvent(someGroupId, someDeviceId, Event(someTimestamp, someValue)),
          stateService
            .insertEvent(someGroupId, someOtherDeviceId, Event(someOtherTimestamp, someOtherValue)))
      }

      for {
        _ <- insertsFuture
        stats <- stateService.fetchStatistics(someGroupId, someDateTime, someDateTime)
      } yield {
        stats shouldBe Some(Statistics(
          sum = someValue,
          avg = someValue,
          max = someValue,
          min = someValue,
          count = 1))
      }
    }
  }
}

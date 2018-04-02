import sbt.{Def, SettingKey, Task, TaskKey, settingKey, taskKey}
import skinny.http.{HTTP, Request}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

object EventLoad {

  val numDevices: SettingKey[Int] = settingKey[Int]("number of devices")
  val host: SettingKey[String] = settingKey[String]("target host")
  val port: SettingKey[Int] = settingKey[Int]("target port")

  val generateLoad: TaskKey[Unit] = taskKey[Unit]("generates load to service")

  lazy val startLoadGeneration: Def.Initialize[Task[Unit]] = Def.task {

    val devicesRange = 1 to numDevices.value
    val sleepable = executeAndWait(1.second) _
    val random = Random

    while(true) {
      sleepable {
        devicesRange.foreach { device => HTTP
          .asyncPost {
            Request(s"http://${host.value}:${port.value}/events/group/group1/device/$device")
              .body(
                s"""{
                   |  "timestamp": ${System.currentTimeMillis()},
                   |  "value": ${random.nextDouble()}
                   |}
                  """.stripMargin.getBytes,
                "application/json")
          }
          .map(_.asString)
          .map(println)
        }
      }
    }
  }

  private def executeAndWait(duration: Duration)(exec: => Unit): Unit = {
    exec
    Thread.sleep(duration.toMillis)
  }
}

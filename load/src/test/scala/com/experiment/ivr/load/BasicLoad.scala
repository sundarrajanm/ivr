package com.experiment.ivr.load

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class BasicLoad extends Simulation {
  val hostName = "localhost:8080"
  val feeder = Iterator.continually(Map("name" -> (Random.alphanumeric.take(20).mkString)))
  val httpConf = http.baseURL(s"http://$hostName")

  val steps =
    exec(http("Start")
      .post(s"/ivr/dummy")
      .check(status.is(200))
      .check(header(HttpHeaderNames.Location).saveAs("callId")))
      .pause(2 seconds)
      .feed(feeder)

    .exec(http("Got Next")
      .post(s"/ivr/dummy")
        .queryParam("sessionId", StringBody("${callId}"))
      .check(status.is(200))
//      .check(xpath("//vxml/form/field/prompt")
//        .is("Would you like coffee, tea, milk, or nothing?"))
      )
      .pause(2 seconds)

    .exec(http("Select Tea").post(s"/ivr/dummy")
      .queryParam("sessionId", StringBody("${callId}"))
      .queryParam("userInput", StringBody("tea"))
      .check(status.is(200))
//      .check(xpath("//vxml/form/block")
//        .is("Not a bad choice."))
    )
      .pause(2 seconds)

    .exec(http("Go next - End").post(s"/ivr/dummy")
      .queryParam("sessionId", StringBody("${callId}"))
      .check(status.is(200))
//      .check(xpath("//vxml/form/block")
//        .is(""))
    )
      .pause(2 seconds)

  val scn = scenario("BasicFlow").exec(steps)

  setUp(
    scn.inject(
      rampUsersPerSec(1) to (50) during (30 seconds),
      constantUsersPerSec(100) during (60 seconds),
      rampUsersPerSec(50) to (1) during (10 seconds))
  ).protocols(httpConf).assertions(
    global.successfulRequests.percent.is(100)
  )
}

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
      .check(header(HttpHeaderNames.Location).saveAs("callId"))
      .check(xpath("//pre:block", List("pre" -> "http://www.w3.org/2001/vxml"))
        .is("Hello, Welcome to Cisco Cloud IVR Server"))
    )
      .pause(2 seconds)
      .feed(feeder)

      .exec(http("Got to choice")
        .post(s"/ivr/dummy")
        .queryParam("sessionId", StringBody("${callId}"))
        .check(status.is(200))
        .check(xpath("//pre:prompt", List("pre" -> "http://www.w3.org/2001/vxml"))
          .is("Do you want a Beer or Tea?"))
      )
      .pause(2 seconds)
      .randomSwitch(
        50d ->
          exec(http("Select Tea").post(s"/ivr/dummy")
            .queryParam("sessionId", StringBody("${callId}"))
            .queryParam("userInput", StringBody("tea"))
            .check(status.is(200))
            .check(xpath("//pre:block", List("pre" -> "http://www.w3.org/2001/vxml"))
              .is("Not a bad choice."))
          ),

        50d ->
          exec(http("Select Beer").post(s"/ivr/dummy")
            .queryParam("sessionId", StringBody("${callId}"))
            .queryParam("userInput", StringBody("beer"))
            .check(status.is(200))
            .check(xpath("//pre:block", List("pre" -> "http://www.w3.org/2001/vxml"))
              .is("Excellent choice."))
          )
      )
      .pause(2 seconds)
      .exec(http("Go to end").post(s"/ivr/dummy")
        .queryParam("sessionId", StringBody("${callId}"))
        .check(status.is(200))
        .check(xpath("//pre:block", List("pre" -> "http://www.w3.org/2001/vxml"))
          .is(""))
      )
      .pause(2 seconds)
      .exec(http("Session Cleared").post(s"/ivr/dummy")
        .queryParam("sessionId", StringBody("${callId}"))
        .check(status.is(404))
      )

  val scn = scenario("BasicFlow").exec(steps)

  setUp(
    scn.inject(
      rampUsersPerSec(1) to (100) during (1 minute),
      constantUsersPerSec(100) during (3 minute),
      rampUsersPerSec(100) to (1) during (1 minute))
  ).protocols(httpConf).assertions(
    global.successfulRequests.percent.is(100)
  )
}

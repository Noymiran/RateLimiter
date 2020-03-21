package throttling

import java.time.Duration

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.jdk.DurationConverters._

object Main extends Logging with App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val conf = ConfigFactory.load()
  val maxPermitsConf: Int = conf.getInt("maxPermits")
  val duration: Duration = conf.getDuration("rateLimiterDuration")

  val rateLimiter = new SimpleRateLimiter(maxPermitsConf, duration.toScala)
  val akkaHttpClientThrottler = new AkkaHttpClientThrottler(rateLimiter)

  val hostName: String = "localhost"
  val portNum: Int = 8080
  val serverBiding = akkaHttpClientThrottler.serverBidingRequests(hostName, portNum)
  log.info(s"Server online at http://${hostName}:${portNum}/\nPress RETURN to stop...")
  StdIn.readLine()
  akkaHttpClientThrottler.shutDown(serverBiding)
}

package throttling

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.io.StdIn
import scala.jdk.DurationConverters._
import scala.language.postfixOps

object Main extends Logging with App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val conf = ConfigFactory.load()
  val maxPermitsConf: Int = Option(conf.getInt("maxPermits")).getOrElse(100)
  val duration: FiniteDuration = Option(conf.getDuration("rateLimiterDuration").toScala).getOrElse(1 hour)
  val hostName: String = Option(conf.getString("hostName")).getOrElse("localhost")
  val portNum: Int = Option(conf.getInt("port")).getOrElse(8080)

  val rateLimiter = SimpleRateLimiter(maxPermitsConf, duration)
  val akkaHttpClientThrottler = new AkkaHttpClientThrottler(rateLimiter)


  val serverBiding = akkaHttpClientThrottler.serverBidingRequests(hostName, portNum)
  log.info(s"Server online at http://${hostName}:${portNum}/\nPress RETURN to stop...")
  StdIn.readLine()
  akkaHttpClientThrottler.shutDown(serverBiding)
}

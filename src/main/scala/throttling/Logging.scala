package throttling

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  lazy val log: Logger = LoggerFactory.getLogger(this.getClass.getName)

}

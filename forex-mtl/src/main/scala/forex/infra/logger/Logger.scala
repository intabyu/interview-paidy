package forex.infra.logger

import org.slf4j.{Logger, LoggerFactory}

object ForexLogger {
  private var logger: Option[Logger] = None

  def get: Logger = {
    if (logger.isEmpty) {
      logger = Some(LoggerFactory.getLogger("forex"))
    }
    logger.get
  }
}

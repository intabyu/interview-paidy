package forex.services.rates

import cats.Applicative
import forex.config.ApplicationConfig
import interpreters._

import scala.concurrent.ExecutionContext

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()

  def oneFrame[F[_]: Applicative](config: ApplicationConfig)(implicit executionContext: ExecutionContext): Algebra[F] = {
//    new OneFrame[F](OneFrameClient(config))
    new OneFrame[F]()
  }
}

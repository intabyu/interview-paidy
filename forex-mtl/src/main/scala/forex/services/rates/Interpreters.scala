package forex.services.rates

import cats.Applicative
import forex.config.ApplicationConfig
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()

  def oneFrame[F[_]: Applicative](config: ApplicationConfig): Algebra[F] = {
    new OneFrame[F](new OneFrameClient(config.oneFrame))
//    new OneFrame[F]()
  }
}

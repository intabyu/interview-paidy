package forex.services.rates

import cats.Applicative
import forex.config.ApplicationConfig
import forex.services.rates.interpreters.oneframe.OneFrame

object Interpreters {
  def oneFrame[F[_]: Applicative](config: ApplicationConfig): Algebra[F] = {
    new OneFrame[F](new forex.infra.oneframe.SyncClient(config.oneFrame))
  }
}

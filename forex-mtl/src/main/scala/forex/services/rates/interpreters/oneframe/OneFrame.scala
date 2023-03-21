package forex.services.rates.interpreters.oneframe

import cats.Applicative
import cats.syntax.applicative._
import forex.domain.Rate
import forex.infra.logger.ForexLogger
import forex.services.rates.Algebra
import forex.services.rates.errors._

import java.time.{OffsetDateTime, ZoneOffset}

/**
  * (A) The service returns an exchange rate when provided with 2 supported currencies
  * (B) The rate should not be older than 5 minutes
  * (C) The service should support at least 10,000 successful requests per day with 1 API token
  *
  * (D) The One-Frame service supports a maximum of 1000 requests per day for any given authentication token.
  * (E) The One-Frame API [...] One or more pairs per request are allowed.
  *
  * - OneFrame service
  * - Cache service with expiration cache
  *
  * - 86400 seconds per day & (C) => 1 query on average, every 8 seconds
  * - 1440 minutes per day & (B) & (D) & (E) => 1 query every [2, 5[ minutes to the One-Frame API with all pairs
  */
class OneFrame[F[_]: Applicative](client: Client, cache: Cache, cacheDurationSeconds: Int) extends Algebra[F] {

  // arbitrary initial value long enough to force the first update
  private var lastUpdate = OffsetDateTime.of(1970, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC)

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    updateCache()
    val result = cache.get(pair)
    ForexLogger.get.debug(s"result: $result")
    result.pure[F]
  }

  private def updateCache(): Unit = {
    val now = OffsetDateTime.now

    val cacheAge = now.toEpochSecond - lastUpdate.toEpochSecond
    if (cacheAge >= cacheDurationSeconds) {
      ForexLogger.get.debug(s"cache is $cacheAge seconds old")
      val pairs: Either[String, List[Pair]] = client.fetchPairs()
      pairs match {
        case Left(err) => println(s"ERROR: $err")
        case Right(p)  => cache.update(p)
      }
      lastUpdate = now
    }
  }
}

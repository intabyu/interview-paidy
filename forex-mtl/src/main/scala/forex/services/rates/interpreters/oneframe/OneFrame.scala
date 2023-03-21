package forex.services.rates.interpreters.oneframe

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors._

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

class OneFrame[F[_]: Applicative](client: Client, cache: Cache) extends Algebra[F] {

  private var timestamp = 0

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val pairs: Either[String, List[Pair]] = client.fetchPairs()
    pairs match {
      case Left(err) => println(s"ERROR: $err")
      case Right(p) => p.foreach(println)
    }

    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]
  }
}

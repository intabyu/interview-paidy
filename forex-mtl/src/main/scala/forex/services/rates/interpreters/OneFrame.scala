package forex.services.rates.interpreters

import cats.Applicative
import cats.effect.{Blocker, _}
import cats.syntax.applicative._
import cats.syntax.either._
import forex.config.OneFrameConfig
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.http4s._
import org.http4s.client._
import org.http4s.implicits._

import java.util.concurrent._
import scala.concurrent.ExecutionContext.global

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

case class OneFramePair(from: String, to: String, bid: Float, ask: Float, price: Float, time_stamp: String)


class OneFrameClient(config: OneFrameConfig) {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  private val blockingPool = Executors.newFixedThreadPool(5)
  private val blocker = Blocker.liftExecutorService(blockingPool)
  private val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

  private val uri = Uri.fromString(config.url).getOrElse(uri"")
  private val headers = List(Header("token", config.token))

  def fetchAllPairs(): Either[io.circe.Error, List[OneFramePair]] = {
    val request: Request[IO] = Request[IO](
      Method.GET,
      uri,
      HttpVersion.`HTTP/1.1`,
      Headers(headers)
    )
    val query: IO[String] = httpClient.expect[String](request) // TODO: handle HTTP error
    val result: String = query.unsafeRunSync()
    val res: Either[io.circe.Error, List[OneFramePair]] = decode[List[OneFramePair]](result)
    res
  }
}

class OneFrame[F[_]: Applicative](client: OneFrameClient) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val pairs = client.fetchAllPairs()
    pairs match {
      case Left(err) => println(s"ERROR: $err")
      case Right(p) => p.foreach(println)
    }

    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]
  }
}

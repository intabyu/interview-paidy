package forex.services.rates.interpreters

import cats.Applicative
import cats.effect.{Blocker, _}
import cats.syntax.applicative._
import cats.syntax.either._
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

class OneFrame[F[_]: Applicative] extends Algebra[F] {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  val blockingPool = Executors.newFixedThreadPool(5)
  val blocker = Blocker.liftExecutorService(blockingPool)
  val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

  def fetchAllPairs(): Either[io.circe.Error,List[OneFramePair]] = {
    val target = uri"http://localhost:8081/rates?pair=USDJPY" // TODO: use config
    val headers = List(Header("token", "10dc303535874aeccc86a8251e6992f5")) // TODO: idem
    val request: Request[IO] = Request[IO](
      Method.GET,
      target,
      HttpVersion.`HTTP/1.1`,
      Headers(headers)
    )
    val query: IO[String] = httpClient.expect[String](request) // TODO: handle HTTP error
    val result: String = query.unsafeRunSync()
    val res: Either[io.circe.Error,List[OneFramePair]] = decode[List[OneFramePair]](result)
    res
  }

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val pairs = fetchAllPairs()
    pairs match {
      case Left(err) => println(s"ERROR: $err")
      case Right(p) => p.foreach(println)
    }

    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]
  }
}

package forex.infra.oneframe

import cats.effect.{Blocker, ContextShift, IO, Timer}
import forex.config.OneFrameConfig
import forex.services.rates.interpreters.oneframe.Pair
import io.circe.generic.auto._
import io.circe.parser.decode
import org.http4s._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global

class SyncClient(config: OneFrameConfig) extends forex.services.rates.interpreters.oneframe.Client {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO]     = IO.timer(global)

  private val blockingPool           = Executors.newFixedThreadPool(config.threadPool)
  private val blocker                = Blocker.liftExecutorService(blockingPool)
  private val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

  private val uri     = Uri.fromString(config.url).getOrElse(uri"")
  private val headers = List(Header("token", config.token))

  def fetchPairs(): Either[String, List[Pair]] = {
    val request: Request[IO] = Request[IO](
      Method.GET,
      uri,
      HttpVersion.`HTTP/1.1`,
      Headers(headers)
    )
    val query: IO[String]                       = httpClient.expect[String](request) // TODO: handle HTTP error
    val result: String                          = query.unsafeRunSync()
    val res: Either[io.circe.Error, List[Pair]] = decode[List[Pair]](result)

    res match {
      case Left(err)    => Left(err.toString)
      case Right(value) => Right(value)
    }
  }
}

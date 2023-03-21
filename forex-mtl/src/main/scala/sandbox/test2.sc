import cats.effect._
import org.http4s._
import org.http4s.implicits._
import cats.effect.Blocker
import org.http4s.client._

import java.util.concurrent._
import scala.concurrent.ExecutionContext.global
import forex.http.jsonDecoder
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import org.http4s.circe.jsonOf
import io.circe.parser.decode
import io.circe.generic.auto._, io.circe.syntax._

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create


case class OneFramePair(from: String, to: String, bid: Float, ask: Float, price: Float, time_stamp: String)

case class OneFrameResponse(pairs: List[OneFramePair])

def example(): IO[String] = {
  val target = uri"http://localhost:8081/rates?pair=USDJPY"
  val headers = List(Header("token", "10dc303535874aeccc86a8251e6992f5"))
  val request: Request[IO] = Request[IO](
    Method.GET,
    target,
    HttpVersion.`HTTP/1.1`,
    Headers(headers)
  )
//  implicit val responseDecoder: Decoder[OneFrameResponse] = deriveConfiguredDecoder[OneFrameResponse]
//  implicit val decoder = jsonOf[IO, OneFrameResponse]
//  implicit val x: EntityDecoder[IO, List[OneFramePair]] = null

//  httpClient.expect[OneFrameResponse](request)
//  httpClient.expect[List[OneFramePair]](request)
  httpClient.expect[String](request)

}

val result = example().unsafeRunSync()
decode[List[OneFramePair]](result)
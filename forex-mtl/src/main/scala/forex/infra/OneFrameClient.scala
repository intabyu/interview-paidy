package forex.infra
import cats.effect._
import forex.config.ApplicationConfig
import io.circe.generic.auto._
import org.http4s.circe.jsonOf
import org.http4s.client.blaze._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import org.http4s.circe.CirceEntityDecoder._


case class OneFramePair(from: String, to: String, bid: Float, ask: Float, price: Float, time_stamp: String)

case class OneFrameResponse(pairs: List[OneFramePair])


class OneFrameClient(url: String, token: String)(implicit executionContext: ExecutionContext) {
  // FIXME
  println(url)
  println(token)
  // create IO-specific context from the executionContext
  private implicit val cs = IO.contextShift(executionContext)
//  private implicit val timer = IO.timer(executionContext)
//  private implicit val jsonDecoder = forex.http.jsonDecoder


//  implicit val responseDecoder: Decoder[OneFrameResponse] =
//    deriveConfiguredDecoder[OneFrameResponse]
  implicit val decoder = jsonOf[IO, OneFrameResponse]

  def fetchPairs(): IO[OneFrameResponse] = {
    BlazeClientBuilder[IO](global).resource.use { client =>
//      Stream.eval(client.expect(url)(jsonOf[IO, OneFrameResponse]))
//      Stream.eval(client.expect[OneFrameResponse](url)(jsonOf[IO, OneFrameResponse]))
      client.expect[OneFrameResponse](url)
    }
//    val request = Request[IO](
//      method = Method.GET,
//      uri = uri"https://my-lovely-api.com/",
//      headers = Headers.of(
//        Authorization(Credentials.Token(AuthScheme.Bearer, "open sesame")),
//        Accept(MediaType.application.json),
//      )
//    )

  }

}

object OneFrameClient {
  def apply(config: ApplicationConfig)(implicit executionContext: ExecutionContext): OneFrameClient = {
    new OneFrameClient(url=config.oneFrame.url, token=config.oneFrame.token)
  }
}
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._


import scala.concurrent.ExecutionContext.global
implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

import scala.concurrent.ExecutionContext.global

val app = HttpRoutes.of[IO] {
  case GET -> Root / "hello" / name =>
    Ok(s"Hello, $name.")
}.orNotFound

val server = BlazeServerBuilder[IO](global)
  .bindHttp(8080, "localhost")
  .withHttpApp(app)
  .resource

val fiber = server.use(_ => IO.never).start.unsafeRunSync()

import org.http4s.client.blaze._
import org.http4s.client._
import scala.concurrent.ExecutionContext.global


// ASYNC CLIENT
BlazeClientBuilder[IO](global).resource.use { client =>
  // use `client` here and return an `IO`.
  // the client will be acquired and shut down
  // automatically each time the `IO` is run.
  IO.unit
}

// SYNC CLIENT
import cats.effect.Blocker
import java.util.concurrent._

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

val helloJames = httpClient.expect[String]("http://localhost:8080/hello/James")

import cats._, cats.effect._, cats.implicits._
import org.http4s.Uri

def hello(name: String): IO[String] = {
  val target = uri"http://localhost:8080/hello/" / name
  httpClient.expect[String](target)
}

val people = Vector("Michael", "Jessica", "Ashley", "Christopher")

val greetingList = people.parTraverse(hello)

//println(greetingList.unsafeRunSync())

val greetingsStringEffect = greetingList.map(_.mkString("\n"))

greetingsStringEffect.unsafeRunSync()


import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType

def example(): IO[String] = {
  val target = uri"http://localhost:8081/rates?pair=USDJPY"

  val request = org.http4s.Method.GET(
    target,
    Authorization(Credentials.Token(AuthScheme.Bearer, "token: 10dc303535874aeccc86a8251e6992f5")),
    Accept(MediaType.application.json)
  )


  httpClient.expect[String](request)
}

example().unsafeRunSync()

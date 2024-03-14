package com.rockthejvm.jobsboard

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.effect.IOApp
import cats.*
import cats.effect.IO
import org.http4s.ember.server.EmberServerBuilder

object Application extends IOApp.Simple {
  def healthEndpoint[F[_]: Monad] : HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "health" => Ok("All going great ! ")
    }
  }

  def allRoutes[F[_] : Monad]: HttpRoutes[F] = healthEndpoint[F]

  def routerWithPathPrefixes = Router(
    "/private" -> healthEndpoint[IO]
  ).orNotFound

  override  def run = EmberServerBuilder
    .default[IO]
    .withHttpApp(routerWithPathPrefixes)
    .build
    .use(_ => IO.println("Server ready!") *> IO.never)
}


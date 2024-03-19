package com.rockthejvm.jobsboard

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*

import cats.effect.*
import cats.effect.IO
import cats.effect.IOApp
import cats.*
import cats.implicits.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import com.rockthejvm.jobsboard.config.*
import com.rockthejvm.jobsboard.config.syntax.*
import com.rockthejvm.jobsboard.modules.*
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
    val appResource = for{
      core <- Core[IO]
      httpApi <- HttpApi[IO](core)
      server <- EmberServerBuilder
        .default[IO]
        .withHost(config.host)
        .withPort(config.port)
        .withHttpApp(httpApi.endpoints.orNotFound)
        .build
    }yield server

    appResource.use(_ => IO.println("Server listening") *> IO.never)

    }
}



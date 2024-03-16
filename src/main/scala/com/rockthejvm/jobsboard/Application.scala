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

import com.rockthejvm.jobsboard.config.*
import com.rockthejvm.jobsboard.config.syntax.*
import com.rockthejvm.jobsboard.http.routes.HttpApi
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
object Application extends IOApp.Simple {

  //val configSource = ConfigSource.default.load[EmberConfig]

  override def run = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
         EmberServerBuilder
           .default[IO]
           .withHost(config.host)
           .withPort(config.port)
           .withHttpApp(HttpApi[IO].endpoints.orNotFound)
           .build
           .use(_ => IO.println("Server listening") *> IO.never)
    }
}



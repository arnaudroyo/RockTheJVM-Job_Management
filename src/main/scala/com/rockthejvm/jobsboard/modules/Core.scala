package com.rockthejvm.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import cats.effect.kernel.MonadCancelThrow
import com.rockthejvm.jobsboard.core.{Jobs, LiveJobs}
import cats.effect.*
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.*

final class Core[F[_]] private (val  jobs: Jobs[F])

//postgres -> jobs -> core -> httpApi -> app
object Core {
  def postgresResource[F[_]: Async]: Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver", //TODO move to config
      "jdbc:postgresql://localhost:5432/board", //"jdbc:postgresql:board"
      "docker",
      "docker",
      ec
    )
  } yield xa

  def apply[F[_]: Async]: Resource[F, Core[F]] =
    postgresResource[F]
      .evalMap(postgres => LiveJobs[F](postgres))
      .map(jobs => new Core(jobs))
}
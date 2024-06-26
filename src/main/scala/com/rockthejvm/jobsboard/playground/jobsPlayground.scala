package com.rockthejvm.jobsboard.playground

import cats.effect.*
import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.domain.job.JobInfo
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


import scala.io.StdIn
object jobsPlayground extends IOApp .Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]


  val postgresResource: Resource[IO, HikariTransactor[IO]] = for{
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/board", //"jdbc:postgresql:board"
      "docker",
      "docker",
      ec
    )
  }yield xa

  val jobInfo = JobInfo.minimal(
    company = "Rock The JVM",
    title = "Software Engineer",
    description = "Best job ever",
    externalUrl = "azert.com",
    remote = true,
    location = "Anywhere"
  )
  override def run: IO[Unit] = postgresResource.use { xa =>
    for{
      jobs <- LiveJobs[IO](xa)
      _ <- IO(println("Ready. Create ?")) *> IO(StdIn.readLine)
      id <- jobs.create("azerty@a.com", jobInfo)
      _ <- IO(println("Ready. list ?")) *> IO(StdIn.readLine)
      list <- jobs.all()
      _ <- IO(println(s"All jobs : $list Update ?")) *> IO(StdIn.readLine)
      _ <- jobs.update(id, jobInfo.copy(title = "Software Rockstar"))
      newJob <- jobs.find(id)
      _ <- IO(println(s"New job: $newJob Delete ?")) *> IO(StdIn.readLine)
      _ <- jobs.delete(id)
      listAfter = jobs.all()
      _ <- IO(println(s"Deleted job, New list: $listAfter Over.")) *> IO(StdIn.readLine)
    }yield {}
  }
}

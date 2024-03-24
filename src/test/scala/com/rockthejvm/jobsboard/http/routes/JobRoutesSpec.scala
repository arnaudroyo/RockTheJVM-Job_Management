package com.rockthejvm.jobsboard.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import org.http4s.dsl.*
import org.http4s.implicits.*
import org.http4s.*

import cats.implicits.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec

import com.rockthejvm.jobsboard.fixtures.*
import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.domain.job.*

import java.util.UUID

class JobRoutesSpec
  extends AsyncFreeSpec
  with AsyncIOSpec
  with Matchers
  with Http4sDsl[IO]
  with JobFixture{

  /////////////////////////////////////////////////////////////////
  /// prep
  /////////////////////////////////////////////////////////////////
  val jobs: Jobs[IO] = new Jobs[IO] {
    def create(ownerEmail: String, jobInfo: JobInfo): IO[UUID] =
      IO.pure(NewJobUuid)

    def all(): IO[List[Job]] =
      IO.pure(List(AwesomeJob))

    def find(id: UUID): IO[Option[Job]] =
      if (id == AwesomeJobUuid)
        IO.pure(Some(AwesomeJob))
      else
        IO.pure(None)

    def update(id: UUID, jobInfo: JobInfo): IO[Option[Job]] =
      if(id == AwesomeJobUuid)
        IO.pure(Some(UpdatedAwesomeJob))
      else
        IO.pure(None)

    def delete(id: UUID): IO[Int] =
      if(id == AwesomeJobUuid)
        IO.pure(1)
      else
        IO.pure(0)

  }

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  //this is what we are testing
  val jobRoutes: HttpRoutes[IO] = JobRoutes[IO](jobs).routes

  /////////////////////////////////////////////////////////////////
  /// tests
  /////////////////////////////////////////////////////////////////

  "JobRoutes" - {
    "should return a job with a given id" in {
      //code under test
      for{
        //simulate http request
        response <- jobRoutes.orNotFound.run{
          Request(method = Method.GET, uri=uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        }
        //get an http response
        retrieved <- response.as[Job]
      } yield {
        //make some assertions
        response.status shouldBe Status.Ok
        retrieved shouldBe AwesomeJob
      }
    }

    "should return all the jobs" in {
      //code under test
      for {
        //simulate http request
        response <- jobRoutes.orNotFound.run {
          Request(method = Method.POST, uri = uri"/jobs")
        }
        //get an http response
        retrieved <- response.as[List[Job]]
      } yield {
        //make some assertions
        response.status shouldBe Status.Ok
        retrieved shouldBe List(AwesomeJob)
      }
    }

    "should create a job" in {
      //code under test
      for {
        //simulate http request
        response <- jobRoutes.orNotFound.run {
          Request(method = Method.POST, uri = uri"/jobs/create")
            .withEntity(AwesomeJob.jobInfo)
        }
        //get an http response
        retrieved <- response.as[UUID]
      } yield {
        //make some assertions
        response.status shouldBe Status.Created
        retrieved shouldBe NewJobUuid
      }
    }

    "should only update a job that exists" in {
      //code under test
      for {
        //simulate http request
        responseOk <- jobRoutes.orNotFound.run {
          Request(method = Method.PUT, uri=uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
            .withEntity(UpdatedAwesomeJob.jobInfo)
        }

        responseNOk <- jobRoutes.orNotFound.run {
          Request(method = Method.PUT, uri = uri"/jobs/843df718-ec6e-4d49-9289-000000000000")
            .withEntity(UpdatedAwesomeJob.jobInfo)
        }
        //get an http response
      } yield {
        //make some assertions
        responseOk.status shouldBe Status.Ok
        responseNOk.status shouldBe Status.NotFound
      }
    }

    "should only delete a job that exists" in {
      //code under test
      for {
        //simulate http request
        responseOk <- jobRoutes.orNotFound.run {
          Request(method = Method.DELETE, uri=uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        }
        responseNOk <- jobRoutes.orNotFound.run {
          Request(method = Method.DELETE, uri=uri"/jobs/6ea79557-3112-4c84-a8f5-1d1e2c300948")
        }
        //get an http response
      } yield {
        //make some assertions
        responseOk.status shouldBe Status.Ok
        responseNOk.status shouldBe Status.NotFound
      }
    }
  }

}

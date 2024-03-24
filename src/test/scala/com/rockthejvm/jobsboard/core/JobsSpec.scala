package com.rockthejvm.jobsboard.core

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.rockthejvm.jobsboard.fixtures.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import doobie.util.*
import doobie.postgres.implicits.*
import doobie.implicits.*
class JobsSpec
  extends AsyncFreeSpec
  with AsyncIOSpec
  with Matchers
  with DoobieSpec
  with JobFixture {

  val initScript: String = "sql/jobs.sql"

  "Jobs 'algebra'" - {
    "should return no job if the uuid soes not exist" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          retrieved <- jobs.find(NotFoundJobUuid)
        }yield retrieved

        program.asserting(_ shouldBe None)
      }
    }

    "should return a job by its id" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          retrieved <- jobs.find(AwesomeJobUuid)
        } yield retrieved

        program.asserting(_ shouldBe Some(AwesomeJob))
      }
    }

    "should return all jobs" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          retrieved <- jobs.all()
        } yield retrieved

        program.asserting(_ shouldBe List(AwesomeJob))
      }
    }

    "should create a new job" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          jobId <- jobs.create("daniel@rockthejvm.com", RockTheJvmNewJob)
          maybeJob <- jobs.find(jobId)
        } yield maybeJob

        program.asserting(_.map(_.jobInfo) shouldBe Some(RockTheJvmNewJob))
      }
    }

    "should update a job if exists" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          maybeUpdatedJob <- jobs.update(AwesomeJobUuid, UpdatedAwesomeJob.jobInfo)
        } yield maybeUpdatedJob

        program.asserting(_ shouldBe Some(UpdatedAwesomeJob))
      }
    }

    "should return None when updating a job which does not exist" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          maybeUpdatedJob <- jobs.update(NotFoundJobUuid, UpdatedAwesomeJob.jobInfo)
        } yield maybeUpdatedJob

        program.asserting(_ shouldBe None)
      }
    }

    "should delete a new job if exists" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          nbOfDeletedJob <- jobs.delete(AwesomeJobUuid)
          countOfJob <- sql"SELECT COUNT(*) FROM jobs WHERE id=$AwesomeJobUuid"
            .query[Int]
            .unique
            .transact(xa)
        } yield (nbOfDeletedJob, countOfJob)
        program.asserting{
          case(nbOfDeletedJob, countOfJob) =>
          nbOfDeletedJob shouldBe 1
          countOfJob shouldBe 0
        }
      }
    }

    "should not delete a job which does not exist" in {
      transactor.use { xa =>
        val program = for {
          jobs <- LiveJobs[IO](xa)
          nbOfDeletedJob <- jobs.delete(NotFoundJobUuid)
        } yield nbOfDeletedJob

        program.asserting(_ shouldBe 0)
      }
    }

  }

}

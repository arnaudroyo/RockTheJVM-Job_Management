package com.rockthejvm.jobsboard.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*

import com.rockthejvm.jobsboard.http.responses.*
import com.rockthejvm.jobsboard.domain.job.*
import cats.effect.*
import cats.implicits.*
import com.rockthejvm.jobsboard.core.Jobs
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import org.typelevel.log4cats.Logger
import com.rockthejvm.jobsboard.logging.syntax.* //log specifique créé

import com.rockthejvm.jobsboard.http.validation.syntax.*


import java.util.UUID
import scala.collection.mutable
class JobRoutes[F[_] : Concurrent: Logger] (jobs: Jobs[F]) extends HttpValidationDsl[F]{

  // POST /jobs?offset=x&limit=y { filters } //TODO add query parmas and filters
  private val allJobRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case POST -> Root  =>
        for{
          jobsList <- jobs.all()
          resp <- Ok(jobsList)
        }yield resp

  }

  //GET /job/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      jobs.find(id).flatMap{
        case Some(job) => Ok(job)
        case None => NotFound(FailureResponse(s"Job $id not found."))
      }
  }

  //POST /jobs {jobInfo}
  private val createJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] { case req@POST -> Root / "create" =>
    req.validate[JobInfo] { jobInfo =>
      for {
        _ <- Logger[F].info("Trying to add job..") //log classique
        jobId <- jobs.create("TODO@gmail.com", jobInfo)
        _ <- Logger[F].info(s"Pass creation of job: $jobId")
        resp <- Created(jobId)
      } yield resp
    }
  }

  //PUT /jobs/uuid {jobInfo}
  private val updateJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      req.validate[JobInfo] { jobInfo =>
        for {
          _ <- Logger[F].info(s"Trying to update job $id") //log classique
          maybeNewjob <- jobs.update(id, jobInfo)
          _ <- Logger[F].info(s"Job update passed newtitle= ${maybeNewjob}") //log classique
          resp <- maybeNewjob match {
            case Some(job) => Ok()
            case None => NotFound(FailureResponse(s"Cannot update job $id not found."))
          }
        } yield resp
      }

  }

  //DELETE /jobs/uuid {jobInfo}
  private val deleteJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      jobs.find(id).flatMap {
        case Some(job) =>
          for {
            _ <- Logger[F].info(s"Trying to delete job $id..") //log classique
            _ <- jobs.delete(id).pure[F]
            resp <- Ok(s"Deleted job $id")
            _ <- Logger[F].info(s"Deleted job $id..")
          }yield resp

        case None => NotFound(FailureResponse(s"Cannot delete job $id not found."))
      }  }

  val routes = Router(
    "/jobs" -> (allJobRoutes <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object  JobRoutes {
  def apply[F[_]: Concurrent: Logger](jobs: Jobs[F]) = new JobRoutes[F](jobs)
}

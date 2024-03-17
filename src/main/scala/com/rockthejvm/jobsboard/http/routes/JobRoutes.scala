package com.rockthejvm.jobsboard.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import com.rockthejvm.jobsboard.http.responses.*
import com.rockthejvm.jobsboard.domain.job.*
import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import org.typelevel.log4cats.Logger

import java.util.UUID
import scala.collection.mutable
class JobRoutes[F[_] : Concurrent: Logger] extends Http4sDsl[F] {

  private val database = mutable.Map[UUID, Job]()


  // POST /jobs?offset=x&limit=y { filters } //TODO add query parmas and filters
  private val allJobRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case POST -> Root  =>
        Ok(database.values)
  }

  //GET /job/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      database.get(id) match{
        case Some(job) => Ok(job)
        case None => NotFound(FailureResponse(s"Job $id not found."))
      }
  }

  //POST /jobs {jobInfo}
  private def createJob(jobInfo: JobInfo): F[Job] = {
    Job(
      id = UUID.randomUUID(),
      date = System.currentTimeMillis(),
      ownerEmail = "ryarnaud@mail.com",
      jobInfo = jobInfo,
      active = true
    ).pure[F]
  }

  import com.rockthejvm.jobsboard.logging.syntax.* //log specifique créé

  private val createJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] { case req @ POST -> Root / "create" =>
    for {
      _ <- Logger[F].info("Trying to add job..") //log classique
      jobInfo <- req.as[JobInfo].logError(e => s"parsing payload failed: $e") // log spécifique
      _ <- Logger[F].info(s"Parse job info: $jobInfo")
      job <-  createJob(jobInfo)
      _ <- database.put(job.id, job).pure[F]
      _ <- Logger[F].info(s"Created job: $job")

      resp <- Created(job.id)
    }yield resp
  }

  //PUT /jobs/uuid {jobInfo}
  private val updateJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(job) =>
          for {
            jobInfo <- req.as[JobInfo]
            _ <- database.put(id, job.copy(jobInfo = jobInfo)).pure[F]
            resp <- Created(job.id)
          }yield resp

        case None => NotFound(FailureResponse(s"Cannot update job $id not found."))
      }
  }

  //DELETE /jobs/uuid {jobInfo}
  private val deleteJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(job) =>
          for {
            _ <- database.remove(id).pure[F]
            resp <- Ok()
          }yield resp

        case None => NotFound(FailureResponse(s"Cannot delete job $id not found."))
      }  }

  val routes = Router(
    "/jobs" -> (allJobRoutes <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object  JobRoutes {
  def apply[F[_]: Concurrent: Logger] = new JobRoutes[F]
}

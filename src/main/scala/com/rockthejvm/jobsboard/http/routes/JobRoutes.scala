package com.rockthejvm.jobsboard.http.routes

import cats.*
import cats.implicits.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
class JobRoutes[F[_] : Monad] extends Http4sDsl[F] {

  // POST /jobs?offset=x&limit=y { filters } //TODO add query parmas and filters
  private val allJobRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case POST -> Root  =>
        Ok("todo")
  }

  //GET /job/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      Ok(s"todo find at $id")
  }

  //POST /jobs {jobInfo}
  private val createJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] {
    case POST -> Root / "create" =>
    Ok("todo")
  }

  //PUT /jobs/uuid {jobInfo}
  private val updateJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] {
    case PUT -> Root / UUIDVar(id) =>
      Ok(s"todo update at $id")
  }

  //DELETE /jobs/uuid {jobInfo}
  private val deleteJobRoute: HttpRoutes[F] =
  HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
    Ok(s"todo delete at $id")
  }

  val routes = Router(
    "/jobs" -> (allJobRoutes <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object  JobRoutes {
  def apply[F[_]: Monad] = new JobRoutes[F]
}

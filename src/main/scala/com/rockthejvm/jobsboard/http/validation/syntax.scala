package com.rockthejvm.jobsboard.http.validation


import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.dsl.*
import cats.*
import cats.implicits.*
import cats.data.*
import cats.data.Validated.*
import com.rockthejvm.jobsboard.http.responses.FailureResponse
import org.typelevel.log4cats.Logger
import validators.*
import com.rockthejvm.jobsboard.logging.syntax.*
object syntax {

  def validateEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A]=
    validator.validate(entity)

  trait HttpValidationDsl[F[_]: MonadThrow: Logger] extends Http4sDsl[F]{

  }

  extension [F[_]: MonadThrow: Logger](req: Request[F])
    def validate[A: Validator](serverLogicIfValid: A => F[Response[F]])(using
       EntityDecoder[F, A]
    ): F[Response[F]] =
      req
        .as[A]
        .logError(e => s"Parsing payload failed: $e")
        .map(validateEntity) //F[ValidationResult[A]]
        .flatMap{
          case Valid(entity) =>
            serverLogicIfValid(entity) //F[Response[F]]
          case Invalid(err) =>
            BadRequest(FailureResponse(err.toList.map(_.errorMessage).mkString(", ")))
        }
}

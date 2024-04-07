package com.rockthejvm.jobsboard.core

import cats.effect.*
import doobie.implicits.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.rockthejvm.jobsboard.fixtures.*
import com.rockthejvm.jobsboard.domain.user.*
import org.postgresql.util.PSQLException
import org.scalatest.Inside

class UsersSpec
  extends AsyncFreeSpec
  with AsyncIOSpec
  with Matchers
  with Inside
  with DoobieSpec
  with UserFixture
  {
    override val initScript: String= "sql/users.sql"

    given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

    "Users 'algebra'" - {
      "should return a user by email" in {
        transactor.use { xa =>
          val program = for {
            users <- LiveUsers[IO](xa)
            retrieved <- users.find("riccardo@rockthejvm.com")
          } yield (retrieved)

          program.asserting(_ shouldBe Some(Riccardo))
        }
      }

      "should return none if email does not exists" in {
        transactor.use { xa =>
          val program = for {
            users <- LiveUsers[IO](xa)
            retrieved <- users.find("notfound@rockthejvm.com")
          } yield (retrieved)

          program.asserting(_ shouldBe None)
        }
      }

      "should create a new user" in {
        transactor.use { xa =>
          val program = for {
            users <- LiveUsers[IO](xa)
            userId <- users.create(NewUser)
            maybeUser <- sql"SELECT * FROM users WHERE email = ${NewUser.email}"
            .query[User]
            .option
            .transact(xa)
          } yield (userId, maybeUser)

          program.asserting { case (userId, maybeUser) =>
              userId shouldBe NewUser.email
              maybeUser shouldBe Some(NewUser)
          }
        }
      }


      "should fail creating a new user if the email already exists" in {
        transactor.use { xa =>
          val program = for {
            users <- LiveUsers[IO](xa)
            userId <- users.create(Daniel).attempt //IO[Either[Thowable, String]]
          } yield (userId)

          program.asserting { outcome =>
            inside(outcome) {
              case Left(e) => e shouldBe a[PSQLException]
              case _ => fail()
            }
          }
        }
      }

      "should return none when updated a user that does not exists" in {
        transactor.use { xa =>
          val program = for {
            users <- LiveUsers[IO](xa)
            maybeUser <- users.update(NewUser)
          } yield (maybeUser)

          program.asserting(_ shouldBe None)
        }
      }

      "should update an existing user" in {
        transactor.use { xa =>
          val program = for {
            users <- LiveUsers[IO](xa)
            maybeUser <- users.update(UpdatedRiccardo)
          } yield (maybeUser)

          program.asserting(_ shouldBe Some(UpdatedRiccardo))
        }
      }

      "should delete an user" in {
        transactor.use { xa =>
          val program = for {
            users <- LiveUsers[IO](xa)
            res <- users.delete("daniel@rockthejvm.com")
            maybeBeUser <- sql"SELECT * FROM users WHERE email = 'daniel@rockthejvm.com'"
              .query[User]
              .option
              .transact(xa)
          } yield (res, maybeBeUser)

          program.asserting { case (result, maybeUser) =>
            result shouldBe true
            maybeUser shouldBe None
          }

        }
      }


      "should not delete an user that does not exists" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          res <- users.delete("notfound@rockthejvm.com")
        } yield (res)

        program.asserting(_ shouldBe false)

      }
    }

    }
  }

package services


import akka.http.scaladsl.model.StatusCodes
import json.{LoginRequest, Response, ResponseError, ResponseSuccess, UserRequest, UserResponse}
import messages.ErrorMessage
import models.User
import repositories.UserRepository

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import utils.{JwtUtils, PasswordUtils}

class UserService(userRepository: UserRepository, passwordUtils: PasswordUtils, jwtUtils: JwtUtils) extends BaseService[UserRequest]{


  override def validate(data: UserRequest): List[String] = List[Option[String]](
    data.username match {
      case Some(username) => username.isEmpty match {
        case true=>Some(ErrorMessage.USER_NAME_IS_REQUIRED)
        case _ =>None
      }
      case _ => Some(ErrorMessage.USER_NAME_IS_REQUIRED)
    },
    data.password match {
      case Some(password) => password.isEmpty match {
        case true => Some(ErrorMessage.PASSWORD_IS_REQUIRED)
        case _ => None
      }
      case _ => Some(ErrorMessage.PASSWORD_IS_REQUIRED)
    },
    data.first_name match {
      case Some(first_name) => first_name.isEmpty match {
        case true=>Some(ErrorMessage.FIRST_NAME_IS_REQUIRED)
        case _ => None
      }
      case _ => Some(ErrorMessage.FIRST_NAME_IS_REQUIRED)
    },
    data.last_name match {
      case Some(last_name)=>last_name.isEmpty match {
        case true=> Some(ErrorMessage.LAST_NAME_IS_REQUIRED)
        case _ => None
      }
      case _ => Some(ErrorMessage.LAST_NAME_IS_REQUIRED)
    },
    data.role match {
      case Some(role)=>role.isEmpty match {
        case true => Some(ErrorMessage.ROLE_IS_REQUIRED)
        case _ => None
      }
      case _ => Some(ErrorMessage.ROLE_IS_REQUIRED)
    }
  ).flatten

  def validate(data:LoginRequest):List[String]= List[Option[String]](
    data.username match {
      case Some(username) => username.isEmpty match {
        case true => Some(ErrorMessage.USER_NAME_IS_REQUIRED)
        case _ => None
      }
      case _ => Some(ErrorMessage.USER_NAME_IS_REQUIRED)
    },
    data.password match {
      case Some(password) => password.isEmpty match {
        case true => Some(ErrorMessage.PASSWORD_IS_REQUIRED)
        case _ => None
      }
      case _ => Some(ErrorMessage.PASSWORD_IS_REQUIRED)
    }
  ).flatten

  def signUp(data: UserRequest): Future[Either[ResponseError[String],ResponseSuccess[UserResponse]]]= {
    val errors = validate(data)
    errors.size > 0 match {
      case true => Future.successful(Left(ResponseError[String](errors.mkString(","), StatusCodes.BadRequest.intValue)))
      case _ => {

        val user = User(0, data.username.get, passwordUtils.hash(data.password.get), data.first_name.get, data.last_name.get, data.role.get)
        for {
          userDb <- userRepository.findByUserName(user.username)
          result <- userDb match {
            case Some(_) => Future.successful(ResponseError[String](ErrorMessage.USER_NOT_EXISTS, StatusCodes.BadRequest.intValue))
            case _ => userRepository.add(user) map {
              case user =>{
                val token = jwtUtils.encode(user.username)
                ResponseSuccess[UserResponse](UserResponse(user.id, user.username, user.password, user.first_name, user.last_name, user.role, token), StatusCodes.Created.intValue)
              }
              case _ => ResponseError[Object](ErrorMessage.DATABASE_ERROR, StatusCodes.InternalServerError.intValue)
            }
          }
        } yield result match {
          case x:ResponseSuccess[UserResponse]=>Right(x)
          case x:ResponseError[String]=>Left(x)
        }
      }


    }
  }

  def signIn(data: LoginRequest): Future[Either[ResponseError[String],ResponseSuccess[UserResponse]]] = {
    val errors = validate(data)
    errors.size > 0 match {
      case true => Future.successful(Left(ResponseError[String](errors.mkString(","), StatusCodes.BadRequest.intValue)))
      case _ => {
        for {
          userDb <- userRepository.findByUserName(data.username.get)
          canLogin <- userDb match {
            case Some(userDb) => Future.successful(passwordUtils.isHash(data.password.get, userDb.password))
            case _ => Future.successful(false)
          }
          result <- canLogin match {
            case true => {
              val token = jwtUtils.encode(userDb.get.username)
              Future.successful(ResponseSuccess[UserResponse](UserResponse(userDb.get.id, userDb.get.username, userDb.get.password, userDb.get.first_name, userDb.get.last_name, userDb.get.role, token), StatusCodes.OK.intValue))
            }
            case _ =>
              Future.successful(ResponseError[String](ErrorMessage.LOGIN_FAIL, StatusCodes.Unauthorized.intValue))
          }
        } yield result match {
          case x:ResponseSuccess[UserResponse]=>Right(x)
          case x:ResponseError[String]=>Left(x)
        }
      }
    }
  }

}

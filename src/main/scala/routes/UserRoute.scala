package routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import json.JsonFormat._
import json.{LoginRequest, ResponseError, ResponseSuccess, UserRequest, UserResponse}
import services.UserService

import scala.util.{Failure, Success}

class UserRoute(userService:UserService)(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {

  def route:Route={
    pathPrefix("signup"){
      pathEnd{
        post{
          entity(as[UserRequest]){
            request=>{

              onComplete(userService.signUp(request)){
                case Success(result) => result match {
                  case Right(r) => complete(r.code, r)
                  case Left(l) => complete(l.code, l)
                }
                case Failure(ex)=>complete(StatusCodes.InternalServerError.intValue,ResponseError[String](ex.getMessage,StatusCodes.InternalServerError.intValue))
              }
            }

          }

        }
      }
    } ~
    pathPrefix("login"){
      pathEnd{
        post{
          entity(as[LoginRequest]){
            request=>{
              onComplete(userService.signIn(request)){
                case Success(result) => result match {
                  case Right(r) => complete(r.code, r)
                  case Left(l) => complete(l.code, l)
                }
                case Failure(ex)=>complete(StatusCodes.InternalServerError.intValue,ResponseError[String](ex.getMessage,StatusCodes.InternalServerError.intValue))
              }
            }
          }
        }
      }
    }
  }
}

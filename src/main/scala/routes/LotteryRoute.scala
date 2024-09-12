package routes
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import json.JsonFormat._
import json.{LotteryRequest, _}
import services.TicketService
import utils.JwtUtils

import scala.util.{Failure, Success}
class LotteryRoute(ticketService: TicketService, jwtUtils: JwtUtils)(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {
  def route: Route = {
    pathPrefix("lotteries") {
      pathEnd {
        post {
          (headerValueByName("Authorization")) {
            header => {
              jwtUtils.decode(header) match {
                case Success(auth) => {
                  entity(as[LotteryRequest]) {
                    request =>
                      onComplete(ticketService.saveTicket(request, auth.content)) {
                        case Success(result) => result match {
                          case Right(r) => complete(r.code, r)
                          case Left(l) => complete(l.code, l)
                        }
                        case Failure(ex) => complete(StatusCodes.InternalServerError.intValue, ResponseError[String](ex.getMessage, StatusCodes.InternalServerError.intValue))
                      }
                  }
                }
                case Failure(ex) => complete(StatusCodes.InternalServerError.intValue, ResponseError[String](ex.getMessage, StatusCodes.InternalServerError.intValue))
              }

            }
          }
        } ~
          get {
            (headerValueByName("Authorization")) {
              header => {
                jwtUtils.decode(header) match {
                  case Success(auth) => {
                    entity(as[LotteryRequest]) {
                      request =>
                        onComplete(ticketService.findAllTicket(auth.content)) {
                          case Success(result) => result match {
                            case Right(r) => complete(r.code, r)
                            case Left(l) => complete(l.code, l)
                          }
                          case Failure(ex) => complete(StatusCodes.InternalServerError.intValue, ResponseError[String](ex.getMessage, StatusCodes.InternalServerError.intValue))
                        }
                    }
                  }
                  case Failure(ex) => complete(StatusCodes.InternalServerError.intValue, ResponseError[String](ex.getMessage, StatusCodes.InternalServerError.intValue))
                }

              }
            }
          }
      }
    }

  }
}

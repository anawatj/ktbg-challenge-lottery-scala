package services

import akka.http.scaladsl.model.StatusCodes
import json.{LotteryRequest, LotteryResponse, TicketRequest, Response, ResponseError, ResponseSuccess}
import models.{Lottery, UserTicket}
import repositories.{LotteryRepository, UserRepository, UserTicketRepository}
import utils.JwtUtils

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future

class TicketService(userRepository: UserRepository,lotteryRepository: LotteryRepository ,userTicketRepository: UserTicketRepository,jwtUtils: JwtUtils) extends BaseService[LotteryRequest]{

  override def validate(data: LotteryRequest): List[String] =
    List[Option[String]](
      data.ticket match {
        case Some(_)=>None
        case _ => Some("Ticket is required")
      },
      data.amount match {
        case Some(_)=>None
        case _ => Some("Amount is required")
      },
      data.price match {
        case Some(_)=>None
        case _ => Some("Price is required")
      }
    ).flatten

  def validate(data:TicketRequest):List[String]=
   List[Option[String]](
      data.ticket match {
        case Some(_)=>None
        case _ => Some("Ticket is required")
      }
    ).flatten

  def findAllTicket(username:String):Future[Either[ResponseError[String],ResponseSuccess[Array[String]]]] ={

    for{
      user <-  userRepository.findByUserName(username)
      result <- user match {
        case Some(_)=>
              lotteryRepository.findAll() map{
                case lotteries =>
                  lotteries.size == 0 match {
                    case true=>ResponseError[String]("Lottery Not Found",StatusCodes.NotFound.intValue)
                    case false=>ResponseSuccess[Array[String]](lotteries.map(lottery=>lottery.ticket).toArray,StatusCodes.OK.intValue)
                  }
                case _ =>ResponseError[String]("Database Error",StatusCodes.InternalServerError.intValue)
              }
        case _ => Future.successful(ResponseError[String]("Unauthorized",StatusCodes.Unauthorized.intValue))
      }
    } yield  result match {
      case x:ResponseSuccess[Array[String]]=>Right(x)
      case x:ResponseError[String]=>Left(x)
    }
  }
  def saveTicket(data:LotteryRequest,username:String):Future[Either[ResponseError[String],ResponseSuccess[String]]]={
      for {
        user<-userRepository.findByUserName(username)
        errors<-Future.successful(validate(data))
        isAdmin <- user match {
          case Some(user) => Future.successful(user.role.equals("ADMIN"))
          case _ => Future.successful(false)
        }
        result <- (isAdmin,errors.size==0) match {
          case (true,true)=>{
            val lottery=Lottery(data.ticket.get,data.price.get,data.amount.get)
            for {
              lotteryDb<-lotteryRepository.find(lottery.ticket)
              r <- lotteryDb match {
                case Some(lotteryDb)=>lotteryRepository.update(lottery,lotteryDb.ticket) map {
                  case lottery=>ResponseSuccess[String](lottery.ticket,StatusCodes.Created.intValue)
                  case _ => ResponseError[String]("Database Error",StatusCodes.InternalServerError.intValue)
                }
                case None=>lotteryRepository.add(lottery) map {
                  case lottery=>ResponseSuccess[String](lottery.ticket,StatusCodes.Created.intValue)
                  case _ => ResponseError[String]("Database Error",StatusCodes.InternalServerError.intValue)
                }
              }
            } yield r
          }
          case (false,_)=>Future.successful(ResponseError[String]("User is Unauthorized",StatusCodes.Unauthorized.intValue))
          case (_,false)=>Future.successful(ResponseError[String](errors.mkString(","),StatusCodes.BadRequest.intValue))
          case _ => Future.successful(ResponseError[String]("Something went wrong",StatusCodes.InternalServerError.intValue))

        }


      } yield result match {
        case x:ResponseSuccess[String]=>Right(x)
        case x:ResponseError[String]=>Left(x)
      }

  }

  def purchaseTicket(data:TicketRequest, username:String):Future[Either[ResponseError[String],ResponseSuccess[String]]]={
      for {
        user <- userRepository.findByUserName(username)
        errors<-Future.successful(validate(data))
        result<-(user.nonEmpty,errors.size==0) match {
          case (true,true)=>{
            val userTicket = UserTicket(0,user.get.id,data.ticket.get)
            for {
              lottery <- lotteryRepository.find(userTicket.ticket)
              r <- lottery match {
                case Some(lottery)=>lottery.amount > 0 match {
                  case true=>
                    for{
                      userTicketDb <- userTicketRepository.findByPK(userTicket.user_id,userTicket.ticket)
                      rr <- userTicketDb match {
                        case Some(_)=>Future.successful(ResponseError[String]("Cannot Purchase Ticket",StatusCodes.BadRequest.intValue))
                        case _ =>
                          for {
                            userTicket <- userTicketRepository.add(userTicket)
                            _ <- lotteryRepository.update(Lottery(lottery.ticket, lottery.price, lottery.amount - 1), lottery.ticket)
                          } yield userTicket match {
                            case userTicket => ResponseSuccess[String](userTicket.ticket, StatusCodes.Created.intValue)
                            case _ => ResponseError[String]("Database Error", StatusCodes.InternalServerError.intValue)
                          }

                      }

                    } yield rr
                  case _ => Future.successful(ResponseError[String]("This Ticket is Full stocked",StatusCodes.BadRequest.intValue))
                }
                case _ => Future.successful(ResponseError[String]("Lottery Not Found",StatusCodes.BadRequest.intValue))
              }
            } yield r
          }
          case (false,_)=>Future.successful(ResponseError[String]("Unauthorized",StatusCodes.Unauthorized.intValue))
          case (_,false)=>Future.successful(ResponseError[String](errors.mkString(","),StatusCodes.BadRequest.intValue))
          case _ => Future.successful(ResponseError[String]("Something went wrong",StatusCodes.InternalServerError.intValue))
        }
      } yield result match {
        case x:ResponseSuccess[String]=>Right(x)
        case x:ResponseError[String]=>Left(x)
      }
  }

  def listOfPurchaseLottery(username:String):Future[Either[ResponseError[String],ResponseSuccess[Array[String]]]]={
    for{
      user<-userRepository.findByUserName(username)
      result<-user match {
        case Some(user)=>userTicketRepository.findByUser(user.id) map {
          case tickets=>tickets.size>0 match {
            case true=>ResponseSuccess[Array[String]](tickets.map(ticket=>ticket.ticket).toArray,StatusCodes.OK.intValue)
            case false => ResponseError[String]("Not Found Ticket",StatusCodes.NotFound.intValue)
          }
          case _ => ResponseError[String]("Database Error",StatusCodes.InternalServerError.intValue)
        }
        case None => Future.successful(ResponseError[String]("Unauthorized",StatusCodes.Unauthorized.intValue))
      }
    } yield result match {
      case x:ResponseSuccess[Array[String]]=>Right(x)
      case x:ResponseError[String]=>Left(x)
    }
  }

  def returnLottery(data:TicketRequest,username:String):Future[Either[ResponseError[String],ResponseSuccess[String]]]={
      for{
        user<-userRepository.findByUserName(username)
        errors<-Future.successful(validate(data))
        result<-(user.nonEmpty,errors.size==0) match {
          case(true,true)=>{
            val userTicket = UserTicket(0,user.get.id,data.ticket.get)
            for{
              lottery<-lotteryRepository.find(userTicket.ticket)
              r<-lottery match {
                case  Some(lottery)=>
                  for{
                    userTicketDb <-  userTicketRepository.findByPK(userTicket.user_id,userTicket.ticket)
                    rr <- userTicketDb match {
                      case  Some(userTicketDb)=>
                          for {
                            deleteSuccess<-userTicketRepository.delete(userTicketDb.id)
                            _ <- lotteryRepository.update(Lottery(lottery.ticket,lottery.price,lottery.amount+1),lottery.ticket)
                          } yield deleteSuccess match {
                            case deleteSuccess => ResponseSuccess[String]("Success", StatusCodes.OK.intValue)
                            case _ => ResponseError[String]("Database Error", StatusCodes.InternalServerError.intValue)
                          }

                      case None=>Future.successful(ResponseError[String]("UserTicket Does Not Found",StatusCodes.BadRequest.intValue))
                    }
                  } yield rr
                case _ => Future.successful(ResponseError[String]("Lottery Does Not Found",StatusCodes.BadRequest.intValue))
              }
            } yield r

          }
          case (false,_)=>Future.successful(ResponseError[String]("Unauthorized",StatusCodes.Unauthorized.intValue))
          case (_,false)=>Future.successful(ResponseError[String](errors.mkString(","),StatusCodes.BadRequest.intValue))
          case _ => Future.successful(ResponseError[String]("Something went wrong",StatusCodes.InternalServerError.intValue))
        }
      } yield result match {
        case x:ResponseSuccess[String]=>Right(x)
        case x:ResponseError[String]=>Left(x)
      }
  }

}

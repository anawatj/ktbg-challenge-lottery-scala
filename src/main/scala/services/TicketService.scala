package services

import akka.http.scaladsl.model.StatusCodes
import json.{LotteryRequest, LotteryResponse, Response, ResponseError, ResponseSuccess, TicketRequest}
import messages.{ErrorMessage, Role}
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
        case _ => Some(ErrorMessage.TICKET_IS_REQUIRED)
      },
      data.amount match {
        case Some(_)=>None
        case _ => Some(ErrorMessage.AMOUNT_IS_REQUIRED)
      },
      data.price match {
        case Some(_)=>None
        case _ => Some(ErrorMessage.PRICE_IS_REQUIRED)
      }
    ).flatten

  def validate(data:TicketRequest):List[String]=
   List[Option[String]](
      data.ticket match {
        case Some(_)=>None
        case _ => Some(ErrorMessage.TICKET_IS_REQUIRED)
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
                    case true=>ResponseError[String](ErrorMessage.LOTTERY_NOT_FOUND,StatusCodes.NotFound.intValue)
                    case false=>ResponseSuccess[Array[String]](lotteries.map(lottery=>lottery.ticket).toArray,StatusCodes.OK.intValue)
                  }
                case _ =>ResponseError[String](ErrorMessage.DATABASE_ERROR,StatusCodes.InternalServerError.intValue)
              }
        case _ => Future.successful(ResponseError[String](ErrorMessage.UN_AUTHORIZE,StatusCodes.Unauthorized.intValue))
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
          case Some(user) => Future.successful(user.role.equals(Role.ADMIN))
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
                  case _ => ResponseError[String](ErrorMessage.DATABASE_ERROR,StatusCodes.InternalServerError.intValue)
                }
                case None=>lotteryRepository.add(lottery) map {
                  case lottery=>ResponseSuccess[String](lottery.ticket,StatusCodes.Created.intValue)
                  case _ => ResponseError[String](ErrorMessage.UN_AUTHORIZE,StatusCodes.InternalServerError.intValue)
                }
              }
            } yield r
          }
          case (false,_)=>Future.successful(ResponseError[String](ErrorMessage.UN_AUTHORIZE,StatusCodes.Unauthorized.intValue))
          case (_,false)=>Future.successful(ResponseError[String](errors.mkString(","),StatusCodes.BadRequest.intValue))
          case _ => Future.successful(ResponseError[String](ErrorMessage.SOME_THING_WENT_WRONG,StatusCodes.InternalServerError.intValue))

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
                        case Some(_)=>Future.successful(ResponseError[String](ErrorMessage.CANNOT_PURCHASE_TICKET,StatusCodes.BadRequest.intValue))
                        case _ =>
                          for {
                            userTicket <- userTicketRepository.add(userTicket)
                            _ <- lotteryRepository.update(Lottery(lottery.ticket, lottery.price, lottery.amount - 1), lottery.ticket)
                          } yield userTicket match {
                            case userTicket => ResponseSuccess[String](userTicket.ticket, StatusCodes.Created.intValue)
                            case _ => ResponseError[String](ErrorMessage.DATABASE_ERROR, StatusCodes.InternalServerError.intValue)
                          }

                      }

                    } yield rr
                  case _ => Future.successful(ResponseError[String](ErrorMessage.CANNOT_PURCHASE_TICKET,StatusCodes.BadRequest.intValue))
                }
                case _ => Future.successful(ResponseError[String](ErrorMessage.LOTTERY_NOT_FOUND,StatusCodes.BadRequest.intValue))
              }
            } yield r
          }
          case (false,_)=>Future.successful(ResponseError[String](ErrorMessage.UN_AUTHORIZE,StatusCodes.Unauthorized.intValue))
          case (_,false)=>Future.successful(ResponseError[String](errors.mkString(","),StatusCodes.BadRequest.intValue))
          case _ => Future.successful(ResponseError[String](ErrorMessage.SOME_THING_WENT_WRONG,StatusCodes.InternalServerError.intValue))
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
            case false => ResponseError[String](ErrorMessage.TICKET_NOT_FOUND,StatusCodes.NotFound.intValue)
          }
          case _ => ResponseError[String](ErrorMessage.DATABASE_ERROR,StatusCodes.InternalServerError.intValue)
        }
        case None => Future.successful(ResponseError[String](ErrorMessage.UN_AUTHORIZE,StatusCodes.Unauthorized.intValue))
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
                            case deleteSuccess => ResponseSuccess[String](ErrorMessage.SUCCESS, StatusCodes.OK.intValue)
                            case _ => ResponseError[String](ErrorMessage.DATABASE_ERROR, StatusCodes.InternalServerError.intValue)
                          }

                      case None=>Future.successful(ResponseError[String](ErrorMessage.TICKET_NOT_FOUND,StatusCodes.BadRequest.intValue))
                    }
                  } yield rr
                case _ => Future.successful(ResponseError[String](ErrorMessage.LOTTERY_NOT_FOUND,StatusCodes.BadRequest.intValue))
              }
            } yield r

          }
          case (false,_)=>Future.successful(ResponseError[String](ErrorMessage.UN_AUTHORIZE,StatusCodes.Unauthorized.intValue))
          case (_,false)=>Future.successful(ResponseError[String](errors.mkString(","),StatusCodes.BadRequest.intValue))
          case _ => Future.successful(ResponseError[String](ErrorMessage.SOME_THING_WENT_WRONG,StatusCodes.InternalServerError.intValue))
        }
      } yield result match {
        case x:ResponseSuccess[String]=>Right(x)
        case x:ResponseError[String]=>Left(x)
      }
  }

}

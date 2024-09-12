package json
import spray.json._
import DefaultJsonProtocol._
object JsonFormat {
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
  implicit val userRequestFormat = jsonFormat5(UserRequest)
  implicit val lotteryRequestFormat = jsonFormat3(LotteryRequest)
  implicit val ticketRequestFormat = jsonFormat1(TicketRequest)

  implicit val userResponseFormat = jsonFormat7(UserResponse)
  implicit val lotteryResponseFormat = jsonFormat3(LotteryResponse)

  implicit  val successResponseStringFormat = jsonFormat2(ResponseSuccess[String])
  implicit  val successResponseArrayStringFormat = jsonFormat2(ResponseSuccess[Array[String]])

  implicit val  userResponseSuccessFormat = jsonFormat2(ResponseSuccess[UserResponse])

  implicit val  lotteryResponseSuccessFormat = jsonFormat2(ResponseSuccess[LotteryResponse])


  implicit val errorResponseStringFormat = jsonFormat2(ResponseError[String])

  implicit val errorResponseArrayStringFormat = jsonFormat2(ResponseError[Array[String]])








}

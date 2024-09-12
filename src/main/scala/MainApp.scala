import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContextExecutor
import repositories.{LotteryRepositoryImpl, UserRepositoryImpl, UserTicketRepositoryImpl}
import routes.{LotteryRoute, TicketRoute, UserRoute}
import services.{TicketService, UserService}
import utils.{JwtUtils, PasswordUtils}

import scala.io.StdIn

object MainApp {


  implicit val system = ActorSystem("Lottery-System")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val actorMaterializer = ActorMaterializer()

  def main(args: Array[String]) = {

    val userRepository = new UserRepositoryImpl
    val ticketRepository = new UserTicketRepositoryImpl
    val lotteryRepository = new LotteryRepositoryImpl
    val passwordUtil = new PasswordUtils
    val jwtUtil = new JwtUtils
    val userService = new UserService(userRepository, passwordUtil, jwtUtil)
    val ticketService = new TicketService(userRepository, lotteryRepository, ticketRepository, jwtUtil)
    val userRoute = new UserRoute(userService)
    val lotteryRoute = new LotteryRoute(ticketService, jwtUtil)
    val ticketRoute = new TicketRoute(ticketService, jwtUtil)


    val route = pathPrefix("api" / "v1") {
      userRoute.route ~
        lotteryRoute.route ~
        ticketRoute.route

    }
    val (host, port) = ("0.0.0.0", 8888)

    val serverBinder = Http().newServerAt(host, port).bindFlow(route)
    StdIn.readLine()
    serverBinder
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }


}


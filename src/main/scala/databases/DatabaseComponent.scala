package databases
import slick.jdbc.JdbcProfile
trait DatabaseComponent {

  val profile : JdbcProfile
}
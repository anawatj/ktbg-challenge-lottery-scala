package databases
import slick.jdbc.PostgresProfile
trait PostgresComponent extends DatabaseComponent  {

  override val profile =PostgresProfile

  val db = profile.backend.Database.forConfig("postgres")

}
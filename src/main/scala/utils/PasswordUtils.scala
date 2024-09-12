package utils
import com.github.t3hnar.bcrypt._

class PasswordUtils {
  def hash(password:String):String =password.bcrypt
  def isHash(password:String,hashPassword:String)=password.isBcrypted(hashPassword)
}

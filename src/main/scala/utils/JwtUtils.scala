package utils

import pdi.jwt.{Jwt, JwtClaim}

import scala.util.Try

class JwtUtils {
  def encode(value:String): String ={
    Jwt.encode(value)
  }
  def decode(token:String) :Try[JwtClaim]= {
    Jwt.decode(token)
  }
}
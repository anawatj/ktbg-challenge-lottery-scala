package json


trait Response{
  val code:Int
}

case class ResponseSuccess[T](data:T,code:Int) extends Response
case class ResponseError[T](errors:T,code:Int) extends Response


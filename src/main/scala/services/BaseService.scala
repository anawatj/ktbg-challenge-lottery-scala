package services

trait BaseService[T] {

  def validate(data:T):List[String]

}

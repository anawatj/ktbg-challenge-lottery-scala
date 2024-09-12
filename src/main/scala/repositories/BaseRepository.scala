package repositories

import scala.concurrent.Future

trait BaseRepository[T,E] {

  def add(data:T):Future[T]
  def update(data:T,id:E ):Future[T]
  def delete(id:E):Future[Unit]
  def find(id:E):Future[Option[T]]
  def bulkAdd(list:List[T]):Future[List[T]]
  def findAll():Future[List[T]]
}

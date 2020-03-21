package throttling

sealed trait Key

case class HttpKey(ip:String) extends Key

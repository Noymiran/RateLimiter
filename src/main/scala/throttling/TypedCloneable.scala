package throttling

trait TypedCloneable[SelfType <: Object] extends scala.Cloneable {
  self: SelfType =>
  override final def clone(): SelfType = super.clone().asInstanceOf[SelfType]
}
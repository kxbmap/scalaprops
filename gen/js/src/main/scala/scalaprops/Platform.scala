package scalaprops

private[scalaprops] object Platform {
  def minSuccessful = 50
  def randFromLong(seed: Long) = TinyMT32.getDefault(seed)
  def randFromInt(seed: Int) = TinyMT32.getDefault(seed)
  def genSize = 50
  def className[A](clazz: Class[A]) = clazz.getSimpleName
  def charCount(codePoint: Int) = if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) 1 else 2
}

package forex.services.rates.interpreters.oneframe

trait Client {
  def fetchPairs(): Either[String, List[Pair]]
}
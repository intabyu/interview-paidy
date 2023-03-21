package forex.services.rates.interpreters

trait OneFrameClient {
  def fetchPairs(): Either[String, List[OneFramePair]]
}
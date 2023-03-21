package forex.services.rates.interpreters.oneframe

case class Pair(from: String, to: String, bid: BigDecimal, ask: BigDecimal, price: BigDecimal, time_stamp: String)

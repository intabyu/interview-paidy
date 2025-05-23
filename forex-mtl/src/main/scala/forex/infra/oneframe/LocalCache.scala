package forex.infra.oneframe

import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.infra.logger.ForexLogger
import forex.services.rates.errors
import forex.services.rates.interpreters.oneframe.{Cache, Pair}

import scala.collection.immutable.HashMap

class LocalCache extends Cache {
  // here we could use a 'val mutable.Hashmap' or a 'var Hashmap'
  // using var would facilitate ensuring to only have latest values
  private var cache: Map[Rate.Pair, Rate] = HashMap.empty
  
  override def update(pairs: List[Pair]): Unit = {
    ForexLogger.get.debug("updating cache...")
    cache = pairs.map { p =>
      val rate = Rate(
        pair = Rate.Pair(from = Currency.fromString(p.from), to = Currency.fromString(p.to)),
        price = Price(p.price),
        timestamp = Timestamp(p.time_stamp)
      )
      (rate.pair, rate)
    }.toMap
    ForexLogger.get.debug("...cache updated")
  }

  override def get(pair: Rate.Pair): Either[errors.Error, Rate] =
    cache.get(pair) match {
      case None        => Left(errors.Error.OneFrameLookupFailed(s"Could not find: $pair"))
      case Some(value) => Right(value)
    }
}

package forex.services.rates.interpreters.oneframe

import forex.domain.Rate
import forex.services.rates.errors.Error

trait Cache {
  def update(pairs: List[Pair]): Unit

  def get(pair: Rate.Pair): Either[Error, Rate]
}

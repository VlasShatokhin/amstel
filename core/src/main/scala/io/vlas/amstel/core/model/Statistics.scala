package io.vlas.amstel.core.model

case class Statistics(sum: Double,
                      avg: Double,
                      max: Double,
                      min: Double,
                      count: Long) {

  def + (other: Statistics): Statistics = {
    val countCombined = count + other.count
    val sumCombined = sum + other.sum
    Statistics(
      sum = sumCombined,
      avg = sumCombined / countCombined,
      max = Math.max(max, other.max),
      min = Math.min(min, other.min),
      count = countCombined)
  }
}

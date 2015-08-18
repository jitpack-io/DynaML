package io.github.mandar2812.dynaml.evaluation

import breeze.linalg.DenseVector
import org.apache.spark.rdd.RDD

/**
 * Abstract trait for metrics
 */
trait Metrics[Q, P] {
  protected val scoresAndLabels: List[(Q, P)]
  def print(): Unit
  def generatePlots(): Unit = {}
  def kpi(): DenseVector[Double]
}

object Metrics{
  def apply(task: String)
           (scoresAndLabels: List[(Double, Double)], length: Int)
  : Metrics[Double, Double] = task match {
    case "regression" => new RegressionMetrics(scoresAndLabels, length)
    case "classification" => new BinaryClassificationMetrics(scoresAndLabels, length)
  }
}

object MetricsSpark {
  def apply(task: String)
           (scoresAndLabels: RDD[(Double, Double)],
            length: Long,
            minmax: (Double, Double))
  : Metrics[Double, Double] = task match {
    case "regression" => new RegressionMetricsSpark(scoresAndLabels, length)
    case "classification" => new BinaryClassificationMetricsSpark(scoresAndLabels, length, minmax)
  }
}

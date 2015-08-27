package io.github.mandar2812.dynaml.evaluation

import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD

/**
 * Calculate performance of multiple binary
 * classifiers
 */
abstract class BinaryClassificationTurbo(
  protected val scores: RDD[(Seq[Double], Double)],
  val len: Long, minmax: Seq[(Double, Double)])
  extends Metrics[Seq[Double], Double] {


  private val logger = Logger.getLogger(this.getClass)

  override protected val scoresAndLabels = List()

  private val thresholds = minmax.map{m => {
    List.tabulate(100)(i => {
       m._1 +
        i.toDouble*((m._2.toInt -
          m._1.toInt + 1)/100.0)})
  }}

  private var num_positives = 0.0
  private var num_negatives = 0.0
  private var tpfpList: List[List[(Double, (Double, Double))]] = List()

  private def areasUnderCurves(lpoints: List[List[(Double, Double)]]): List[Double] =
    lpoints.map((point) => {
      BinaryClassificationMetrics.areaUnderCurve(point)
    })

  def tpfpByThresholds(): List[List[(Double, (Double, Double))]] = {
    val positives = scores.context.accumulator(0.0, "positives")
    val negatives = scores.context.accumulator(0.0, "negatives")
    val ths = scores.context.broadcast(thresholds.head.length)
    val thres = scores.context.broadcast(thresholds)

    List()
  }

}

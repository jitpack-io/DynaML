package io.github.mandar2812.dynaml.models.svm

import breeze.linalg.{DenseVector, DenseMatrix}
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.frames.FramedGraph
import io.github.mandar2812.dynaml.evaluation.Metrics
import io.github.mandar2812.dynaml.graphutils.CausalEdge
import io.github.mandar2812.dynaml.kernels.SVMKernel
import io.github.mandar2812.dynaml.optimization.ConjugateGradient

import scala.collection.mutable

class SSDLSSVMModel(override protected val g: FramedGraph[Graph],
                    override protected val nPoints: Long,
                    override protected val featuredims: Int,
                    override protected val vertexMaps: (mutable.HashMap[String, AnyRef],
                      mutable.HashMap[Long, AnyRef],
                      mutable.HashMap[Long, AnyRef]),
                    override protected val edgeMaps: (mutable.HashMap[Long, AnyRef],
                      mutable.HashMap[Long, AnyRef]),
                    override implicit protected val task: String)
  extends LSSVMModel(g, nPoints, featuredims, vertexMaps, edgeMaps, task) {

  var kernel :SVMKernel[DenseMatrix[Double]] = null

  var (feature_a, b): (DenseMatrix[Double], DenseVector[Double]) = (null, null)

  override def applyKernel(kern: SVMKernel[DenseMatrix[Double]],
                           M: Int = this.points.length):Unit = {
    kernel = kern
  }

  override def applyFeatureMap: Unit = {}

  override def learn(): Unit = {
    this.params = optimizer.optimize(nPoints,
      this.filterXYEdges((p) => this.points.contains(p)).toIterable,
      this.params)
  }

  override def evaluateFold(params: DenseVector[Double])
                           (test_data_set: Iterable[CausalEdge])
                           (task: String): Metrics[Double, Double] = {
    var index: Int = 1
    val prototypes = this.filterFeatures(p => this.points.contains(p))
    val scorepred: (DenseVector[Double]) => Double =
      x => params dot DenseVector(prototypes.map(p => this.kernel.evaluate(p, x)), Array(1.0))

    val scoresAndLabels = test_data_set.map((e) => {

      val x = DenseVector(e.getPoint().getFeatureMap())
      val y = e.getLabel().getValue()
      index += 1
      (scorepred(x), y)
    })
    Metrics(task)(scoresAndLabels.toList, index)
  }

  override def crossvalidate(folds: Int = 10, reg: Double = 0.001,
                             optionalStateFlag: Boolean = false): (Double, Double, Double) = {
    //Create the folds as lists of integers
    //which index the data points

    (feature_a, b) = SSDLSSVMModel.getFeatureMatrix(points.length.toLong, kernel,
      this.filterXYEdges((p) => this.points.contains(p)), this.initParams(),
      1.0, reg)

    this.optimizer.setRegParam(reg).setNumIterations(this.params.length)
      .setStepSize(0.001).setMiniBatchFraction(1.0)

    val params = ConjugateGradient.runCG(feature_a, b,
      this.initParams(), 0.0001,
      this.params.length)
    val metrics =
      this.evaluateFold(params)(this.filterXYEdges((p) => !this.points.contains(p)))(this.task)
    val ans = metrics.kpi()
    (ans(0), ans(1), ans(2))
  }
}

object SSDLSSVMModel {

  def getFeatureMatrix(nPoints: Long,
                       kernel: SVMKernel[DenseMatrix[Double]],
                       ParamOutEdges: Iterable[CausalEdge],
                       initialP: DenseVector[Double],
                       frac: Double, regParam: Double) = {

    val kernelmat = kernel.buildKernelMatrix(
      ParamOutEdges.map(p =>
        DenseVector(p.getPoint().getFeatureMap())).toList,
      nPoints.toInt).getKernelMatrix()

    val smoother = DenseMatrix.eye[Double](nPoints.toInt)/regParam

    val ones = DenseMatrix.fill[Double](1,nPoints.toInt)(1.0)
    val y = DenseVector(ParamOutEdges.map(p => p.getLabel().getValue()))
    /**
     * A = [K + I/reg]|[1]
     *     [1.t]      |[0]
     * */
    val A = DenseMatrix.horzcat(
      DenseMatrix.vertcat(kernelmat + smoother, ones),
      DenseMatrix.vertcat(ones.t, DenseMatrix(0))
    )

    val b = DenseVector.vertcat(y, DenseVector(0))
    (A,b)
  }

  def apply(implicit config: Map[String, String]): SSDLSSVMModel = {
    LSSVMModel(config).asInstanceOf[SSDLSSVMModel]
  }

}
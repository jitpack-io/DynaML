package io.github.mandar2812.dynaml.models.neuralnets

import breeze.linalg.DenseVector
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.frames.FramedGraph

/**
 * Represents the template of a Feed Forward Neural Network
 * backed by an underlying graph.
 */
abstract class BasicFeedForwardNetwork[G]
  extends NeuralNetwork[G, FramedGraph[Graph]]{

  val feedForward = BasicFeedForwardNetwork.feedForwardFunc(params) _

}

object BasicFeedForwardNetwork {
  def feedForwardFunc(networkGraph: FramedGraph[Graph])
                     (inputPattern: DenseVector[Double]): Unit = {}

  def initializeWeights(hiddenLayers: Int,
                        inputDimensions: Int,
                        outputDimensions: Int,
                        neuronCounts: List[Int]) = {}

}
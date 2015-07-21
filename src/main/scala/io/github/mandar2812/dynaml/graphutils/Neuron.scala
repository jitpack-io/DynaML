package io.github.mandar2812.dynaml.graphutils

import com.tinkerpop.frames.{Property, VertexFrame}

/**
 * Outline of a Neuron
 */
trait Neuron extends VertexFrame {
  @Property("localfield")
  def getLocalField(): Double

  @Property("activationFunc")
  def getActivationFunc(): String

  @Property("layer")
  def getLayer(): Int

}

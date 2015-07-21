package io.github.mandar2812.dynaml.graphutils

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.frames.Incidence

/**
 * Models the characteristics of a hidden layer neuron
 * (must contain both incoming & outgoing synapses)
 */
trait HiddenNeuron extends Neuron with OutputNeuron {
  @Incidence(label = "synapse", direction = Direction.OUT)
  def getOutgoingSynapses(): java.lang.Iterable[Synapse]

}

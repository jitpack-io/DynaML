package io.github.mandar2812.dynaml.graphutils

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.frames.Incidence

/**
 * Models the characteristics of a neuron
 * capable of producing output
 * (must contain some incoming synapses)
 */
trait OutputNeuron extends Neuron {
  @Incidence(label = "synapse", direction = Direction.IN)
  def getIncomingSynapses(): java.lang.Iterable[Synapse]

}

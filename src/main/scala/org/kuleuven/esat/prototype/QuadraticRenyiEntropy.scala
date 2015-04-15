/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuleuven.esat.prototype

import breeze.linalg.DenseVector
import org.kuleuven.esat.kernels.DensityKernel

/**
 * Implements the quadratic Renyi Entropy
 */
class QuadraticRenyiEntropy(dist: DensityKernel)
  extends EntropyMeasure
  with Serializable {

  val log_e = scala.math.log _
  val sqrt = scala.math.sqrt _
  override protected val density: DensityKernel = dist

  /**
   * Calculate the quadratic Renyi entropy
   * within a distribution specific
   * proportionality constant. This can
   * be used to compare the entropy values of
   * different sets of data on the same
   * distribution.
   *
   * @param data The data set whose entropy is
   *             required.
   * @return The entropy of the dataset assuming
   *         it is distributed as given by the value
   *         parameter 'density'.
   * */

  override def entropy(data: List[DenseVector[Double]]): Double = {
    val dim = data(0).length
    val root_two: breeze.linalg.Vector[Double] = DenseVector.fill(dim, sqrt(2))
    val product = for(i <- data.view; j <- data.view) yield (i, j)
    -1*log_e(product.map((couple) =>
      density.eval((couple._1 - couple._2) :/ root_two)).sum)
  }
}
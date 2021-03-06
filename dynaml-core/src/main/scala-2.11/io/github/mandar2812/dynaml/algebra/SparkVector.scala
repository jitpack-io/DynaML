/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
* */
package io.github.mandar2812.dynaml.algebra

import breeze.linalg.NumericOps
import org.apache.spark.rdd.RDD

import scala.collection.immutable.NumericRange

/**
  * @author mandar2812 date: 28/09/2016.
  *
  * A distributed vector backed by a spark [[RDD]]
  */
class SparkVector(baseVector: RDD[(Long, Double)])
  extends SparkMatrix(baseVector.map(c => ((c._1, 1L), c._2)))
    with NumericOps[SparkVector] {

  def _baseVector = baseVector

  override lazy val cols = 1L

  override def repr: SparkVector = this

  override def t: DualSparkVector = new DualSparkVector(baseVector)

  def apply(r: NumericRange[Long]): SparkVector =
    new SparkVector(_baseVector.filterByRange(r.min, r.max))


}

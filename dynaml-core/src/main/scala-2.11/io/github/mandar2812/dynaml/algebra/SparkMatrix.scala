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
  *  @author mandar2812 date: 28/09/2016
  *
  *  A distributed matrix backed by a spark [[RDD]]
  *
  */
class SparkMatrix(baseMatrix: RDD[((Long, Long), Double)]) extends NumericOps[SparkMatrix] {

  lazy val rows = baseMatrix.map(_._1._1).max() + 1L

  lazy val cols = baseMatrix.map(_._1._2).max() + 1L

  protected val matrix: RDD[((Long, Long), Double)] = baseMatrix

  override def repr: SparkMatrix = this

  /**
    * @return The backing [[RDD]]
    */
  def _matrix = matrix

  /**
    * Obtain transpose of the matrix.
    *
    */
  def t: SparkMatrix = new SparkMatrix(this.baseMatrix.map(c => ((c._1._2, c._1._1), c._2)))

  /**
    * Get a sub-matrix based on a range of rows and columns
    *
    */
  def apply(r: NumericRange[Long], c: NumericRange[Long]): SparkMatrix =
    new SparkMatrix(matrix.filterByRange((r.min, c.min), (r.max, c.max)))

}

/**
  * @author mandar2812 date: 09/10/2016
  *
  * A distributed square matrix backed by a spark [[RDD]]
  *
  */
class SparkSquareMatrix(baseMatrix: RDD[((Long, Long), Double)]) extends SparkMatrix(baseMatrix) {

  assert(rows == cols, "For a square matrix, rows must be equal to columns")

  /**
    * Extract diagonal elements into a new [[SparkSquareMatrix]]
    *
    */
  def diag: SparkSquareMatrix = new SparkSquareMatrix(
    baseMatrix.map(c => if(c._1._1 == c._1._2) c else (c._1, 0.0))
  )

  /**
    * Extract lower triangular elements into a new [[SparkSquareMatrix]]
    */
  def LowerTri: SparkSquareMatrix = new SparkSquareMatrix(
    baseMatrix.map(c => if(c._1._1 <= c._1._2) c else (c._1, 0.0))
  )

}
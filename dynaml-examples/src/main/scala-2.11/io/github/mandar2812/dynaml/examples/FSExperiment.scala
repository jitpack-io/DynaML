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
package io.github.mandar2812.dynaml.examples

import java.io.File

import breeze.linalg.DenseVector
import com.github.tototoshi.csv.CSVWriter
import io.github.mandar2812.dynaml.utils

import scala.collection.mutable.{MutableList => ML}

/**
 * @author mandar2812 on 3/8/15.
 * Carry out experiment with the FS-LSSVM
 * on three possible data sets.
 * 1. Forest Cover type : Class 2 vs rest
 * 2. Adult Data set
 * 3. Magic Gamma Telescope Data set.
 *
 * Conduct a number of trials and average out
 * the performance results and store it in a file
 */
object FSExperiment {
  def apply(nCores: Int = 4, trials: Int,
            data: String = "ForestCover",
            root: String = "data/",
            factor: Int = 2,
            kernels: List[String] = List("RationalQuadratic", "Cauchy",
              "Laplacian", "RBF", "Linear")): Unit = {
    val writer = CSVWriter.open(new File(root+data+"Res.csv"), append = true)

    def Experiment(kern: String, prototypes: Int = 100):Unit = {

      List("gs", "csa").foreach((globalOpt) => {

        List((2, 0.55), (3, 0.45)).foreach((gridSize) => {
          val perfs: ML[DenseVector[Double]] = ML()
          var times: ML[Double] = ML()
          (1 to trials).toList.foreach((trial) => {
            data match {
              case "Delve" => {
                val t0 = System.currentTimeMillis().toDouble/1000.0
                perfs += TestDelve(nCores,
                  prototypes, kern, globalOpt,
                  grid = gridSize._1, step = gridSize._2,
                  frac = 1.0, logscale = true, paraFactor = factor)
                times += System.currentTimeMillis().toDouble/1000.0 - t0
              }

              case "ForestCover" => {
                val t0 = System.currentTimeMillis().toDouble/1000.0
                perfs += TestForestCover(nCores,
                  prototypes, kern, globalOpt,
                  grid = gridSize._1, step = gridSize._2,
                  frac = 1.0, dataRoot = root, local = true,
                  logscale = true, paraFactor = factor)
                times += System.currentTimeMillis().toDouble/1000.0 - t0
              }
              case "HiggsSUSY" => {
                val t0 = System.currentTimeMillis().toDouble/1000.0
                perfs += TestSUSY(nCores,
                  prototypes, kern, globalOpt,
                  grid = gridSize._1, step = gridSize._2,
                  frac = 1.0, dataRoot = root, local = true,
                  logscale = true, paraFactor = factor)
                times += System.currentTimeMillis().toDouble/1000.0 - t0
              }
              case "Adult" => {
                val t0 = System.currentTimeMillis().toDouble/1000.0
                perfs += TestAdult(nCores, prototypes, kern,
                  globalOpt, grid = gridSize._1, step = gridSize._2,
                  frac = 1.0, logscale = true, paraFactor = factor)
                times += System.currentTimeMillis().toDouble/1000.0 - t0
              }
              case "MagicGamma" => {
                val t0 = System.currentTimeMillis().toDouble/1000.0
                perfs += TestMagicGamma(nCores,
                  prototypes, kern,
                  globalOpt, grid = gridSize._1, step = gridSize._2,
                  dataRoot = root, logscale = true, paraFactor = factor)
                times += System.currentTimeMillis().toDouble/1000.0 - t0
              }
            }
          })
          val (avg_perf, var_perf) = utils.getStats(perfs.toList)
          val (avg_time, var_time) = utils.getStats(times.toList.map(DenseVector(_)))
          val row = Seq(kern, prototypes.toString, globalOpt,
            gridSize._1.toString, gridSize._2.toString, "log",
            avg_perf(0), math.sqrt(var_perf(0)/(trials.toDouble-1.0)),
            avg_perf(1), math.sqrt(var_perf(1)/(trials.toDouble-1.0)),
            avg_perf(2), math.sqrt(var_perf(2)/(trials.toDouble-1.0)),
            avg_time(0), math.sqrt(var_time(0)/(trials.toDouble-1.0)))
          writer.writeRow(row)
        })
      })
    }


    kernels.foreach((kern) => {
      if(kern == "Linear") {
        Experiment("Linear")
      } else {
        List(50, 100, 200).foreach((prototypes) => {
          Experiment(kern, prototypes)
        })
      }

    })
    writer.close()
  }
}

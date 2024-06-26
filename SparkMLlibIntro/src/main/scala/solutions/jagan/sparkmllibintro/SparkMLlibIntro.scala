package solutions.jagan.sparkmllibintro

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD

case class Flower(species: String)

// the following code is based on this article: https://www.baeldung.com/spark-mlib-machine-learning
object SparkMLlibIntro {
  val master = "local[2]"
  val objectName: String = this.getClass.getSimpleName.stripSuffix("$")
  val conf: SparkConf = new SparkConf().setAppName(objectName).setMaster(master)
  val sc: SparkContext = new SparkContext(conf)
  val inputFilename = "src/main/resources/iris/iris.data"

  private def readData(fileName: String) = {

    val data = sc.textFile(fileName)

    println("Raw input data:\n")
    data.collect().foreach(println)
    println

    data.collect().map {
      case line: String if line.split(",").length > 1 => {
        val splitLine = line.split(",")
        val inputElements = List(splitLine(0), splitLine(1), splitLine(2), splitLine(3)).map { elem => elem.toDouble }
        val species = Flower(splitLine(4))
        val speciesAsNumber: Int = species match {
          case Flower("Iris-setosa") => 0
          case Flower("Iris-versicolor") => 1
          case Flower("Iris-virginica") => 2
          case _ => -1
        }
        Vectors.dense(inputElements.toArray :+ speciesAsNumber.toDouble)
      }
    }
  }

  private def printlLabeledData(points: RDD[LabeledPoint]): Unit = {

    println("Labeled input data:\n")
    points.collect().foreach(println)

  }

  def main(args: Array[String]): Unit = {

    println(s"\nJAG: $objectName\n")

    val rows = readData(inputFilename)
    performExploratoryDataAnalysis(rows)

    val labeledData = sc.parallelize {
      rows.map { row =>
        LabeledPoint(row(4), Vectors.dense(row.toArray.slice(0, 4)))
      }
    }
    printlLabeledData(labeledData)

    println
    sc.stop()
  }

  private def performExploratoryDataAnalysis(data: Array[Vector]): Unit = {

    val summary = Statistics.colStats(sc.parallelize(data))

    println("Summary mean:")
    println(summary.mean)
    println("Summary variance:")
    println(summary.variance)
    println("Summary non-zero:")
    println(summary.numNonzeros)

  }

}

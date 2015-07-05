package com.snailteam.simple_project

import java.util.Date
import java.sql.DriverManager
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.collection.mutable.Map
import org.apache.spark.SparkContext
import org.apache.spark.rdd.JdbcRDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import com.huaban.analysis.jieba.JiebaSegmenter
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import scala.collection.JavaConversions
import org.apache.spark.{ SparkContext, SparkConf }
import org.apache.spark.rdd.RDD
import scopt.OptionParser
import java.io.{ File, PrintWriter }
import com.huaban.analysis.jieba.SegToken
import org.apache.spark.mllib.linalg.Vectors
import com.huaban.analysis.jieba.Word


object Jieba_Seg {
  
  private case class Params(
      stopwordFile: String = "",
      //outputPath: String = "",
      path: Seq[String] = Seq.empty
      )
      
  def main(args: Array[String]): Unit = {
    if(args.length < 2){
      println("Usage:Jieba_Seg inputPath(File) outputPath(Directory)")
      sys.exit(1)
    }
    val defaultParams = Params()
    val parser = new OptionParser[Params]("Jieba_Seg") {
      head("JieBaSeg: Seg text via JiaBa.")
      opt[String]("stopwordFile")
        .text(s"filepath for a list of stopwords. Note: This must fit on a single machine." +
        s"  default: ${defaultParams.stopwordFile}")
        .action((x, c) => c.copy(stopwordFile = x))
      //opt[String]("outputPath")
      //  .text(s"output path(directories) to store the segmentation result." +
      //  s"  default: ${defaultParams.outputPath}")
      //  .action((x, c) => c.copy(outputPath = x))
      arg[String]("<Path>...")
        .text("input paths (directories) and output paths." +
        "  Each text file line should hold 1 document.")
        .unbounded()
        .required()
        .action((x, c) => c.copy(path = c.path :+ x))   
      
    }
    parser.parse(args, defaultParams).map { params =>
      run(params)
    }.getOrElse {
      parser.showUsageAsError
      sys.exit(1)
    }
  }
  private def run(params: Params) {
    val conf = new SparkConf().setAppName("Segment").set("spark.driver.maxResultSize", "40G")
    //println(conf.get("spark.driver.maxResultSize"));
    //println(conf.get("spark.akka.frameSize"));
    val sc = new SparkContext(conf)
    val seg = new JiebaSegmenter
    val stopwordMap = Map[String, Int]()
    try{
      for (line <- Source.fromFile(params.stopwordFile).getLines)
        stopwordMap += (line -> 1)  
    }catch {
      case e: Exception => println("Warning:There is no stopwordFile")
    }
    //println(stopwordMap)
    val textRDD: RDD[String] = sc.textFile(params.path(0))
    
    //val values = textRDD.foreach(line => seg.process(line, SegMode.SEARCH))
    
    try {
            var list: RDD[String] = textRDD.map {
                case (line) =>
                    var tuples = seg.process(line, SegMode.SEARCH)
                    var str: String = " "
                    var seg_it = tuples.iterator()
                    while (seg_it.hasNext()) {
                        var s: String= seg_it.next().word.getToken 
                        if(!stopwordMap.contains(s))
                            str += s + " "
                    }
                    Vectors.+(str)
            }
            println("list size: " + list.count)

            var datas:List[String] = List()
            list.collect.foreach(str => datas = str.substring(str.indexOf(" ") + 1).trim():: datas)
            list.cache();
            println("datas: " + datas.size)
            var result = sc.parallelize(datas, 10000)
            result.saveAsTextFile(params.path(1))
        } catch {
            case e: Exception => println(e)
        }
        
    sc.stop()
  }
}

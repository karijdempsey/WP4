import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object AISframe
{
 	   

	def main(args: Array[String])
	{
		val hdfsprefix = "hdfs://namenode.ib.sandbox.ichec.ie:8020/" 
		val tfiles = hdfsprefix + args(0)
		val locdatafile = hdfsprefix + args(1)
		val outputfile = hdfsprefix + args(2)
		
		//val tfiles = "hdfs://namenode.ib.sandbox.ichec.ie:8020/user/tessadew/defframe6all.csv"
		//val locdatafile = "hdfs://namenode.ib.sandbox.ichec.ie:8020/datasets/AIS/Locations/2015120100*.csv.gz"
		//val outputfile = "hdfs://namenode.ib.sandbox.ichec.ie:8020/user/tessadew/AMS1.csv"


		val conf = new SparkConf()
		conf.setAppName("AIS-frame")
		conf.setMaster("yarn-client")
		val sc = new SparkContext(conf)

		val data = sc.textFile(tfiles)
			.map(_.split(","))
			//.filter(x => x(2)=="1")
		
		//val locdata = sc.textFile(locdatafile)
		//	.map(_.split(",")).map("%.3g" format _)
		//	.filter(x=> x(0)!="mmsi")
		val locdata = sc.textFile(locdatafile).map(_.split(",")).filter(x=> x(0)!="mmsi" && x(1).toDouble>=4.7298 && x(1).toDouble<=4.8814  && x(2).toDouble>=52.3878  && x(2).toDouble<=52.4406 )


		val single_mmsi = data.map(x => (x(0), Array(x(1), x(2)).mkString(",")))
		val loc_orig = locdata.map(x => (x(0), Array(x(0), x(1), x(2), x(4), x(8)).mkString(",")))
		//val loc_orig = locdata.map(x => (x(0), Array(x(0), x(8)., x(1), x(2), x(4)).mkString(",")))
					   
		val couples = loc_orig.join(single_mmsi);
		//val ship = 
		

					   
		//val alldata = loc_orig ++ single_mmsi;
		//val result = alldata.filterByKey(x => "1" in x);


        //val koppel = imommsi.map(z => (z.mkString(","), 1)).reduceByKey(_+_);
        //val koppel2 = koppel.map(b => b._1.split(",")++Array(b._2)).map(c=>(c(0),c.slice(1,3)));
        //val max_mmsi = koppel2.reduceByKey((b, c)=> if(b(1).toString.toInt>c(1).toString.toInt) b else c).cache;

        //val aantalmmsi = max_mmsi.map(z => (z._1.toString.toInt,1)).reduceByKey(_+_).filter(x=>x._2 > 1).cache;
        //aantalmmsi.map(a=> Array(a._1,a._2).mkString(",")).saveAsTextFile(outputfile);
        //println ("dubbele mmsi = " + aantalmmsi.count);
        //val filt_max_mmsi = max_mmsi.filter(y=>checkimo(y._2(0).toString));
        couples.map(a=> a._2).saveAsTextFile(outputfile);

		sc.stop()
	}
}

   

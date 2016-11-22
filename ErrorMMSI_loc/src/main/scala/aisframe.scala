
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.sql.Timestamp;
import java.text._;
import java.util.Date;

object AISframe
{
 	   

	def main(args: Array[String])
	{
		val hdfsprefix = "hdfs://namenode.ib.sandbox.ichec.ie:8020/" 
		val tfiles = hdfsprefix + args(0)
		//val locharbdata = hdfsprefix + args(1)
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
		
		//val LocHarb = io.Source.fromFile("ports_locations.csv").getLines.map(_.split(",")).toArray
		val LocHarb = sc.textFile("hdfs://namenode.ib.sandbox.ichec.ie:8020/user/tessadew/ports_locations.csv").map(_.split(",")).collect()
		val brLocHarb = sc.broadcast(LocHarb)
		
		
		//val locdata = sc.textFile(locdatafile)
		//	.map(_.split(",")).map("%.3g" format _)
		//	.filter(x=> x(0)!="mmsi")
		def findHarbour(lat: Double, lon: Double): String =
		{
  			val x = brLocHarb.value
    				.filter(x=>x(1).toDouble<lat&x(2).toDouble>lat&x(3).toDouble<lon&x(4).toDouble>lon).map(x=>x(0))
  			return (if (x.length==0) "SEA" else x(0))
		}
		
		val locdata = sc.textFile(locdatafile).map(_.split(","))
			.filter(x=> x(0)!="mmsi")
			.mapPartitions{it => 
				       val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				       it.map(x=>x++Array(findHarbour(x(1).toDouble,x(2).toDouble),df.parse(x(8)).getTime/1000))
			}
		
		//locdata.map(a=> a.mkString(",")).saveAsTextFile(outputfile);
			     
		
		val shipframe = data.map(x => (x(0), Array(x(1), x(2)).mkString(",")))
		val ship_orig = locdata.map(x=>(x(0), Array(x(1),x(2),x(4),x(8),x(9),x(10))))
			.groupByKey()
			.map(x=>(x._1,x._2.toList.sortWith((a,b)=>a(5).toLong<b(5).toLong)))

		//val enters = ship_orig.flatMap(x=>(x._2.map(y=>y(4)).toArray.sliding(2).filter(x(0)!=x(1) && x(1)!="SEA" ).map(x=>x(1)),1)).reduceByKey
		val enters = ship_orig.flatMap(x=>x._2.map(y=>y(4)).toArray.sliding(2).toArray.filter(x=>x.length>1).map(x=>((x(0),x(1)),1))).filter(x._1._1=="SEA" && x._1._2=="SEA").reduceByKey(_+_)
		enters.map(a=> Array(a._1._1, a._1._2,a._2).mkString(",")).saveAsTextFile(outputfile);
		//val couples = loc_orig.join(shipframe);
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
        
		//couples.map(a=> a._2).saveAsTextFile(outputfile);

		sc.stop()
	}
}

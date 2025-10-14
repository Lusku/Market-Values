package marketValues.app

import org.apache.spark.sql.SparkSession
import marketValues.ui.UserInterface

object MainApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Mercado Finanzas App")
      .master("local[*]")
      .getOrCreate()

    val ui = new UserInterface(spark)
    ui.start()

    spark.stop()
  }
}

package marketValues.data

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import java.time.LocalDate
import org.knowm.xchart.{XYChartBuilder, SwingWrapper}
import org.apache.poi.ss.usermodel._
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.{FileOutputStream, File}

object DataProcessor {

  def processAndDisplay(data: Map[String, Any], title: String)(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    val today = LocalDate.now().toString
    val timeSeriesKey = data.keys.find(_.contains("Time Series")).getOrElse("")
    val timeSeriesData = data.get(timeSeriesKey).collect {
      case m: Map[String, Map[String, String]] @unchecked => m
    }.getOrElse(Map())

    val rows = timeSeriesData.toSeq.map { case (timestamp, values) =>
      (
        timestamp,
        values.getOrElse("1. open", "0").toDouble,
        values.getOrElse("2. high", "0").toDouble,
        values.getOrElse("3. low", "0").toDouble,
        values.getOrElse("4. close", "0").toDouble,
        values.getOrElse("5. volume", "0").toDouble
      )
    }

    val df = rows.toDF("FechaHora", "Apertura", "Máximo", "Mínimo", "Cierre", "Volumen")
      .orderBy($"FechaHora".desc)

    println(s"\n$title")
    df.show(truncate = false)
  }

  def saveToExcel(data: Map[String, Any], symbol: String, today: Boolean): Unit = {
    val workbook = new XSSFWorkbook()
    val sheet = workbook.createSheet("Datos")

    val header = sheet.createRow(0)
    val headers = Array("FechaHora", "Apertura", "Máximo", "Mínimo", "Cierre", "Volumen")
    headers.zipWithIndex.foreach { case (name, idx) => header.createCell(idx).setCellValue(name) }

    val rows = data.keys.find(_.contains("Time Series")).flatMap { key =>
      data.get(key).collect {
        case m: Map[String, Map[String, String]] @unchecked => m
      }
    }.getOrElse(Map())

    rows.toSeq.sortBy(_._1).zipWithIndex.foreach { case ((timestamp, values), rowIdx) =>
      val row = sheet.createRow(rowIdx + 1)
      row.createCell(0).setCellValue(timestamp)
      row.createCell(1).setCellValue(values.getOrElse("1. open", "0").toDouble)
      row.createCell(2).setCellValue(values.getOrElse("2. high", "0").toDouble)
      row.createCell(3).setCellValue(values.getOrElse("3. low", "0").toDouble)
      row.createCell(4).setCellValue(values.getOrElse("4. close", "0").toDouble)
      row.createCell(5).setCellValue(values.getOrElse("5. volume", "0").toDouble)
    }

    val file = new File(s"$symbol-${if (today) "Hoy" else "Historico"}.xlsx")
    val fos = new FileOutputStream(file)
    workbook.write(fos)
    fos.close()
    workbook.close()

    println(s"📊 Datos guardados en ${file.getAbsolutePath}")
  }

  def generateChart(data: Map[String, Any], symbol: String, today: Boolean): Unit = {
    val rows = data.keys.find(_.contains("Time Series")).flatMap { key =>
      data.get(key).collect {
        case m: Map[String, Map[String, String]] @unchecked => m
      }
    }.getOrElse(Map())

    val timestamps = rows.keys.toSeq.sorted
    val closePrices = timestamps.map(ts => rows(ts).getOrElse("4. close", "0").toDouble)

    val chart = new XYChartBuilder()
      .width(800)
      .height(600)
      .title(s"Precio de Cierre de $symbol")
      .xAxisTitle("Tiempo")
      .yAxisTitle("Precio")
      .build()

    chart.addSeries("Cierre", timestamps.zipWithIndex.map(_._2.toDouble).toArray, closePrices.toArray)
    new SwingWrapper(chart).displayChart()
  }
}
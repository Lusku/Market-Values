package marketValues.ui

import org.apache.spark.sql.SparkSession
import marketValues.api.MarketDataAPI
import marketValues.data.DataProcessor

class UserInterface(spark: SparkSession) {

  def start(): Unit = {
    var exit = false

    while (!exit) {
      println("\nBienvenido a la aplicación de mercado financiero.")
      println("Seleccione una opción:")
      println("1. Datos de una ACCIÓN")
      println("2. Datos de una CRIPTOMONEDA")
      println("3. Salir")

      val choice = scala.io.StdIn.readInt()

      choice match {
        case 1 => handleStocks()
        case 2 => handleCryptos()
        case 3 => exit = true
        case _ => println("Selección no válida. Intente de nuevo.")
      }
    }
    println("Gracias por usar la aplicación. ¡Hasta luego!")
  }

  def handleStocks(): Unit = {
    val symbol = ingresarAccionCryto("ACCION")
    implicit val session: SparkSession = spark

    val today = recuperacionDatos()

    val data = today match {
      case true => MarketDataAPI.getTodayStockData(symbol)
      case false => MarketDataAPI.getStockData(symbol)
      case _ =>
        println("Selección no válida. Intente de nuevo.")
        // Puedes lanzar una excepción o devolver un valor predeterminado
        throw new IllegalArgumentException("Selección no válida")
      // o
      // null.asInstanceOf[TipoDeData] // Reemplaza TipoDeData con el tipo adecuado
    }

    data match {
      case Some(stockData) =>
        val title = s"Acción: $symbol (${if (today) "Día Actual" else "Histórico"})"
        DataProcessor.processAndDisplay(stockData, title)
        DataProcessor.saveToExcel(stockData, symbol, today)
        DataProcessor.generateChart(stockData, symbol, today)
      case None =>
        println(s"No se pudo obtener información para la acción: $symbol")
    }
  }

  def handleCryptos(): Unit = {
    val symbol = ingresarAccionCryto("CRYPTO")
    implicit val session: SparkSession = spark

    val today = recuperacionDatos()
    val data = if (today) {
      MarketDataAPI.getTodayStockData(symbol)
    } else {
      MarketDataAPI.getStockData(symbol)
    }

    data match {
      case Some(cryptoData) =>
        val title = s"Criptomoneda: $symbol (${if (today) "Día Actual" else "Histórico"})"
        DataProcessor.processAndDisplay(cryptoData, title)
        DataProcessor.saveToExcel(cryptoData, symbol, today)
        DataProcessor.generateChart(cryptoData, symbol, today)
      case None =>
        println(s"No se pudo obtener información para la criptomoneda: $symbol")
    }
  }
  def ingresarAccionCryto(tipo: String): String = {
    if (tipo == "ACCION") {
      println("Ingrese el símbolo de la acción (por ejemplo, AAPL):")
    } else {
      println("Ingrese el símbolo de la criptomoneda (por ejemplo, BTC):")
    }
    scala.io.StdIn.readLine().toUpperCase
  }

  def recuperacionDatos(): Boolean = {
    println("\n¿Qué datos quiere recuperar? :")
    println("Seleccione una opción:")
    println("1. Datos del día")
    println("2. Histórico de datos")
    if (scala.io.StdIn.readInt() == 1) true else false
  }
}

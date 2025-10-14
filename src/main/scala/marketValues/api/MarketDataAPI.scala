package marketValues.api

import scalaj.http.Http
import org.json4s._
import org.json4s.jackson.JsonMethods._

import java.time.LocalDate

object MarketDataAPI {
  implicit val formats = DefaultFormats
  val apiKey = "M73T3F4QFZ1GHKKW"

  def getStockData(symbol: String): Option[Map[String, Any]] = {
    val url = s"https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=$symbol&interval=5min&apikey=$apiKey"
    try {
      val response = Http(url).asString.body
      val jsonData = parse(response).extract[Map[String, Any]]

      if (jsonData.isEmpty || !jsonData.keys.exists(_.contains("Time Series"))) {
        println(s"⚠️ No se encontraron datos válidos para $symbol")
        None
      } else {
        Some(jsonData)
      }
    } catch {
      case e: Exception =>
        println(s"❌ Error al obtener datos históricos de $symbol: ${e.getMessage}")
        None
    }
  }




  def getCryptoData(symbol: String): Option[Map[String, Any]] = {
    val url = s"https://api.coingecko.com/api/v3/coins/$symbol/market_chart?vs_currency=usd&days=1"
    val response = Http(url).asString.body
    Some(parse(response).extract[Map[String, Any]])
  }

  def getTodayStockData(symbol: String): Option[Map[String, Any]] = {
    val url = s"https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=$symbol&interval=5min&apikey=$apiKey"
    try {
      val response = Http(url).asString.body
      val jsonData = parse(response).extract[Map[String, Any]]

      // Obtener la clave "Time Series (5min)"
      val timeSeriesKey = jsonData.keys.find(_.contains("Time Series")).getOrElse("")
      val timeSeriesData = jsonData.get(timeSeriesKey).collect {
        case m: Map[String, Map[String, String]] @unchecked => m
      }.getOrElse(Map())

      // Obtener la fecha de hoy en formato "YYYY-MM-DD"
      val today = LocalDate.now().toString

      // Filtrar solo los datos de hoy
      val todayData = timeSeriesData.filter { case (timestamp, _) => timestamp.startsWith(today) }

      if (todayData.isEmpty) {
        println(s"\n⚠️ No hay datos disponibles para hoy ($today) en $symbol")
        None
      } else {
        Some(todayData)
      }
    } catch {
      case e: Exception =>
        println(s"❌ Error al obtener datos de hoy para $symbol: ${e.getMessage}")
        None
    }
  }
}

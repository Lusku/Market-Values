# 📘 Market Values App

## 🚀 Descripción general
**Market Values App** es una aplicación desarrollada en **Scala** con **Apache Spark** que permite consultar y analizar datos financieros en tiempo (casi) real tanto de **acciones** como de **criptomonedas**.

A través de una interfaz de consola, el usuario puede:
- Elegir entre **acciones** o **criptomonedas**.
- Elegir si quiere **datos del día actual** o **histórico**.
- Ver los datos procesados en una tabla con Spark (**OHLCV**: apertura, máximo, mínimo, cierre, volumen).
- **Exportar los datos a Excel (.xlsx)**.
- **Generar un gráfico de precios de cierre** usando XChart.

El objetivo del proyecto es ofrecer una base sencilla y extensible para construir aplicaciones más complejas relacionadas con análisis de mercado, tratamiento de datos financieros y visualización con herramientas Big Data.

---

## 🧱 Arquitectura del proyecto
El proyecto está organizado en los siguientes módulos:

```text
src/main/scala/marketValues/
│
├── api/
│   └── MarketDataAPI.scala
│
├── data/
│   └── DataProcessor.scala
│
└── ui/
    └── UserInterface.scala
```

### 🌐 `MarketDataAPI`
Ubicación: `marketValues.api.MarketDataAPI`

Responsable de acceder a las **APIs externas** y devolver los datos en forma de `Option[Map[String, Any]]`:

- `getStockData(symbol: String): Option[Map[String, Any]]`  
  - Llama a la API de **Alpha Vantage** (`TIME_SERIES_INTRADAY`, intervalo 5 minutos).  
  - Valida que exista la clave de **Time Series**.
  - Maneja errores con `try/catch` y devuelve `None` si no hay datos válidos.

- `getCryptoData(symbol: String): Option[Map[String, Any]]`  
  - Llama a la API de **CoinGecko** (`market_chart` para 1 día en USD).
  - Devuelve los datos en un `Map[String, Any]` envuelto en `Some(...)`.

- `getTodayStockData(symbol: String): Option[Map[String, Any]]`  
  - También usa **Alpha Vantage** con `TIME_SERIES_INTRADAY`.
  - Filtra los datos para quedarse **solo con los registros del día actual** (`LocalDate.now()`).
  - Si no hay datos del día, informa por consola y devuelve `None`.

> ℹ️ **Nota:** la API key está en el código como:
> ```scala
> val apiKey = "M73T3F4QFZ1GHKKW"
> ```
> En un entorno real, deberías extraerla a variables de entorno o un fichero de configuración y no subirla a GitHub.

---

### 🧮 `DataProcessor`
Ubicación: `marketValues.data.DataProcessor`

Encargado de procesar, mostrar, guardar y representar los datos:

- `processAndDisplay(data: Map[String, Any], title: String)(implicit spark: SparkSession): Unit`
  - Localiza la clave de **Time Series** dentro del `Map`.
  - Extrae y transforma las series temporales en filas con:
    - `FechaHora`
    - `Apertura` (`1. open`)
    - `Máximo` (`2. high`)
    - `Mínimo` (`3. low`)
    - `Cierre` (`4. close`)
    - `Volumen` (`5. volume`)
  - Crea un `DataFrame` de Spark y lo ordena de forma descendente por fecha/hora.
  - Muestra el resultado en consola.

- `saveToExcel(data: Map[String, Any], symbol: String, today: Boolean): Unit`
  - Crea un libro de Excel (`XSSFWorkbook`) con una hoja llamada `"Datos"`.
  - Escribe encabezados: `FechaHora`, `Apertura`, `Máximo`, `Mínimo`, `Cierre`, `Volumen`.
  - Vuelca las filas de la serie temporal.
  - Guarda el archivo con el nombre:
    - `SYMBOL-Hoy.xlsx` si `today == true`
    - `SYMBOL-Historico.xlsx` si `today == false`
  - Muestra por consola la ruta completa del fichero generado.

- `generateChart(data: Map[String, Any], symbol: String, today: Boolean): Unit`
  - Extrae los precios de cierre (`4. close`) a lo largo del tiempo.
  - Genera un gráfico de línea con **XChart**:
    - Eje X: índice de los puntos (orden temporal).
    - Eje Y: precio de cierre.
  - Abre una ventana Swing con el gráfico.

---

### 💬 `UserInterface`
Ubicación: `marketValues.ui.UserInterface`

Interfaz de usuario por consola. Gestiona el flujo de interacción:

- Menú principal en bucle:
  ```text
  1. Datos de una ACCIÓN
  2. Datos de una CRIPTOMONEDA
  3. Salir
  ```

- `start()`
  - Muestra el menú en un bucle `while`.
  - Según la opción elegida:
    - `1` → `handleStocks()`
    - `2` → `handleCryptos()`
    - `3` → salir de la aplicación.

- `handleStocks()`
  - Pide el símbolo de la acción (`AAPL`, etc.) mediante `ingresarAccionCryto("ACCION")`.
  - Pregunta si se quieren **datos del día** o **históricos** con `recuperacionDatos()`.
  - Llama a:
    - `MarketDataAPI.getTodayStockData(symbol)` o
    - `MarketDataAPI.getStockData(symbol)` según la elección.
  - Si hay datos (`Some(...)`):
    - Llama a:
      - `DataProcessor.processAndDisplay(...)`
      - `DataProcessor.saveToExcel(...)`
      - `DataProcessor.generateChart(...)`
  - Si no hay datos: muestra un mensaje de aviso.

- `handleCryptos()`
  - Pide el símbolo de la criptomoneda (`BTC`, etc.) mediante `ingresarAccionCryto("CRYPTO")`.
  - Pregunta si se quieren **datos del día** o **históricos** con `recuperacionDatos()`.
  - Según la elección, intenta recuperar datos y los procesa igual que en acciones.

- `ingresarAccionCryto(tipo: String): String`
  - Muestra un mensaje distinto si se trata de una acción o una criptomoneda.
  - Lee el símbolo introducido por el usuario y lo convierte a mayúsculas.

- `recuperacionDatos(): Boolean`
  - Muestra:
    ```text
    1. Datos del día
    2. Histórico de datos
    ```
  - Devuelve `true` si se elige `1` (día actual) y `false` en caso contrario.

---

## 🛠️ Tecnologías usadas
- **Scala**
- **Apache Spark** (`SparkSession`, `DataFrame`)
- **scalaj-http** (peticiones HTTP)
- **json4s** (parseo JSON)
- **XChart** (`org.knowm.xchart`) para gráficos
- **Apache POI** (`XSSFWorkbook`) para exportar a **Excel**
- **Java Swing** (gestión de la ventana de gráficos vía `SwingWrapper`)

---

## 📦 Requisitos
- **Scala** 2.12 o superior  
- **Apache Spark** 3.x  
- **Java 8/11+**  
- Dependencias de librerías (ejemplo SBT):
  ```scala
  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-sql" % "3.5.0",
    "org.scalaj" %% "scalaj-http" % "2.4.2",
    "org.json4s" %% "json4s-jackson" % "4.1.0",
    "org.knowm.xchart" % "xchart" % "3.8.8",
    "org.apache.poi" % "poi-ooxml" % "5.2.3"
  )
  ```

---

## ▶️ Ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/tuusuario/market-values-app.git
   cd market-values-app
   ```

2. Configurar la API Key de Alpha Vantage en `MarketDataAPI.scala`  
   (opcionalmente moverla a variables de entorno o fichero de config).

3. Ejecutar con SBT:
   ```bash
   sbt run
   ```

---

## 📝 Ejemplo de uso (flujo típico)

```text
Bienvenido a la aplicación de mercado financiero.
Seleccione una opción:
1. Datos de una ACCIÓN
2. Datos de una CRIPTOMONEDA
3. Salir
> 1

Ingrese el símbolo de la acción (por ejemplo, AAPL):
> AAPL

¿Qué datos quiere recuperar? :
Seleccione una opción:
1. Datos del día
2. Histórico de datos
> 1

Acción: AAPL (Día Actual)
+----------------------+---------+--------+--------+--------+--------+
| FechaHora            | Apertura| Máximo | Mínimo | Cierre | Volumen|
+----------------------+---------+--------+--------+--------+--------+
| 2025-12-01 15:20:00  | 190.10  | 190.50 | 189.90 | 190.30 | 12345  |
| ...                  |   ...   |   ...  |   ...  |   ...  |   ...  |
+----------------------+---------+--------+--------+--------+--------+

📊 Datos guardados en /ruta/AAPL-Hoy.xlsx
📈 Se abre una ventana con el gráfico de precios de cierre.
```

---

## 📚 Objetivo del proyecto
Este proyecto sirve como base para:

- Realizar **ETL sencillas** con Spark aplicadas a datos financieros.
- Crear aplicaciones Scala con **capas bien separadas** (UI, API, procesamiento).
- Integrar APIs externas con herramientas de análisis y visualización.
- Practicar exportación de datos a **Excel** y generación de **gráficos** a partir de series temporales.

Es ideal como proyecto educativo o como punto de partida para herramientas más grandes de análisis cuantitativo o dashboards.

---

## 📌 Mejoras futuras
- Añadir nuevas fuentes de datos (otros proveedores de mercado).
- Añadir más indicadores técnicos (medias móviles, RSI, etc.).
- Persistencia en formatos como **Parquet** o **Delta Lake**.
- Crear una **API REST** o interfaz web.
- Internacionalización de mensajes y soporte multilingüe.

---

## 🟦 Resumen corto (para la descripción del repositorio)
> Aplicación en Scala y Apache Spark para consultar y analizar datos financieros (acciones y criptomonedas), con opción de elegir entre datos del día o históricos. Procesa series temporales (OHLCV), exporta a Excel y genera gráficos de precios usando XChart.

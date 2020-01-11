package craicoverflow89.krash.system

import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.URLDecoder

class KrashServer(private val port: Int, private val logic: (request: KrashServerRequest, response: (response: String) -> Unit) -> Unit) {

    init {

        // Create Server
        HttpServer.create(InetSocketAddress(port), 0)?.apply {

            // Create Context
            createContext("/", KrashServerHandler()).apply {

                // Apply Filter
                filters.add(KrashServerFilter())
            }

            // Set Executor
            executor = null

            // Start Server
            start()
        }
    }

    inner class KrashServerFilter: Filter() {

        override fun description() = "Parses request parameters"

        override fun doFilter(exchange: HttpExchange, chain: Chain) {

            // Define Logic
            val decodeString = {value: String -> URLDecoder.decode(value, System.getProperty("file.encoding"))}
            val parseQuery = fun(query: String, params: HashMap<String, String>): HashMap<String, String> {

                // Split Pairs
                query.split("&").forEach {

                    // Parse Pair
                    val split = it.split("=")
                    val key: String = if(split.isNotEmpty()) decodeString(split[0]) else ""
                    val value: String = if(split.size > 1) decodeString(split[1]) else ""

                    // Append Pair
                    params[key] = value
                }

                // Return Params
                return params
            }

            // Parse GET
            exchange.setAttribute("parameters", if(exchange.requestURI.rawQuery != null) parseQuery(exchange.requestURI.rawQuery, hashMapOf()) else hashMapOf())

            // Parse POST
            if(exchange.requestMethod.equals("POST", ignoreCase = true)) {
                val reader = BufferedReader(InputStreamReader(exchange.requestBody, "utf-8"))
                exchange.setAttribute("parameters", parseQuery(reader.readLine(), exchange.getAttribute("parameters") as HashMap<String, String>))
            }

            // Invoke Filter
            chain.doFilter(exchange)
        }

    }

    inner class KrashServerHandler: HttpHandler {

        override fun handle(ex: HttpExchange) {

            // Request Data
            val path = ex.requestURI.path
            val method = ex.requestMethod
            val parameterMap = ex.getAttribute("parameters") as HashMap<String, Any>

            // Invoke Logic
            logic(KrashServerRequest(path, method, parameterMap)) {response: String ->
                ex.apply {

                    // Response Headers
                    sendResponseHeaders(200, response.length.toLong())

                    // Response Body
                    with(responseBody) {
                        write(response.toByteArray())
                        close()
                    }
                }
            }
        }

    }

}

data class KrashServerRequest(val path: String, val method: String, val parameter: HashMap<String, Any>)
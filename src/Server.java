import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] arg) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new TestHandler());
        server.createContext("/search", new SearchHanlder());
        server.start();
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "hello world";
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class SearchHanlder implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                JSONObject object = App.testSearchIndex(getRequestParam(exchange).replace("text=", ""));
                handleResponse(exchange, object);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private String getRequestParam(HttpExchange httpExchange) throws Exception {
            String paramStr = "";
    
            if (httpExchange.getRequestMethod().equals("GET")) {
                //GET请求读queryString
                paramStr = httpExchange.getRequestURI().getQuery();
            } else {
                //非GET请求读请求体
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "utf-8"));
                StringBuilder requestBodyContent = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    requestBodyContent.append(line);
                }
                paramStr = requestBodyContent.toString();
            }
    
            return paramStr;
        }

        private void handleResponse(HttpExchange httpExchange, JSONObject object) throws Exception {
            byte[] responseContentByte = object.toString().getBytes("utf-8");
    
            //设置响应头，必须在sendResponseHeaders方法之前设置！
            Headers head = httpExchange.getResponseHeaders();

            //设置响应码和响应体长度，必须在getResponseBody方法之前调用！
            httpExchange.sendResponseHeaders(200, responseContentByte.length);
            
            OutputStream out = httpExchange.getResponseBody();
            out.write(responseContentByte);
            out.flush();
            out.close();
        }
    }
}

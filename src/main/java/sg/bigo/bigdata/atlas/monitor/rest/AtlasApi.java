package sg.bigo.bigdata.atlas.monitor.rest;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AtlasApi {
    private final static Logger LOG = LoggerFactory.getLogger(AtlasApi.class);

    public JSONObject getMetrics(String connectUrl, int timeout) {
        HttpURLConnection connection = null;
        JSONObject metrics = new JSONObject();
        try {
            if (connectUrl == null) {
                throw new IOException("Unknown URL. Unable to connect to atlas.");
            }
            connection = getConnection(connectUrl);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(true);
            int statusCode = connection.getResponseCode();
            if (HttpURLConnection.HTTP_OK == statusCode) {
                LOG.debug("Fetched metrics from atlas " + connectUrl);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String readLine = null;
                StringBuffer response = new StringBuffer();
                while (null != (readLine = bufferedReader.readLine())) {
                    response.append(readLine);
                }
                bufferedReader.close();
                metrics = JSONObject.parseObject(response.toString());
            } else {
                LOG.info("Unable to get metrics from atlas, " + connectUrl + ", " + "statusCode = " + statusCode);
            }
            return metrics;
        } catch (IOException ioe) {
            StringBuilder errorMessage = new StringBuilder("Unable to connect to atlas, " + connectUrl + "\n");
            try {
                if ((connection != null)) {
                    errorMessage.append(connection.getErrorStream());
                }
            } catch (Exception e) {
                LOG.info("Unable to get metrics from atlas, " + connectUrl + ", " + "error: " + e);
            }
            LOG.info(errorMessage.toString(), ioe);
            return metrics;
        }
    }

    private HttpURLConnection getConnection(String spec) throws IOException {
        return (HttpURLConnection) new URL(spec).openConnection();
    }

}

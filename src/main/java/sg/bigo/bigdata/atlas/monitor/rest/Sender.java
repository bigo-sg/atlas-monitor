package sg.bigo.bigdata.atlas.monitor.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Sender {
    private final static Logger LOG = LoggerFactory.getLogger(Sender.class);

    public boolean emitMetricsJson(String connectUrl, String jsonData, int timeout) {
        HttpURLConnection connection = null;
        try {
            if (connectUrl == null) {
                throw new IOException("Unknown URL. Unable to connect to ambari metrics collector.");
            }
            connection = getConnection(connectUrl);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(true);

            if (jsonData != null) {
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes("UTF-8"));
                }
            }
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                LOG.info("Unable to POST metrics to collector, " + connectUrl + ", " + "statusCode = " + statusCode);
            } else {
                LOG.debug("Metrics posted to Collector " + connectUrl);
            }
            cleanupInputStream(connection.getInputStream());
            return true;
        } catch (IOException ioe) {
            StringBuilder errorMessage = new StringBuilder("Unable to connect to collector, " + connectUrl + "\n");
            try {
                if ((connection != null)) {
                    errorMessage.append(cleanupInputStream(connection.getErrorStream()));
                }
            } catch (IOException e) {
                //NOP
            }
            LOG.info(errorMessage.toString(), ioe);
            return false;
        }

    }

    private HttpURLConnection getConnection(String spec) throws IOException {
        return (HttpURLConnection) new URL(spec).openConnection();
    }

    private String cleanupInputStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            try (
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)
            ) {
                // read the response body
                String line;
                while ((line = br.readLine()) != null) {
                    if (LOG.isDebugEnabled()) {
                        sb.append(line);
                    }
                }
            } finally {
                is.close();
            }
        }
        return sb.toString();
    }
}

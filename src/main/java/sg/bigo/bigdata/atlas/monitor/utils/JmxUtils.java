package sg.bigo.bigdata.atlas.monitor.utils;

import static sg.bigo.bigdata.atlas.monitor.Constants.*;

public class JmxUtils {
    public static String getAmbariCollectorUri(String host, int port) {
        StringBuilder sb = new StringBuilder(HTTP_PROTOCOL);
        sb.append("://");
        sb.append(host)
          .append(":")
          .append(port)
          .append(AMBARI_COLLECTOR_RESTAPI);
        return sb.toString();
    }

    public static String getAtlasMetricsUri(String host) {
        StringBuilder sb = new StringBuilder(host);
        sb.append(ATLAS_METRICS_RESTAPI);
        return sb.toString();
    }
}

package sg.bigo.bigdata.atlas.monitor.utils;

import static sg.bigo.bigdata.atlas.monitor.Constants.*;

public class Utils {
    public static String getAmbariCollectorUri(String host, int port) {
        StringBuilder sb = new StringBuilder(AMBARI_COLLECTOR_PROTOCOL);
        sb.append("://");
        sb.append(host)
          .append(":")
          .append(port)
          .append(AMBARI_COLLECTOR_RESTAPI);
        return sb.toString();
    }

}

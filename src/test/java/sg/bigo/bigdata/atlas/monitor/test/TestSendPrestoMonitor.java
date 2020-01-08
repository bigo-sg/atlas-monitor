package sg.bigo.bigdata.atlas.monitor.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.bigo.bigdata.atlas.monitor.InfoArgs;
import sg.bigo.bigdata.atlas.monitor.metrics.JmxHandler;

public class TestSendPrestoMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(TestSendPrestoMonitor.class);

    public static void main(String[] args) throws Exception {
        InfoArgs infoArgs = new InfoArgs();
        JmxHandler jmxHandler = new JmxHandler("test_host", infoArgs);
        while (true) {
            boolean re = jmxHandler.testMetrics();
            LOG.info("send result: " + re);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

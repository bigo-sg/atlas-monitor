package sg.bigo.bigdata.atlas.monitor.sink;

import org.apache.hadoop.metrics2.sink.timeline.TimelineMetrics;
import org.apache.hadoop.metrics2.sink.timeline.availability.MetricCollectorHAHelper;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.bigo.bigdata.atlas.monitor.rest.Sender;
import sg.bigo.bigdata.atlas.monitor.InfoArgs;
import sg.bigo.bigdata.atlas.monitor.utils.JmxUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static sg.bigo.bigdata.atlas.monitor.Constants.RETRY_COUNT_BEFORE_COLLECTOR_FAILOVER;

public class MetricsSink {
    private final static Logger LOG = LoggerFactory.getLogger(MetricsSink.class);

    private static ObjectMapper mapper;
    private static MetricCollectorHAHelper collectorHAHelper = null;
    private final static Set<String> cacheHosts = new HashSet<>();
    private final static Random r = new Random();
    // failed 5 times fail
    private final static AtomicInteger failedCollectorConnectionsCounter = new AtomicInteger(0);

    private Sender sender;
    private InfoArgs args;

    static {
        mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.setAnnotationIntrospector(introspector);
        mapper.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }

    public MetricsSink(InfoArgs args) {
        this.args = args;
        sender = new Sender();
        synchronized (MetricsSink.class) {
            if (collectorHAHelper == null) {
                collectorHAHelper = new MetricCollectorHAHelper(
                        args.getAmbariZkQuorum(),
                        args.getAmbariZkConnTryCount(),
                        args.getAmbariZkConnTryInterval());
            }
        }
    }

    /**
     * send metrics to ambari collector by http protocol
     * @param metrics
     * @return
     * @throws Exception
     */
    public boolean emitMetrics(TimelineMetrics metrics) throws Exception {
        if (!args.isEmitMetrics()) {
            if (args.isDebug()) {
                LOG.debug("current emitMetricsSwitch is off, do not send metrics to ambari collector");
            }
            return true;
        }
        String collectorHost = getCurrentCollectorHost();
        String connectUrl = JmxUtils.getAmbariCollectorUri(collectorHost, args.getAmbariCollectorHttpPort());
        String jsonData = null;
        LOG.info("EmitMetrics connectUrl = " + connectUrl + ", objName: " + args.getObjName());
        try {
            jsonData = mapper.writeValueAsString(metrics);
        } catch (IOException e) {
            LOG.error("Unable to parse metrics", e);
        }

        boolean sendResult = false;
        if (jsonData != null) {
            sendResult = sender.emitMetricsJson(connectUrl, jsonData, args.getAmbariCollectorHttpTimeout());
        }
        if (!sendResult) {
            failedCollectorConnectionsCounter.getAndIncrement();
            if (failedCollectorConnectionsCounter.get() > RETRY_COUNT_BEFORE_COLLECTOR_FAILOVER) {
                removeInvalidHost(collectorHost);
            } else {
                // retry? or go on
                // sendResult = sender.emitMetricsJson(connectUrl, jsonData);
            }
        } else {
            failedCollectorConnectionsCounter.set(0);
        }

        LOG.info("EmitMetrics data size: " + metrics.getMetrics().size() +
                ", result: " + sendResult + ", objName: " + args.getObjName());
        return sendResult;
    }

    /**
     * init the ambari HA client and cache the collector hosts
     * @return
     * @throws Exception
     */
    private synchronized static String getCurrentCollectorHost() throws Exception {
        if (cacheHosts.isEmpty()) {

            if (collectorHAHelper == null) {
                LOG.error("Please init MetricsSink first");
                throw new Exception("Please init MetricsSink first");
            }
            cacheHosts.addAll(collectorHAHelper.findLiveCollectorHostsFromZNode());
            LOG.info("Get metric collector hosts: " + cacheHosts);
        }

        if (cacheHosts.isEmpty()) {
            throw new Exception("has no alive metric collector in ambari zk");
        }

        // random choose the ambari collector
        int randomIndex = r.nextInt(cacheHosts.size());
        Iterator<String> iterator = cacheHosts.iterator();
        String host = null;
        while (iterator.hasNext() && randomIndex >=0 ) {
            randomIndex--;
            host = iterator.next();
        }

        return host;
    }

    private synchronized static void removeInvalidHost(String host) {
        cacheHosts.remove(host);
        failedCollectorConnectionsCounter.set(0);
    }
}

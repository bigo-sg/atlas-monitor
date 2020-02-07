package sg.bigo.bigdata.atlas.monitor.metrics;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.util.ObjectToDouble;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetric;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetricMetadata.MetricType;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.bigo.bigdata.atlas.monitor.Constants;
import sg.bigo.bigdata.atlas.monitor.InfoArgs;
import sg.bigo.bigdata.atlas.monitor.rest.AtlasApi;
import sg.bigo.bigdata.atlas.monitor.sink.MetricsSink;
import sg.bigo.bigdata.atlas.monitor.utils.AtlasUtils;
import sg.bigo.bigdata.atlas.monitor.utils.JmxUtils;

import java.util.*;

import static com.googlecode.jmxtrans.util.NumberUtils.isNumeric;

public class JmxHandler {
    private final static Logger LOG = LoggerFactory.getLogger(JmxHandler.class);

    private String hostname;
    private InfoArgs args;
    private MetricsSink metricsSink;

    private ObjectToDouble converter = new ObjectToDouble();

    public JmxHandler(String hostname, InfoArgs args) {
        this.hostname = hostname;
        this.args = args;
        metricsSink = new MetricsSink(args);
        TimelineMetricsFilter.initializeMetricFilter(args.getJmxWhitelistAttrsFile());
    }

    /**
     * Handle jmxTrans Results and send them to ambari collector
     * @param results
     * @return
     * @throws Exception
     */
    public boolean handleJmxtransResult(ImmutableList<Result> results) throws Exception {
        TimelineMetrics metrics = new TimelineMetrics();
        ArrayList<TimelineMetric> timelineMetrics = new ArrayList<>();
        // JMX metrics
        List<TimelineMetric> jmxMetrics = buildTimelineMetrics(results);
        timelineMetrics.addAll(jmxMetrics);
        if (jmxMetrics.size() == 0) {
            LOG.warn("jmx metric size is 0");
        }

        metrics.setMetrics(timelineMetrics);
        if (metrics.getMetrics().size() == 0) {
            LOG.warn("all metric size is 0, return");
            return true;
        }

        return emitMetrics(metrics);
    }

    /**
     * Handle API Results and send them to ambari collector
     * @return
     * @throws Exception
     */
    public boolean handleApiResult() throws Exception {
        TimelineMetrics metrics = new TimelineMetrics();
        ArrayList<TimelineMetric> timelineMetrics = new ArrayList<>();

        // API metrics
        if (args.isCoordinator()) {
            List<TimelineMetric> apiMetrics = buildApiTimelineMetrics();
            timelineMetrics.addAll(apiMetrics);
            if (apiMetrics.size() == 0) {
                LOG.warn("api metric size is 0");
            }
        }

        metrics.setMetrics(timelineMetrics);
        if (metrics.getMetrics().size() == 0) {
            LOG.warn("all metric size is 0, return");
            return true;
        }

        return emitMetrics(metrics);
    }

    /**
     * build the timeline metrics of ambari
     * @param results
     * @return
     */
    private List<TimelineMetric> buildTimelineMetrics(ImmutableList<Result> results) {
        List<TimelineMetric> metrics = new LinkedList();
        for (Result result: results) {
            if (args.isDebug()) {
                LOG.debug("result is: " + result);
            }
            TimelineMetric currMetric = buildTimelineMetric(result);
            if (currMetric != null) {
                metrics.add(currMetric);
            }
        }
        return metrics;
    }

    /**
     * Build atlas metric
     * @return
     */
    public List<TimelineMetric> buildApiTimelineMetrics() {
        List<TimelineMetric> metrics = new LinkedList();
        AtlasApi prestoApi = new AtlasApi();
        long currTimeMillis = new Date().getTime();

        // get active atlas host
        String activeAddress = AtlasUtils.getActiveAddress(args.getAmbariZkQuorum());

        // request presto for metrics
        JSONObject data = prestoApi.getMetrics(JmxUtils.getAtlasMetricsUri(activeAddress),
                5000).getJSONObject("data");

        JSONObject general = data.getJSONObject("general");
        JSONObject tag = data.getJSONObject("tag");
        JSONObject entity = data.getJSONObject("entity");
        JSONObject system = data.getJSONObject("system");

        // metric for general
        Map<String, Object> generalMetricMap = general.getInnerMap();
        for (String key : generalMetricMap.keySet()) {
            if (Constants.ATLAS_GENERAL_METRICS.contains(key)) {
                Object value = generalMetricMap.get(key);
                String metricName = "atlas.metric." + "general." + key;
                TimelineMetric currMetric = buildApiTimelineMetric(metricName, value, MetricType.GAUGE, currTimeMillis);
                if (currMetric != null) {
                    metrics.add(currMetric);
                }
            }
        }

        Map<String, Object> topics = general.getJSONObject("stats").getJSONObject("Notification:topicDetails")
                .getInnerMap();
        for (String topic : topics.keySet()) {
            Map<String, Object> topicDetails = JSONObject.parseObject(topics.get(topic).toString()).getInnerMap();
            for (String key : topicDetails.keySet()) {
                Object value = topicDetails.get(key);
                String metricName = "atlas.metric." + "general." + StringUtils.replace(topic, "-", "_")
                        + "." + key;
                TimelineMetric currMetric = buildApiTimelineMetric(metricName, value, MetricType.GAUGE, currTimeMillis);
                if (currMetric != null) {
                    metrics.add(currMetric);
                }
            }
        }

        // metric for tag
        Map<String, Object> tagEntities = tag.getJSONObject("tagEntities").getInnerMap();
        for (String key : tagEntities.keySet()) {
            Object value = tagEntities.get(key);
            String metricName = "atlas.metric." + "tag.tagEntities." + key;
            TimelineMetric currMetric = buildApiTimelineMetric(metricName, value, MetricType.GAUGE, currTimeMillis);
            if (currMetric != null) {
                metrics.add(currMetric);
            }
        }

        // metric for entity
        Map<String, Object> entityActive = entity.getJSONObject("entityActive").getInnerMap();
        for (String key : entityActive.keySet()) {
            Object value = entityActive.get(key);
            String metricName = "atlas.metric." + "entity.entityActive." + key;
            TimelineMetric currMetric = buildApiTimelineMetric(metricName, value, MetricType.GAUGE, currTimeMillis);
            if (currMetric != null) {
                metrics.add(currMetric);
            }
        }
        Map<String, Object> entityDeleted = entity.getJSONObject("entityDeleted").getInnerMap();
        for (String key : entityDeleted.keySet()) {
            Object value = entityDeleted.get(key);
            String metricName = "atlas.metric." + "entity.entityDeleted." + key;
            TimelineMetric currMetric = buildApiTimelineMetric(metricName, value, MetricType.GAUGE, currTimeMillis);
            if (currMetric != null) {
                metrics.add(currMetric);
            }
        }

        return metrics;
    }

    /**
     * Build atlas metric
     * @param metricName
     * @param value
     * @return
     */
    private TimelineMetric buildApiTimelineMetric(String metricName, Object value, MetricType metricType, long currTimeMillis) {
        TimelineMetric metric = null;
        if (isNumeric(value)) {
            if (args.isDebug()) {
                LOG.debug("Creating timeline metric, " +
                        " metricType = " + metricType +
                        " time = " + currTimeMillis +
                        " app_id = " + args.getAmbariAppId() +
                        " hostname = " + hostname +
                        " attrName = " + metricName +
                        " attrValue = " + value);
            }
            TimelineMetric timelineMetric = new TimelineMetric();
            timelineMetric.setMetricName(metricName);
            timelineMetric.setHostName("hostname");
            timelineMetric.setAppId("args.getAmbariAppId()");
            timelineMetric.setStartTime(currTimeMillis);
            timelineMetric.setTimestamp(currTimeMillis);
            timelineMetric.setType(metricType.name());
            timelineMetric.getMetricValues().put(currTimeMillis, Double.valueOf(String.valueOf(value)));
            return timelineMetric;
        }
        return metric;
    }

    /**
     * build the timeline metric of ambari
     * Note that: return null if the attribute is not accepted
     *
     * jmx example:
     *   Result(attributeName=99thPercentile,
     *       className=com.yammer.metrics.reporting.JmxReporter$Histogram,
     *       objDomain=kafka.network,
     *       typeName=type=RequestMetrics,name=LocalTimeMs,request=Offsets,
     *       valuePath=[],
     *       value=5.0,
     *       epoch=1556079293324,
     *       keyAlias=null
     *   )
     *
     *   result attrName: kafka.network.RequestMetrics.LocalTimeMs.request.Offsets.99thpercentile
     *
     * @param result
     * @return
     */
    private TimelineMetric buildTimelineMetric(Result result) {
        TimelineMetric metric = null;
        if (isNumeric(result.getValue())) {
            long currTimeMillis = result.getEpoch();
            StringBuilder attrNameSb = new StringBuilder(result.getObjDomain());

            // append type:name to attrName
            appendTypeName(result.getTypeNameMap(), attrNameSb);
            String attrNameSuffix = result.getAttributeName().toLowerCase();
            // append attribute name except "value"
            if (attrNameSuffix != null && !attrNameSuffix.equals("value")) {
                attrNameSb.append(Constants.JMX_ATTR_NAME_SEP).append(attrNameSuffix);
            }

            // append value path
            ImmutableList<String> valuePath = result.getValuePath();
            if (valuePath != null && !valuePath.isEmpty()) {
                for (String value: valuePath) {
                    attrNameSb.append(value);
                }
            }

            // remove the blank in attribute name
            String attrName = attrNameSb.toString().replace(" ", "");
            // only create timelineMetric for accepted attribute
            if (TimelineMetricsFilter.acceptAttr(attrName)) {
                Double value = converter.apply(result.getValue());
                metric = createTimelineMetric(currTimeMillis, MetricType.GAUGE, attrName, value);
            } else {
                if (args.isDebug()) {
                    LOG.debug("attribute: " + attrName + " is not in white list file");
                }
            }
        }
        return metric;
    }

    /**
     * sent metrics to ambari collector
     * @param metrics
     * @return
     * @throws Exception
     */
    protected boolean emitMetrics(TimelineMetrics metrics) throws Exception {
        return metricsSink.emitMetrics(metrics);
    }

    /**
     * concat the typeNameMap to attribute name
     * example1:
     *   typeName=name=PS Eden Space,type=MemoryPool
     *   the result is: "{attrNamePrefixSb}.PS Eden Space.MemoryPool"
     * example2:
     *   typeName=type=RequestMetrics,name=TotalTimeMs,request=OffsetFetch
     *   the result is: "{attrNamePrefixSb}.RequestMetrics.TotalTimeMs.request.OffsetFetch"
     * @param typeNameMap
     * @param attrNamePrefixSb
     */
    private void appendTypeName(Map<String, String> typeNameMap, StringBuilder attrNamePrefixSb) {
        if (typeNameMap != null && typeNameMap.size() > 0) {
            for (String typeName: args.getTypeNamesList()) {
                String typeNameValue = typeNameMap.get(typeName);
                if (typeNameValue != null && !typeNameValue.isEmpty()) {
                    if (!typeName.equals("type") && !typeName.equals("name")) {
                        attrNamePrefixSb.append(Constants.JMX_ATTR_NAME_SEP).append(typeName);
                    }
                    attrNamePrefixSb.append(Constants.JMX_ATTR_NAME_SEP).append(typeNameValue);
                }
            }
        }
    }

    /**
     * create the timeline metric based on the basic param
     * @param currentTimeMillis
     * @param metricType
     * @param attributeName
     * @param attributeValue
     * @return
     */
    protected TimelineMetric createTimelineMetric(long currentTimeMillis,
                                                MetricType metricType,
                                                String attributeName,
                                                Number attributeValue) {
        if (args.isDebug()) {
            LOG.debug("Creating timeline metric, " +
                    " metricType = " + metricType +
                    " time = " + currentTimeMillis +
                    " app_id = " + args.getAmbariAppId() +
                    " hostname = " + hostname +
                    " attrName = " + attributeName +
                    " attrValue = " + attributeValue);
        }
        TimelineMetric timelineMetric = new TimelineMetric();
        timelineMetric.setMetricName(attributeName);
        timelineMetric.setHostName(hostname);
        timelineMetric.setAppId(args.getAmbariAppId());
        timelineMetric.setStartTime(currentTimeMillis);
//        timelineMetric.setTimestamp(currentTimeMillis);
//        timelineMetric.setType(ClassUtils.getShortCanonicalName(attributeValue, "Number"));
        timelineMetric.setType(metricType.name());
        timelineMetric.getMetricValues().put(currentTimeMillis, attributeValue.doubleValue());
        return timelineMetric;
    }

    public boolean testMetrics() throws Exception {
        TimelineMetrics metrics = new TimelineMetrics();
        List<TimelineMetric> allMetrics = new ArrayList<TimelineMetric>();
        long currTime = System.currentTimeMillis();
        LOG.info("curr time: " + currTime);
        Random r = new Random();
        int v1 = r.nextInt(100);
        int v2 = r.nextInt(100);
        int v3 = r.nextInt(100);
        TimelineMetric metric1 = createTimelineMetric(
                currTime,
                MetricType.GAUGE,
                "kafka.server.BrokerTopicMetrics.BytesInPerSec.count",
                v1
        );
        TimelineMetric metric2 = createTimelineMetric(
                currTime,
                MetricType.GAUGE,
                "kafka.server.BrokerTopicMetrics.BytesOutPerSec.count",
                v2
        );
        TimelineMetric metric3 = createTimelineMetric(
                currTime,
                MetricType.GAUGE,
                "test.new.name.metric.count.1",
                v3
        );
        allMetrics.add(metric1);
        allMetrics.add(metric2);
        allMetrics.add(metric3);
        metrics.setMetrics(allMetrics);

        return emitMetrics(metrics);
    }

}

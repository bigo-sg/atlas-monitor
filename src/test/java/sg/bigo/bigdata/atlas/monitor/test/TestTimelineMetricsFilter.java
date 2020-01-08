package sg.bigo.bigdata.atlas.monitor.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.bigo.bigdata.atlas.monitor.metrics.TimelineMetricsFilter;

import java.net.Inet4Address;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class TestTimelineMetricsFilter {
    private final static Logger LOG = LoggerFactory.getLogger(TestTimelineMetricsFilter.class);

    public static void main(String[] args) {
        Set<Pattern> whitelistedMetricPatterns1 = new HashSet<>();
        Pattern p1 = Pattern.compile("^kafka\\.server\\.BrokerTopicMetrics\\.(BytesInPerSec|BytesOutPerSec|MessagesInPerSec|TotalProduceRequestsPerSec|TotalFetchRequestsPerSec)\\.topic\\.test\\w*\\.(count|oneminuterate)$");
        whitelistedMetricPatterns1.add(p1);
        TimelineMetricsFilter.init(null, whitelistedMetricPatterns1);
        String testAttr1 = "kafka.server.BrokerTopicMetrics.BytesInPerSec.topic.test.oneminuterate";
        String testAttr2 = "kafka.server.BrokerTopicMetrics.BytesInPerSec.topic.test1.count";
        String testAttr3 = "kafka.server.BrokerTopicMetrics.BytesOutPerSec.topic.test2.oneminuterate";
        String testAttr4 = "kafka.server.BrokerTopicMetrics.BytesOutPerSec.topic.test2.count";
        String testAttr5 = "kafka.server.BrokerTopicMetrics.BytesOutPerSec.topic.test2.count1";
        String testAttr6 = "kafka.server.BrokerTopicMetrics.BytesOutPerSec.topic.1test2.count1";
        String testAttr7 = "kafka.server.BrokerTopicMetrics.BytesOutPerSec1.topic.test2.count1";
        String testAttr8 = "kafka.server.BrokerTopicMetrics1.BytesOutPerSec.topic.1test2.count1";
        LOG.info("p1 match result[true]: " + TimelineMetricsFilter.acceptAttr(testAttr1));
        LOG.info("p1 match result[true]: " + TimelineMetricsFilter.acceptAttr(testAttr2));
        LOG.info("p1 match result[true]: " + TimelineMetricsFilter.acceptAttr(testAttr3));
        LOG.info("p1 match result[true]: " + TimelineMetricsFilter.acceptAttr(testAttr4));
        LOG.info("p1 match result[false]: " + TimelineMetricsFilter.acceptAttr(testAttr5));
        LOG.info("p1 match result[false]: " + TimelineMetricsFilter.acceptAttr(testAttr6));
        LOG.info("p1 match result[false]: " + TimelineMetricsFilter.acceptAttr(testAttr7));
        LOG.info("p1 match result[false]: " + TimelineMetricsFilter.acceptAttr(testAttr8));

        Pattern p2 = Pattern.compile("^org\\.apache\\.ZooKeeperService\\.name0\\.ReplicatedServer_id\\d+\\.name1\\.replica\\.\\d+\\.name2\\.(Follower|Leader)\\.\\w+$");
        Set<Pattern> whitelistedMetricPatterns2 = new HashSet<>();
        whitelistedMetricPatterns2.add(p2);
        TimelineMetricsFilter.init(null, whitelistedMetricPatterns2);
        testAttr1 = "org.apache.ZooKeeperService.name0.ReplicatedServer_id2.name1.replica.2.name2.Follower.avgrequestlatency";
        testAttr2 = "org.apache.ZooKeeperService.name0.ReplicatedServer_id2.name1.replica.2.name2.Follower.maxclientcnxnsperhost";
        testAttr3 = "org.apache.ZooKeeperService.name0.ReplicatedServer_id3.name1.replica.3.name2.Leader.maxclientcnxnsperhost";
        testAttr4 = "org.apache.ZooKeeperService1.name0.ReplicatedServer_id3.name1.replica.3.name2.Leader.maxclientcnxnsperhost";
        testAttr5 = "org.apache.ZooKeeperService.name0.ReplicatedServer_id3.name1.replica.3.namen.Leader.maxclientcnxnsperhost";
        LOG.info("p2 match result[true]: " + TimelineMetricsFilter.acceptAttr(testAttr1));
        LOG.info("p2 match result[true]: " + TimelineMetricsFilter.acceptAttr(testAttr2));
        LOG.info("p2 match result[true]: " + TimelineMetricsFilter.acceptAttr(testAttr3));
        LOG.info("p2 match result[false]: " + TimelineMetricsFilter.acceptAttr(testAttr4));
        LOG.info("p2 match result[false]: " + TimelineMetricsFilter.acceptAttr(testAttr5));

        try {
            String hostName = Inet4Address.getLocalHost().getHostName();
            LOG.info("hostName: " + hostName);
        } catch (Exception e) {

        }
    }
}

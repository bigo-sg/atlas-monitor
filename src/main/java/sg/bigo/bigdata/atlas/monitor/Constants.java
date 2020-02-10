package sg.bigo.bigdata.atlas.monitor;

import java.util.Arrays;
import java.util.HashSet;

public class Constants {

    public final static String HTTP_PROTOCOL = "http";
    public final static String AMBARI_COLLECTOR_RESTAPI = "/ws/v1/timeline/metrics";
    public final static String ATLAS_METRICS_RESTAPI = "/api/atlas/admin/metrics";
    /**
     * ambari metric protocol, specify component
     */
    public final static String AMBARI_COLLECTOR_APP_ID = "ambariAppId";
    /**
     * ambari collector zookeeper quorum
     */
    public final static String AMBARI_COLLECTOR_ZK_QUORUM = "ambariZkQuorum";
    public final static String AMBARI_COLLECTOR_ZK_CONN_TRY_COUNT = "ambariZkConnTryCount";
    public final static String AMBARI_COLLECTOR_ZK_CONN_TRY_INTERVAL_MS = "ambariZkConnTryInterval";
    public final static String AMBARI_COLLECTOR_CONN_TIMEOUT = "ambariCollectorHttpTimeout";
    public final static String AMBARI_COLLECTOR_PORT = "ambariCollectorHttpPort";
    /**
     * whether send the metrics to ambari collector or not, for debug
     */
    public final static String EMIT_METRICS_SWITCH = "emitMetricsSwitch";
    /**
     * jmx attribute white list file, support pattern which start with {WHITE_LIST_PATTERN_PREFIX}
     */
    public final static String JMX_WHITELIST_ATTRS_FILE = "jmxAttrsWhitelistFile";
    /**
     * jmx typeName schema list
     */
    public final static String JMX_TYPE_NAMES_LIST = "jmxTypeNamesList";
    public final static String JMX_ATTR_NAME_SEP = ".";
    public final static int RETRY_COUNT_BEFORE_COLLECTOR_FAILOVER = 5;

    /**
     * metric white list pattern prefix flag
     */
    public final static String WHITE_LIST_PATTERN_PREFIX = "._p_";

    /**
     * local host
     */
    public final static String LOCAL_HOST = "0.0.0.0";

    /**
     * atlas Api
     */
    public final static HashSet ATLAS_GENERAL_METRICS = new HashSet(Arrays.asList("entityCount", "tagCount", "typeUnusedCount", "typeCount"));

}

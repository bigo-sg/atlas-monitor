package sg.bigo.bigdata.atlas.monitor;

public class Constants {

    public final static String AMBARI_COLLECTOR_PROTOCOL = "http";
    public final static String AMBARI_COLLECTOR_RESTAPI = "/ws/v1/timeline/metrics";
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
     *
     */
    public final static String LOCAL_HOST = "0.0.0.0";

    /**
     * Presto Api
     */
    public final static String IS_COORDINATOR = "isCoordinator";
    public final static String PRESTO_API_PROTOCOL = "http";
    public final static String PRESTO_API_PORT = "prestoApiPort";
    public final static String PRESTO_API_INFO = "/v1/info";
    public final static String PRESTO_API_CLUSTER = "/v1/cluster";
    public final static String PRESTO_API_QUERY = "/v1/query";
    public final static String PRESTO_API_WORKER_STATUS = "/v1/status";
    public final static String PRESTO_TIMEOUT = "prestoTimeout";

}

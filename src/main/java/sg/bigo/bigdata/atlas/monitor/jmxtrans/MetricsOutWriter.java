package sg.bigo.bigdata.atlas.monitor.jmxtrans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.model.Server;
import com.googlecode.jmxtrans.model.ValidationException;
import com.googlecode.jmxtrans.model.output.BaseOutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.bigo.bigdata.atlas.monitor.InfoArgs;
import sg.bigo.bigdata.atlas.monitor.metrics.JmxHandler;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Map;

import static sg.bigo.bigdata.atlas.monitor.Constants.*;

public class MetricsOutWriter extends BaseOutputWriter {
    private final static Logger LOG = LoggerFactory.getLogger(MetricsOutWriter.class);

    private JmxHandler jmxHandler;
    private InfoArgs args;

    @JsonCreator
    public MetricsOutWriter(@JsonProperty("typeNames") ImmutableList<String> typeNames,
                            @JsonProperty("booleanAsNumber") boolean booleanAsNumber,
                            @JsonProperty("debug") Boolean debugEnabled,
                            @JsonProperty("settings") Map<String, Object> settings,
                            @JsonProperty(AMBARI_COLLECTOR_APP_ID) String appId,
                            @JsonProperty(AMBARI_COLLECTOR_ZK_QUORUM) String zkQuorum,
                            @JsonProperty(AMBARI_COLLECTOR_ZK_CONN_TRY_COUNT) int zkConnTryCount,
                            @JsonProperty(AMBARI_COLLECTOR_ZK_CONN_TRY_INTERVAL_MS) int zkConnTryInterval,
                            @JsonProperty(AMBARI_COLLECTOR_CONN_TIMEOUT) int collectorTimeout,
                            @JsonProperty(AMBARI_COLLECTOR_PORT) int collectorPort,
                            @JsonProperty(JMX_WHITELIST_ATTRS_FILE) String jmxWhitelistAttrsFile,
                            @JsonProperty(EMIT_METRICS_SWITCH) boolean emitMetricsSwitch,
                            @JsonProperty(JMX_TYPE_NAMES_LIST) ImmutableList<String> typeNamesList
                            ) {
        super(typeNames, booleanAsNumber, debugEnabled, settings);
        args = new InfoArgs();
        args.setAmbariAppId(appId);
        args.setAmbariZkQuorum(zkQuorum);
        args.setAmbariZkConnTryCount(zkConnTryCount);
        args.setAmbariZkConnTryInterval(zkConnTryInterval);
        args.setAmbariCollectorHttpTimeout(collectorTimeout);
        args.setAmbariCollectorHttpPort(collectorPort);
        // if debug is on & log level is debug, the debug msg will be printed
        args.setDebug(debugEnabled);
        args.setJmxWhitelistAttrsFile(jmxWhitelistAttrsFile);
        args.setEmitMetrics(emitMetricsSwitch);
        if (typeNamesList == null || typeNamesList.isEmpty()) {
            typeNamesList = ImmutableList.of("type", "name");
        }
        args.setTypeNamesList(typeNamesList);
    }

    @Override
    protected void internalWrite(Server server,
                                 Query query,
                                 ImmutableList<Result> results) throws Exception {
        jmxHandler.handleJmxtransResult(results);
    }

    @Override
    public void validateSetup(Server server, Query query) throws ValidationException {
        args.setObjName(query.getObjectName().toString());
        String host = server.getHost();
        if (LOCAL_HOST.equals(host)) {
            try {
                host = Inet4Address.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                throw new ValidationException("Can not get local host name", query);
            }
        }
        LOG.info("init JmxHandler, host: " + host + ", objName: " + args.getObjName());
        jmxHandler = new JmxHandler(host, args);
    }
}

package sg.bigo.bigdata.atlas.monitor;

import com.google.common.collect.ImmutableList;

public class InfoArgs {
    /**
     * ambari appId i.e. component, specify different cluster
     */
    private String ambariAppId;
    /**
     * ambari collector zk quorum, e.g. zk1:2181,zk2:2181
     */
    private String ambariZkQuorum;
    /**
     * jmx white list file path, recommend absolute path
     */
    private String jmxWhitelistAttrsFile;
    private int ambariZkConnTryCount;
    private int ambariZkConnTryInterval;
    /**
     * the timeout of sending metric to ambari collector
     */
    private int ambariCollectorHttpTimeout;
    /**
     * the ambari collector http port, e.g. 6181
     */
    private int ambariCollectorHttpPort;
    /**
     * whether send the debug info about the process, need `export LOG_LEVEL=DEBUG`
     */
    private boolean isDebug;
    /**
     * whether send the collected metrics to collector or not
     */
    private boolean emitMetrics;
    /**
     * specify the list and order about typeName in jmx metric i.e. {'type', 'name'}
     */
    private ImmutableList<String> typeNamesList;
    /**
     * specify the objName for logging
     */
    private String objName;
    /**
     * is coordinator
     */
    private boolean isCoordinator;

    public String getAmbariAppId() {
        return ambariAppId;
    }

    public void setAmbariAppId(String ambariAppId) {
        this.ambariAppId = ambariAppId;
    }

    public String getAmbariZkQuorum() {
        return ambariZkQuorum;
    }

    public void setAmbariZkQuorum(String ambariZkQuorum) {
        this.ambariZkQuorum = ambariZkQuorum;
    }

    public int getAmbariZkConnTryCount() {
        return ambariZkConnTryCount;
    }

    public void setAmbariZkConnTryCount(int ambariZkConnTryCount) {
        this.ambariZkConnTryCount = ambariZkConnTryCount;
    }

    public int getAmbariZkConnTryInterval() {
        return ambariZkConnTryInterval;
    }

    public void setAmbariZkConnTryInterval(int ambariZkConnTryInterval) {
        this.ambariZkConnTryInterval = ambariZkConnTryInterval;
    }

    public int getAmbariCollectorHttpTimeout() {
        return ambariCollectorHttpTimeout;
    }

    public void setAmbariCollectorHttpTimeout(int ambariCollectorHttpTimeout) {
        this.ambariCollectorHttpTimeout = ambariCollectorHttpTimeout;
    }

    public int getAmbariCollectorHttpPort() {
        return ambariCollectorHttpPort;
    }

    public void setAmbariCollectorHttpPort(int ambariCollectorHttpPort) {
        this.ambariCollectorHttpPort = ambariCollectorHttpPort;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public String getJmxWhitelistAttrsFile() {
        return jmxWhitelistAttrsFile;
    }

    public void setJmxWhitelistAttrsFile(String jmxWhitelistAttrsFile) {
        this.jmxWhitelistAttrsFile = jmxWhitelistAttrsFile;
    }

    public boolean isEmitMetrics() {
        return emitMetrics;
    }

    public void setEmitMetrics(boolean emitMetrics) {
        this.emitMetrics = emitMetrics;
    }

    public ImmutableList<String> getTypeNamesList() {
        return typeNamesList;
    }

    public void setTypeNamesList(ImmutableList<String> typeNamesList) {
        this.typeNamesList = typeNamesList;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public void setCoordinator(boolean coordinator) {
        isCoordinator = coordinator;
    }

}

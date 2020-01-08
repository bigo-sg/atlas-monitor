#!/bin/bash
source /etc/profile

CURRENT_HOME="$(cd "$(dirname "$0")"/..; pwd)"
cd ${CURRENT_HOME}

source ${CURRENT_HOME}/conf/env.conf
export JMXTRANS_HOME=${CURRENT_HOME}/jmxtrans

start() {
  # sed conf
  CONF_FILE=${CURRENT_HOME}/${MONITOR_CONTENT}/conf/atlas_monitor.json

  if [ -f ${CONF_FILE} ]; then
    rm ${CONF_FILE}
  fi

  cp ${CONF_FILE}_template ${CONF_FILE}
  MONITOR_JMX_ATTRS_WHILTELIST_FILE=${CURRENT_HOME}/${MONITOR_CONTENT}/conf/metrics_whitelist
  sed -i "s,#JMX_PORT#,${MONITOR_JMX_PORT},g" ${CONF_FILE}
  sed -i "s,#AMBARI_APP_ID#,${MONITOR_AMBARI_APP_ID},g" ${CONF_FILE}
  sed -i "s/#AMBARI_ZK_QUORUM#/${MONITOR_AMBARI_ZK_QUORUM}/g" ${CONF_FILE}
  sed -i "s/#IS_COORDINATOR#/${MONITOR_IS_COORDINATOR}/g" ${CONF_FILE}
  sed -i "s,#JMX_ATTRS_WHITELIST_FILE#,${MONITOR_JMX_ATTRS_WHILTELIST_FILE},g" ${CONF_FILE}

  # start monitor
  chmod u+x ${CURRENT_HOME}/${MONITOR_CONTENT}/monitor.sh
  chmod u+x ${JMXTRANS_HOME}/jmxtrans.sh
  cmd="${CURRENT_HOME}/${MONITOR_CONTENT}/monitor.sh start ${CONF_FILE}"
  echo ${cmd}
  eval ${cmd}
}

stop() {
  # stop monitor
  chmod u+x ${CURRENT_HOME}/${MONITOR_CONTENT}/monitor.sh
  chmod u+x ${JMXTRANS_HOME}/jmxtrans.sh
  cmd="${CURRENT_HOME}/${MONITOR_CONTENT}/monitor.sh stop"
  echo ${cmd}
  eval ${cmd}
}

case $1 in
    start)
        start
    ;;
    stop)
        stop
	;;
    *)
        echo $"Usage: $0 {start|stop} [filename.json]"
    ;;
esac

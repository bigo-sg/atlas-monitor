/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sg.bigo.bigdata.atlas.monitor.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * DruidCuratorUtils
 */

public class AtlasUtils {
    private static final Logger log = LoggerFactory.getLogger(AtlasUtils.class);
    private static final ConcurrentMap<String, CuratorFramework> CACHE = new ConcurrentHashMap<String, CuratorFramework>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (CuratorFramework curator : CACHE.values()) {
                    try {
                        curator.close();
                    }
                    catch (Exception ex) {
                        log.error("Error at closing " + curator, ex);
                    }
                }
                CACHE.clear();
            }
        }));
    }

    public static CuratorFramework getCurator(String config) {
        CuratorFramework curator = CACHE.get(config);
        if (curator == null) {
            synchronized (AtlasUtils.class) {
                curator = CACHE.get(config);
                if (curator == null) {
                    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
                    curator = CuratorFrameworkFactory.builder()
                            .connectString(config)
                            .sessionTimeoutMs(5000)
                            .connectionTimeoutMs(5000)
                            .retryPolicy(retryPolicy)
                            .build();
                    curator.start();
                    CACHE.put(config, curator);
                    if (CACHE.size() > 1) {
                        log.warn("More cthan one singleton exist");
                    }
                }
            }
        }
        return curator;
    }

    public static String getActiveAddress(String config) {
        final CuratorFramework curator = getCurator(config);
        String leaderCoorZkPath = "/apache_atlas/active_server_info";
        String activeAddress = "";
        try {
            byte[] bytes = curator.getData().forPath(leaderCoorZkPath);
            activeAddress = new String(bytes, Charset.forName("UTF-8"));
        } catch (Exception e) {
            log.error("get active address failed!", e);
        }
        return activeAddress;
    }

}

package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterAppAssignResultVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.alibaba.csp.sentinel.dashboard.service.ClusterAssignServiceImpl;
import com.alibaba.csp.sentinel.dashboard.util.MachineUtils;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *  
 *
 * @author tomgs
 * @version 2020/2/24 1.0 
 */
public class ZkClusterAssignServiceImpl extends ClusterAssignServiceImpl {

    private final Logger LOGGER = LoggerFactory.getLogger(ZkClusterAssignServiceImpl.class);

    private SentinelApiClient sentinelApiClient;

    public ZkClusterAssignServiceImpl(SentinelApiClient sentinelApiClient) {
        this.sentinelApiClient = sentinelApiClient;
    }

    @Override
    public ClusterAppAssignResultVO applyAssignToApp(String app, List<ClusterAppAssignMap> clusterMap, Set<String> remainingSet) {
        AssertUtil.assertNotBlank(app, "app cannot be blank");
        AssertUtil.notNull(clusterMap, "clusterMap cannot be null");
        Set<String> failedServerSet = new HashSet<>();
        Set<String> failedClientSet = new HashSet<>();

        // Assign server and apply config.
        clusterMap.stream()
                .filter(Objects::nonNull)
                //.filter(ClusterAppAssignMap::getBelongToApp)
                .map(e -> {
                    String ip = e.getIp();
                    int commandPort = parsePort(e);
                    CompletableFuture<Void> f = new CompletableFuture<>();
                    if (e.getBelongToApp()) {
                        f = sentinelApiClient.modifyClusterMode(app, ip, commandPort, ClusterStateManager.CLUSTER_SERVER);
                    } else {
                        f.complete(null);
                    }
                    f = f.thenCompose(v -> applyServerConfigChange(app, ip, commandPort, e));
                    return Tuple2.of(e.getMachineId(), f);
                })
                .forEach(t -> handleFutureSync(t, failedServerSet));

        // Assign client of servers and apply config.
        clusterMap.parallelStream()
                .filter(Objects::nonNull)
                .forEach(e -> this.applyAllClientConfigChange(app, e, failedClientSet));

        // Unbind remaining (unassigned) machines.
        applyAllRemainingMachineSet(app, remainingSet, failedClientSet);

        return new ClusterAppAssignResultVO()
                .setFailedClientSet(failedClientSet)
                .setFailedServerSet(failedServerSet);
    }

    private void applyAllRemainingMachineSet(String app, Set<String> remainingSet, Set<String> failedSet) {
        if (remainingSet == null || remainingSet.isEmpty()) {
            return;
        }
        remainingSet.parallelStream()
                .filter(Objects::nonNull)
                .map(MachineUtils::parseCommandIpAndPort)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ipPort -> {
                    String ip = ipPort.r1;
                    int commandPort = ipPort.r2;
                    CompletableFuture<Void> f = sentinelApiClient.modifyClusterMode(app, ip, commandPort, ClusterStateManager.CLUSTER_NOT_STARTED);
                    return Tuple2.of(ip + '@' + commandPort, f);
                })
                .forEach(t -> handleFutureSync(t, failedSet));
    }

    private void applyAllClientConfigChange(String app, ClusterAppAssignMap assignMap,
                                            Set<String> failedSet) {
        Set<String> clientSet = assignMap.getClientSet();
        if (clientSet == null || clientSet.isEmpty()) {
            return;
        }
        final String serverIp = assignMap.getIp();
        final int serverPort = assignMap.getPort();
        clientSet.stream()
                .map(MachineUtils::parseCommandIpAndPort)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ipPort -> {
                    CompletableFuture<Void> f = sentinelApiClient
                            .modifyClusterMode(app, ipPort.r1, ipPort.r2, ClusterStateManager.CLUSTER_CLIENT)
                            .thenCompose(v -> sentinelApiClient.modifyClusterClientConfig(app, ipPort.r1, ipPort.r2,
                                    new ClusterClientConfig().setRequestTimeout(20)
                                            .setServerHost(serverIp)
                                            .setServerPort(serverPort)
                            ));
                    return Tuple2.of(ipPort.r1 + '@' + ipPort.r2, f);
                })
                .forEach(t -> handleFutureSync(t, failedSet));
    }

    private int parsePort(ClusterAppAssignMap assignMap) {
        return MachineUtils.parseCommandPort(assignMap.getMachineId())
                .orElse(ServerTransportConfig.DEFAULT_PORT);
    }

    private CompletableFuture<Void> applyServerConfigChange(String app, String ip, int commandPort,
                                                            ClusterAppAssignMap assignMap) {
        ServerTransportConfig transportConfig = new ServerTransportConfig()
                .setPort(assignMap.getPort())
                .setIdleSeconds(600);
        return sentinelApiClient.modifyClusterServerTransportConfig(app, ip, commandPort, transportConfig)
                .thenCompose(v -> applyServerFlowConfigChange(app, ip, commandPort, assignMap))
                .thenCompose(v -> applyServerNamespaceSetConfig(app, ip, commandPort, assignMap));
    }

    private CompletableFuture<Void> applyServerFlowConfigChange(String app, String ip, int commandPort,
                                                                ClusterAppAssignMap assignMap) {
        Double maxAllowedQps = assignMap.getMaxAllowedQps();
        if (maxAllowedQps == null || maxAllowedQps <= 0 || maxAllowedQps > 20_0000) {
            return CompletableFuture.completedFuture(null);
        }
        return sentinelApiClient.modifyClusterServerFlowConfig(app, ip, commandPort,
                new ServerFlowConfig().setMaxAllowedQps(maxAllowedQps));
    }

    private CompletableFuture<Void> applyServerNamespaceSetConfig(String app, String ip, int commandPort,
                                                                  ClusterAppAssignMap assignMap) {
        Set<String> namespaceSet = assignMap.getNamespaceSet();
        if (namespaceSet == null || namespaceSet.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return sentinelApiClient.modifyClusterServerNamespaceSet(app, ip, commandPort, namespaceSet);
    }

    private void handleFutureSync(Tuple2<String, CompletableFuture<Void>> t, Set<String> failedSet) {
        try {
            t.r2.get(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            if (ex instanceof ExecutionException) {
                LOGGER.error("Request for <{}> failed", t.r1, ex.getCause());
            } else {
                LOGGER.error("Request for <{}> failed", t.r1, ex);
            }
            failedSet.add(t.r1);
        }
    }
}

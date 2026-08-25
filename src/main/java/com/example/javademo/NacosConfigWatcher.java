package com.example.javademo;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Nacos 配置中心客户端。
 *
 * <p>通过环境变量连接 Nacos（平台部署时注入，见 local-k8s-test-nacos-env ConfigMap）：
 * <ul>
 *   <li>NACOS_SERVER_ADDR：Nacos 地址（如 10.100.113.74:8848），缺省 localhost:8848</li>
 *   <li>NACOS_GROUP：分组，缺省 DEFAULT_GROUP</li>
 *   <li>NACOS_NAMESPACE：命名空间 ID，可选</li>
 * </ul>
 *
 * <p>拉取 dataId=demo.yaml 的配置，解析为键值对并缓存；每 30s 主动刷新，
 * 同时注册 Nacos 监听器实现配置变更即时推送。
 */
@Component
public class NacosConfigWatcher {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigWatcher.class);

    /** 配置 dataId（与平台环境 local-k8s-test 的 nacos_config.configs 一致） */
    private static final String DATA_ID = "demo.yaml";

    @Value("${nacos.server.addr:localhost:8848}")
    private String serverAddr;

    @Value("${nacos.group:DEFAULT_GROUP}")
    private String group;

    @Value("${nacos.namespace:}")
    private String namespace;

    private final Map<String, String> config = new ConcurrentHashMap<>();
    private ConfigService configService;
    private volatile String rawContent = "";

    @PostConstruct
    public void init() {
        // 平台部署注入的环境变量优先，其次本地配置
        String envAddr = System.getenv("NACOS_SERVER_ADDR");
        if (envAddr != null && !envAddr.isBlank()) {
            serverAddr = envAddr.trim();
        }
        String envGroup = System.getenv("NACOS_GROUP");
        if (envGroup != null && !envGroup.isBlank()) {
            group = envGroup.trim();
        }
        String envNs = System.getenv("NACOS_NAMESPACE");
        if (envNs != null && !envNs.isBlank()) {
            namespace = envNs.trim();
        }

        Properties props = new Properties();
        props.setProperty("serverAddr", serverAddr);
        if (namespace != null && !namespace.isBlank()) {
            props.setProperty("namespace", namespace);
        }
        try {
            configService = NacosFactory.createConfigService(props);
            refresh();
            // 注册监听：Nacos 配置变更时即时刷新
            configService.addListener(DATA_ID, group, new com.alibaba.nacos.api.config.listener.Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("[nacos] config changed for {}/{}", DATA_ID, group);
                    apply(configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return null; // 使用 Nacos 内部线程池
                }
            });
            log.info("[nacos] watcher started server={} group={} namespace='{}' dataId={}", serverAddr, group, namespace, DATA_ID);
        } catch (Exception e) {
            log.error("[nacos] init failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 30_000)
    public void refresh() {
        if (configService == null) {
            return;
        }
        try {
            String content = configService.getConfig(DATA_ID, group, 3000);
            apply(content);
        } catch (Exception e) {
            log.warn("[nacos] refresh failed: {}", e.getMessage());
        }
    }

    private void apply(String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        rawContent = content;
        config.clear();
        for (String line : content.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int idx = trimmed.indexOf(':');
            if (idx <= 0) {
                continue;
            }
            String key = trimmed.substring(0, idx).trim();
            String value = trimmed.substring(idx + 1).trim();
            config.put(key, value);
        }
        log.info("[nacos] applied {} keys from {}", config.size(), DATA_ID);
    }

    public Map<String, String> snapshot() {
        return new ConcurrentHashMap<>(config);
    }

    public String raw() {
        return rawContent;
    }

    public String serverAddr() {
        return serverAddr;
    }

    public String group() {
        return group;
    }

    @PreDestroy
    public void shutdown() {
        if (configService != null) {
            try {
                configService.shutDown();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
    }
}

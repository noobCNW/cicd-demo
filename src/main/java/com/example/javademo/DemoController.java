package com.example.javademo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 展示 Nacos 配置读取结果：
 * GET /        —— 汇总信息（服务名、Nacos 连接信息、配置键值）
 * GET /config  —— demo.yaml 原始内容
 * GET /health  —— 健康检查（平台部署 readiness/liveness 探针）
 */
@RestController
public class DemoController {

    private final NacosConfigWatcher watcher;

    public DemoController(NacosConfigWatcher watcher) {
        this.watcher = watcher;
    }

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("service", "javademo");
        out.put("nacos", watcher.serverAddr() + "/" + watcher.group());
        out.put("config", watcher.snapshot());
        return out;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("raw", watcher.raw());
        out.put("parsed", watcher.snapshot());
        return out;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("status", "UP");
        out.put("nacos", watcher.serverAddr());
        out.put("config_keys", String.valueOf(watcher.snapshot().size()));
        return out;
    }
}

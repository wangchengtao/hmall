package com.hmall.gateway.route;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.hmall.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final RouteDefinitionWriter writer;

    private final NacosConfigManager nacosConfigManager;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";

    private Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        updateConfigInfo(configInfo);
                    }
                });

        updateConfigInfo(configInfo);
    }

    private void updateConfigInfo(String configInfo) {
        log.info("监听到路由变更 {}", configInfo);
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);

        // 更新前先清理路由
        routeIds.forEach(routeId -> {
            writer.delete(Mono.just(routeId)).subscribe();
        });

        routeIds.clear();

        // 判断是否有新的路由要更新
        if (CollUtils.isEmpty(routeDefinitions)) {
            // 无新路由配置, 直接结束
            return;
        }

        // 更新路由
        routeDefinitions.forEach(routeDefinition -> {
            writer.save(Mono.just(routeDefinition)).subscribe();
            // 记录路由 id, 方便将来删除
            routeIds.add(routeDefinition.getId());
        });
    }

}

package me.giraffetree.raftjavaserver.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "node")
public class NodeConfiguration {

    private String id;

    private Map<String, String> idMap;

}

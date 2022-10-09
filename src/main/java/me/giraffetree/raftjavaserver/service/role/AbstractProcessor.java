package me.giraffetree.raftjavaserver.service.role;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractProcessor {

    protected volatile long term = 0;

    /**
     * key: nodeId
     * value: 节点的 host:port
     */
    protected Map<String, String> nodeHostAndPortMap;
    /**
     * 节点id 列表
     */
    protected List<String> nodeIdList;

    /**
     * 当前服务对应的节点id
     */
    protected String currentNodeId;


    public AbstractProcessor(Map<String, String> nodeHostAndPortMap, String currentNodeId) {
        this.nodeHostAndPortMap = nodeHostAndPortMap;
        this.nodeIdList = nodeHostAndPortMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        this.currentNodeId = currentNodeId;
    }

}

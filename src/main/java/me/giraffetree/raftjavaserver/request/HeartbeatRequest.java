package me.giraffetree.raftjavaserver.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HeartbeatRequest {

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 任期
     */
    private long term;
}

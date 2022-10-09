package me.giraffetree.raftjavaserver.service.role;

import me.giraffetree.raftjavaserver.client.RaftClient;
import me.giraffetree.raftjavaserver.request.HeartbeatRequest;
import me.giraffetree.raftjavaserver.request.VoteRequest;
import me.giraffetree.raftjavaserver.response.HeartbeatResponse;
import me.giraffetree.raftjavaserver.response.VoteResponse;
import me.giraffetree.raftjavaserver.service.IRaft;
import me.giraffetree.raftjavaserver.service.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LeaderProcessor extends AbstractProcessor implements IRaft {

    private StatusManager statusManager;
    private final static Logger logger = LoggerFactory.getLogger(LeaderProcessor.class);
    public LeaderProcessor(Map<String,String> nodeMap, String currentServerId, StatusManager statusManager) {
        super(nodeMap, currentServerId);
        this.statusManager = statusManager;
    }

    private long heartBeatIntervalMills = 1000L;
    private long nextHeartBeatTimestamp;

    @Override
    public void beforeProcess() {
        // 向每个 node 发送心跳
        nextHeartBeatTimestamp = System.currentTimeMillis() + heartBeatIntervalMills;
        sendHeartbeatToNodes();
    }

    @Override
    public void process() {
        long currentTimeMillis = System.currentTimeMillis();
        if (nextHeartBeatTimestamp < currentTimeMillis) {
            return;
        }
        nextHeartBeatTimestamp = currentTimeMillis + heartBeatIntervalMills;
        sendHeartbeatToNodes();
    }

    private void sendHeartbeatToNodes() {
        for (Map.Entry<String, String> entry : nodeHostAndPortMap.entrySet()) {
            String nodeId = entry.getKey();
            if (nodeId.equals(currentNodeId)) {
                continue;
            }
            String hostAndPort = entry.getValue();
            try {
                HeartbeatResponse response = RaftClient.heartbeat(hostAndPort, new HeartbeatRequest(currentNodeId, term));
                logger.info("leader:{} heartbeat to {} success - response:{} ", currentNodeId, nodeId, response);
            } catch (Exception e) {
                // ignore
                logger.info("heartbeat to {} error:{} ", nodeId, e.getMessage());
            }
        }
    }

    @Override
    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        // 收到 term 大于自身的 term 则转为 follower
        long remoteTerm = request.getTerm();
        if (remoteTerm > term) {
            statusManager.changeToFollower();
            logger.info("leader:{} receive heartbeat:{} changeToLeader",request, currentNodeId);
        }
        return new HeartbeatResponse(term);
    }

    @Override
    public VoteResponse vote(VoteRequest request) {
        long remoteTerm = request.getTerm();
        if (remoteTerm > term) {
            statusManager.changeToFollower();
            term = remoteTerm;
            logger.info("leader:{} receive vote:{} changeToLeader", request, currentNodeId);
            return new VoteResponse(true, remoteTerm);
        }
        return new VoteResponse(false, term);
    }

    @Override
    public void beforeChangeRole() {

    }
}

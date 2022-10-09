package me.giraffetree.raftjavaserver.service.role;

import me.giraffetree.raftjavaserver.request.HeartbeatRequest;
import me.giraffetree.raftjavaserver.request.VoteRequest;
import me.giraffetree.raftjavaserver.response.HeartbeatResponse;
import me.giraffetree.raftjavaserver.response.VoteResponse;
import me.giraffetree.raftjavaserver.service.IRaft;
import me.giraffetree.raftjavaserver.service.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FollowerProcessor extends AbstractProcessor implements IRaft {

    private final static Logger logger = LoggerFactory.getLogger(FollowerProcessor.class);

    private StatusManager statusManager;

    /**
     * 默认心跳间隔, 单位:毫秒
     */
    private int heartbeatIntervalMills = 1000;
    /**
     * leader过期时间的最小值, 单位:毫秒
     */
    private int leftLeaderHeartbeatLatencyMills = (heartbeatIntervalMills << 1) + (heartbeatIntervalMills >> 1);
    /**
     * leader过期时间的最大值, 单位:毫秒
     */
    private int rightLeaderHeartbeatLatencyMills = (heartbeatIntervalMills << 1) + heartbeatIntervalMills;

    private volatile long currentLeaderHeartbeatLatencyMills;

    /**
     * leader ip:port 组合
     * 可能为 null
     */
    private volatile String currentLeaderServerId;

    private volatile long latestHeartbeatMills;

    private volatile boolean start = false;

    public FollowerProcessor(Map<String, String> nodeMap, String currentServerId, StatusManager statusManager, String currentLeaderServerId) {
        super(nodeMap, currentServerId);
        this.statusManager = statusManager;
        this.currentLeaderServerId = currentLeaderServerId;
    }

    @Override
    public void beforeProcess() {
        if (start) {
            return;
        }
        latestHeartbeatMills = System.currentTimeMillis();
        currentLeaderHeartbeatLatencyMills = getNextMills();
        start = true;
    }

    @Override
    public void process() {
        // 1. 心跳未过期 -> 继续
        // 2. 心跳过期 ->  切换到 candidate
        checkLeaderAvailable();
    }

    private void checkLeaderAvailable() {
        if (!start) {
            return;
        }
        boolean delay = (System.currentTimeMillis() - latestHeartbeatMills) > currentLeaderHeartbeatLatencyMills;
        if (!delay) {
            return;
        }
        // 如果延迟了, 则需要切换状态 -> candidate
        try {
            statusManager.changeToCandidate();
            logger.info("follower:{} changeToCandidate", currentNodeId);
        } catch (Exception e) {
            // 这里的错误可以忽略
            logger.error("checkLeaderAvailable changeToCandidate error");
        }
    }

    private long getNextMills() {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        return current.nextLong(leftLeaderHeartbeatLatencyMills, rightLeaderHeartbeatLatencyMills);
    }

    @Override
    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        String serverId = request.getNodeId();
        if (currentLeaderServerId.equals(serverId)) {
            latestHeartbeatMills = System.currentTimeMillis();
        }
        return new HeartbeatResponse(term);
    }

    @Override
    public VoteResponse vote(VoteRequest request) {
        long remoteTerm = request.getTerm();
        if (remoteTerm > term) {
            term = remoteTerm;
            return new VoteResponse(true, term);
        }
        return new VoteResponse(false, remoteTerm);
    }

    @Override
    public void beforeChangeRole() {
        start = false;
    }

}

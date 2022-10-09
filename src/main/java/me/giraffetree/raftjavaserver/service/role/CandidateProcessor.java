package me.giraffetree.raftjavaserver.service.role;

import me.giraffetree.raftjavaserver.client.RaftClient;
import me.giraffetree.raftjavaserver.dto.VoteNodeDetailDTO;
import me.giraffetree.raftjavaserver.request.HeartbeatRequest;
import me.giraffetree.raftjavaserver.request.VoteRequest;
import me.giraffetree.raftjavaserver.response.HeartbeatResponse;
import me.giraffetree.raftjavaserver.response.VoteResponse;
import me.giraffetree.raftjavaserver.service.IRaft;
import me.giraffetree.raftjavaserver.service.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CandidateProcessor extends AbstractProcessor implements IRaft {

    private final static Logger logger = LoggerFactory.getLogger(CandidateProcessor.class);

    private final static long defaultElectionTimeOutMills = 5000L;
    private long leftElectionLatencyMills;
    private long rightElectionLatencyMills;

    /**
     * 本次选举超时时间, 毫秒时间戳
     */
    private long currentElectionMaxLatencyMillTimestamp;

    /**
     * key: serverId
     * value: 投票节点 node 对应的详细信息
     */
    private ConcurrentHashMap<String, VoteNodeDetailDTO> serverResponseMap = new ConcurrentHashMap<>();

    private StatusManager statusManager;

    public CandidateProcessor(Map<String, String> nodeMap, String currentServerId, StatusManager statusManager) {
        super(nodeMap, currentServerId);
        this.statusManager = statusManager;
    }

    @Override
    public void beforeProcess() {
        resetElectionTimeout();
        currentElectionMaxLatencyMillTimestamp = System.currentTimeMillis() + getNextMills();
        // 发送投票请求
        vote();
    }

    private void resetElectionTimeout() {
        leftElectionLatencyMills = defaultElectionTimeOutMills;
        rightElectionLatencyMills = defaultElectionTimeOutMills + (defaultElectionTimeOutMills >> 1);
    }

    private long getNextMills() {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        return current.nextLong(leftElectionLatencyMills, rightElectionLatencyMills);
    }

    @Override
    public void process() {
        // 这里只处理选举过期的情况, 则 term + 1 -> 准备下一次选举
        // 对于选举未过期且大多数返回同意的情况, 直接处理 -> switch to leader

        // 选举未过期
        if (currentElectionMaxLatencyMillTimestamp - System.currentTimeMillis() > 0) {
            int effectSize = serverResponseMap.size();
            if (((effectSize << 1) - 1) >= nodeIdList.size()) {
                // 大于一半的节点同意了 -> switch to leader
                statusManager.changeToLeader();
                logger.info("candidate:{} changeToLeader", currentNodeId);
            }
            return;
        }
        // 选举过期, 准备下一次选举
        long nextMills = getNextMills();
        currentElectionMaxLatencyMillTimestamp = System.currentTimeMillis() + nextMills;

        term++;
        serverResponseMap.clear();
        logger.info("candidate:{} prepare next vote", currentNodeId);
        // 发送 vote 请求
        vote();
    }

    private void vote() {
        // 发送 vote 请求
        VoteRequest voteRequest = new VoteRequest(currentNodeId, term);

        for (String nodeId : nodeIdList) {
            if (currentNodeId.equals(nodeId)) {
                serverResponseMap.put(nodeId, new VoteNodeDetailDTO(term));
                return;
            }
            try {
                VoteResponse vote = RaftClient.vote(nodeHostAndPortMap.get(nodeId), voteRequest);
                long remoteTerm = vote.getTerm();
                Boolean support = vote.getSupport();
                if (support && remoteTerm == term) {
                    serverResponseMap.put(nodeId, new VoteNodeDetailDTO(term));
                } else if (remoteTerm > term) {
                    serverResponseMap.clear();
                    term = remoteTerm;
                    break;
                }
                // todo: 这里没有处理 remoteTerm < term 的情况
            } catch (Exception e) {
                logger.info("vote nodeId:{} error:{}", nodeId, e.getMessage());
            }
        }
    }


    @Override
    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        // 3. 收到心跳/rpc请求, 作为 follower ->  switch to follower
        long remoteTerm = request.getTerm();
        if (remoteTerm > term) {
            statusManager.changeToFollower();
            logger.info("candidate:{} receive heartbeat:{} changeToFollower", currentNodeId, request);
        }
        return new HeartbeatResponse(term);
    }

    @Override
    public VoteResponse vote(VoteRequest request) {
        // 3. 收到心跳/rpc请求, 作为 follower ->  switch to follower
        long remoteTerm = request.getTerm();
        if (remoteTerm > term) {
            term = remoteTerm;
            statusManager.changeToFollower();
            logger.info("candidate:{} receive vote:{} changeToFollower", currentNodeId, request);
            return new VoteResponse(true, term);
        }
        return new VoteResponse(false, term);
    }

    @Override
    public void beforeChangeRole() {
        serverResponseMap.clear();
    }
}

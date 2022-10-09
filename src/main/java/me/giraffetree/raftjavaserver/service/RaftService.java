package me.giraffetree.raftjavaserver.service;

import me.giraffetree.raftjavaserver.configuration.NodeConfiguration;
import me.giraffetree.raftjavaserver.enums.StatusEnum;
import me.giraffetree.raftjavaserver.request.HeartbeatRequest;
import me.giraffetree.raftjavaserver.request.VoteRequest;
import me.giraffetree.raftjavaserver.response.HeartbeatResponse;
import me.giraffetree.raftjavaserver.response.VoteResponse;
import me.giraffetree.raftjavaserver.service.role.CandidateProcessor;
import me.giraffetree.raftjavaserver.service.role.FollowerProcessor;
import me.giraffetree.raftjavaserver.service.role.LeaderProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class RaftService {
    @Value("${node.id}")
    private String serverId;

    @Resource
    private NodeConfiguration nodeConfiguration;

    @Resource
    private StatusManager statusManager;

    private StatusEnum latestStatus = null;
    private IRaft raft;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void afterConstruct() {
        executorService.execute(this::loop);
    }

    private void loop() {
        while (true) {
            StatusEnum currentStatus = statusManager.getCurrentStatus();
            if (!currentStatus.equals(latestStatus)) {
                raft = getProcessor(currentStatus);
                raft.beforeProcess();
                latestStatus = currentStatus;
            }
            raft.process();
            StatusEnum currentStatusAfterProcess = statusManager.getCurrentStatus();
            if (!currentStatusAfterProcess.equals(currentStatus)) {
                raft.beforeChangeRole();
            }
        }
    }

    public VoteResponse vote(VoteRequest request) {
        StatusEnum currentStatus = statusManager.getCurrentStatus();
        IRaft processor = getProcessor(currentStatus);
        return processor.vote(request);
    }

    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        StatusEnum currentStatus = statusManager.getCurrentStatus();
        IRaft processor = getProcessor(currentStatus);
        return processor.heartbeat(request);
    }

    private IRaft getProcessor(StatusEnum currentStatus) {
        Map<String, String> idMap = nodeConfiguration.getIdMap();
        switch (currentStatus) {
            case FOLLOWER:
                return new FollowerProcessor(idMap, serverId, statusManager, null);
            case CANDIDATE:
                return new CandidateProcessor(idMap, serverId, statusManager);
            case LEADER:
                return new LeaderProcessor(idMap, serverId, statusManager);
            default:
                throw new IllegalArgumentException("getProcessor 未知状态");
        }
    }

}

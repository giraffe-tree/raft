package me.giraffetree.raftjavaserver.controller;


import me.giraffetree.raftjavaserver.request.HeartbeatRequest;
import me.giraffetree.raftjavaserver.request.VoteRequest;
import me.giraffetree.raftjavaserver.response.BaseResponse;
import me.giraffetree.raftjavaserver.response.HeartbeatResponse;
import me.giraffetree.raftjavaserver.response.VoteResponse;
import me.giraffetree.raftjavaserver.service.RaftService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/raft")
@RestController
public class RaftController {

    @Resource
    private RaftService raftService;

    @PostMapping("/heartbeat")
    public BaseResponse<HeartbeatResponse> heartbeat(@RequestBody HeartbeatRequest request) {
        HeartbeatResponse heartbeatResponse = raftService.heartbeat(request);
        return BaseResponse.success(heartbeatResponse);
    }

    @PostMapping("/vote")
    public BaseResponse<VoteResponse> requestVote(@RequestBody VoteRequest voteRequest) {
        VoteResponse vote = raftService.vote(voteRequest);
        return BaseResponse.success(vote);
    }

}

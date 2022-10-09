package me.giraffetree.raftjavaserver.service;

import me.giraffetree.raftjavaserver.request.HeartbeatRequest;
import me.giraffetree.raftjavaserver.request.VoteRequest;
import me.giraffetree.raftjavaserver.response.HeartbeatResponse;
import me.giraffetree.raftjavaserver.response.VoteResponse;

public interface IRaft {


    /**
     * 初始化
     */
    void beforeProcess();

    /**
     * 检查心跳时间, 选举时间, 并做出相关相应
     */
    default void process() {

    }

    /**
     * 处理心跳
     *
     * @param request 心跳请求
     */
    HeartbeatResponse heartbeat(HeartbeatRequest request);

    /**
     * 处理投票请求
     *
     * @param request 处理投票请求
     * @return 投票结果
     */
    VoteResponse vote(VoteRequest request);


    /**
     * 在变更角色之前
     */
    void beforeChangeRole();

}

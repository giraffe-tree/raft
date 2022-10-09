package me.giraffetree.raftjavaserver.service;

import me.giraffetree.raftjavaserver.enums.StatusEnum;
import org.springframework.stereotype.Service;

@Service
public class StatusManager {

    /**
     * 状态
     * {@link me.giraffetree.raftjavaserver.enums.StatusEnum}
     */
    private volatile int currentStatus = 0;

    public void changeToCandidate() {
        if (StatusEnum.FOLLOWER.getStatus() == currentStatus) {
            currentStatus = StatusEnum.CANDIDATE.getStatus();
            return;
        }
        throw new IllegalArgumentException("changeToCandidate 状态错误 - 当前状态:" + currentStatus);
    }

    public void changeToLeader() {
        if (StatusEnum.CANDIDATE.getStatus() == currentStatus) {
            currentStatus = StatusEnum.LEADER.getStatus();
            return;
        }
        throw new IllegalArgumentException("changeToLeader 状态错误 - 当前状态:" + currentStatus);
    }

    public void changeToFollower() {
        currentStatus = StatusEnum.FOLLOWER.getStatus();
    }

    /**
     * 检查状态是否符合预期
     *
     * @param statusEnum 期望状态
     * @return 是否符合预期
     */
    public boolean checkStatus(StatusEnum statusEnum) {
        return statusEnum.getStatus() == currentStatus;
    }

    /**
     * 获取当前状态
     *
     * @return 当前状态
     */
    public StatusEnum getCurrentStatus() {
        for (StatusEnum statusEnum : StatusEnum.values()) {
            if (currentStatus == statusEnum.getStatus()) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("未知状态错误");
    }

}

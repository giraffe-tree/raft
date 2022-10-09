package me.giraffetree.raftjavaserver.enums;

public enum StatusEnum {

    /**
     * 跟随者, 默认状态
     */
    FOLLOWER(0),
    /**
     * 候选者
     */
    CANDIDATE(1),
    /**
     * leader
     */
    LEADER(2),
    ;

    private final int status;

    StatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

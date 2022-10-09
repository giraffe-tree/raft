package me.giraffetree.raftjavaserver.enums;

public enum ErrorEnum {

    OK(0, "ok"),
    DEFAULT_ERROR(100000, "默认错误"),
    ;

    private final int code;

    private final String msg;

    ErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

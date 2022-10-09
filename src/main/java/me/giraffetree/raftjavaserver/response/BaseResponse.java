package me.giraffetree.raftjavaserver.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.giraffetree.raftjavaserver.enums.ErrorEnum;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BaseResponse<T> {
    /**
     * 返回码
     */
    private int code;

    private String msg;

    private T data;

    public BaseResponse(T data) {
        this.data = data;
        this.code = 0;
        this.msg = "OK";
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data);
    }

    public static <T> BaseResponse<T> failed(int code, String msg) {
        return new BaseResponse<>(code, msg, null);
    }

    public static <T> BaseResponse<T> failed(ErrorEnum error) {
        return new BaseResponse<>(error.getCode(), error.getMsg(), null);
    }

    public static <T> BaseResponse<T> failed(ErrorEnum error, String msg) {
        return new BaseResponse<>(error.getCode(), msg, null);
    }

}
package me.giraffetree.raftjavaserver.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import me.giraffetree.raftjavaserver.enums.ErrorEnum;
import me.giraffetree.raftjavaserver.request.HeartbeatRequest;
import me.giraffetree.raftjavaserver.request.VoteRequest;
import me.giraffetree.raftjavaserver.response.BaseResponse;
import me.giraffetree.raftjavaserver.response.HeartbeatResponse;
import me.giraffetree.raftjavaserver.response.VoteResponse;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;

public class RaftClient {

    private final static String httpPrefix = "http://";
    private final static String votePath = "/raft/vote";
    private final static String heartbeatPath = "/raft/heartbeat";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofMillis(1000))
            .readTimeout(Duration.ofMillis(2000))
            .build();

    public static VoteResponse vote(String hostAndPort, VoteRequest voteRequest) {
        RequestBody body = RequestBody.create(JSON.toJSONString(voteRequest).getBytes());
        Request request = new Request.Builder()
                .url(httpPrefix + hostAndPort + votePath)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new RuntimeException("vote error " + hostAndPort + " - " + voteRequest.toString());
            }
            TypeReference<BaseResponse<VoteResponse>> typeReference = new TypeReference<BaseResponse<VoteResponse>>() {};
            BaseResponse<VoteResponse> result = typeReference.parseObject(responseBody.bytes());
            if (result.getCode() == ErrorEnum.OK.getCode()) {
                return result.getData();
            }
            throw new RuntimeException(result.getMsg());
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    public static HeartbeatResponse heartbeat(String hostAndPort, HeartbeatRequest heartbeatRequest) {
        RequestBody body = RequestBody.create(JSON.toJSONString(heartbeatRequest).getBytes());
        Request request = new Request.Builder()
                .url(httpPrefix + hostAndPort + heartbeatPath)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new RuntimeException("heartbeat error " + hostAndPort + " - " + heartbeatRequest.toString());
            }
            TypeReference<BaseResponse<HeartbeatResponse>> typeReference = new TypeReference<BaseResponse<HeartbeatResponse>>() {};
            BaseResponse<HeartbeatResponse> result = typeReference.parseObject(responseBody.bytes());
            if (result.getCode() == ErrorEnum.OK.getCode()) {
                return result.getData();
            }
            throw new RuntimeException(result.getMsg());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

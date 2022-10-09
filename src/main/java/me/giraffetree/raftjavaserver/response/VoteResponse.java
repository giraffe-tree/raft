package me.giraffetree.raftjavaserver.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VoteResponse {

    /**
     * 是否支持
     */
    private Boolean support = false;

    /**
     * 是否支持
     */
    private long term;

}

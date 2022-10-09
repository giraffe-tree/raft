package me.giraffetree.raftjavaserver.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class HeartbeatResponse {

    private long term;

}

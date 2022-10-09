

在 Raft 里，Leader 会定期向 Follower 发送心跳。

Follower 也会设置一个超时时间，如果超过超时时间没有接收到心跳，那么它就会认为 Leader 可能挂掉了，就会发起一轮新的 Leader 选举。

Follower 发起选举，会做两个动作。第一个，是先给自己投一票；第二个，是向所有其他的 Follower 发起一个 RequestVote 请求，也就是要求那些 Follower 为自己投票。这个时候，Follower 的角色，就转变成了 Candidate。在每一个 RequestVote 的请求里，除了有发起投票的服务器信息之外，还有一个任期（Term）字段。这个字段，本质上是一个 Leader 的“版本信息”或者说是“逻辑时钟”。

每个 Follower，在本地都会保留当前 Leader 是哪一个任期的。当它要发起投票的时候，会把任期自增 1，和请求一起发出去。

其他 Follower 在接收到 RequestVote 请求的时候，会去对比请求里的任期和本地的任期。如果请求的任期更大，那么它会投票给这个 Candidate。不然，这个请求会被拒绝掉。
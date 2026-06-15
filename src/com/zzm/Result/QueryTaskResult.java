package com.zzm.Result;

import com.zzm.TaskModel;

/**
 * 查询任务操作的结果封装。
 * 用状态枚举区分查询成功、任务不存在和 ID 不合法。
 */
public class QueryTaskResult {
    /**
     * 查询任务可能出现的结果状态。
     */
    public enum queryTaskStatus {
        ILLEGAL_ID, // 输入的任务 ID 不合法
        NOT_FOUND, // 没有找到对应任务
        SUCCESS // 查询成功
    }

    public queryTaskStatus status;
    public TaskModel task;

    public QueryTaskResult(queryTaskStatus status, TaskModel task) {
        this.status = status;
        this.task = task;
    }

    /** 返回查询到的任务对象，失败时为 null。 */
    public TaskModel getTask() {
        return task;
    }

    /** 返回查询任务操作的状态。 */
    public queryTaskStatus getStatus() {
        return status;
    }
}

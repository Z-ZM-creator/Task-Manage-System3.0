package com.zzm.Result;

import com.zzm.TaskModel;

/**
 * 删除任务操作的结果封装。
 * 用状态枚举表达删除成功、任务不存在或 ID 不合法。
 */
public class RemoveTaskResult {
    /**
     * 删除任务可能出现的结果状态。
     */
    public enum removeTaskResult {
        SUCCESS, // 删除成功
        TASK_NOT_FOUND, // 任务不存在
        ILLEGAL_ID // 输入的任务 ID 不合法
    }

    private removeTaskResult status;
    private TaskModel task;

    public RemoveTaskResult(removeTaskResult status, TaskModel task) {
        this.status = status;
        this.task = task;
    }

    /** 返回删除任务操作的状态。 */
    public removeTaskResult getStatus() {
        return status;
    }

    /** 返回被删除的任务对象，失败时为 null。 */
    public TaskModel getTask() {
        return task;
    }
}

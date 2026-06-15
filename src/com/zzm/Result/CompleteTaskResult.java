package com.zzm.Result;

import com.zzm.TaskModel;

/**
 * 完成任务操作的结果封装。
 * 用状态枚举表达完成成功、任务不存在、任务已完成或 ID 不合法。
 */
public class CompleteTaskResult {
    /**
     * 完成任务可能出现的结果状态。
     */
    public enum completeTaskResult {
        SUCCESS, // 完成成功
        TASK_NOT_FOUND, // 任务不存在
        FAIL_ALREADY_COMPLETE, // 任务已经完成，不能重复完成
        ILLEGAL_ID // 输入的任务 ID 不合法
    }

    public completeTaskResult status;
    public TaskModel task;

    public CompleteTaskResult(completeTaskResult status, TaskModel task) {
        this.status = status;
        this.task = task;
    }

    /** 返回完成任务操作的状态。 */
    public completeTaskResult getStatus() {
        return status;
    }

    /** 返回被完成的任务对象，失败时可能为 null。 */
    public TaskModel getTask() {
        return task;
    }
}

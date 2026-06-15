package com.zzm.Result;

import com.zzm.TaskModel;

/**
 * 创建任务操作的结果封装。
 * 通过状态枚举说明创建是否成功，并在成功时携带新建任务对象。
 */
public class CreateTaskResult {
    /**
     * 创建任务可能出现的结果状态。
     */
    public enum createTaskStatus {
        ILLEGAL_NAME, // 任务名称为空或不合法
        ILLEGAL_TIME, // 时间格式或时间范围不合法
        SUCCESS // 创建成功
    }

    public createTaskStatus status;
    public TaskModel task;

    public CreateTaskResult(createTaskStatus status, TaskModel task) {
        this.status = status;
        this.task = task;
    }

    /** 返回创建成功后的任务对象，失败时为 null。 */
    public TaskModel getTask() {
        return task;
    }

    /** 返回创建任务操作的状态。 */
    public createTaskStatus getStatus() {
        return status;
    }
}

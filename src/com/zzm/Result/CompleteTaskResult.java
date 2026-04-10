package com.zzm.Result;
import com.zzm.TaskModel;
public class CompleteTaskResult {
    public enum completeTaskResult{
        SUCCESS,//成功
        TASK_NOT_FOUND,//任务不存在
        FAIL_ALREADY_COMPLETE,//任务已经完成
        ILLEGAL_ID//ID输入不合法
    }
    public completeTaskResult status;
    public TaskModel task;
    public CompleteTaskResult(completeTaskResult status,TaskModel task){
        this.status = status;
        this.task = task;
    }
    public completeTaskResult getStatus(){
        return status;
    }
    public TaskModel getTask(){
        return task;
    }


}

package com.zzm.Result;
import com.zzm.TaskModel;
public class RemoveTaskResult {
    public enum removeTaskResult{
        SUCCESS,//成功
        TASK_NOT_FOUND,//任务不存在
        ILLEGAL_ID//ID输入不合法
    }
    private removeTaskResult status;
    private TaskModel task;
    public RemoveTaskResult(removeTaskResult status,TaskModel task){
        this.status = status;
        this.task = task;
    }
    public removeTaskResult getStatus(){
        return status;
    }
    public TaskModel getTask(){
        return task;
    }
}

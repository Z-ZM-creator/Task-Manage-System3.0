package com.zzm.Result;
import com.zzm.TaskModel;

public class QueryTaskResult {
    public enum queryTaskStatus{
        ILLEGAL_ID,//ID输入不合法
        NOT_FOUND,//未找到对应ID的任务
        SUCCESS//成功
    }
    public queryTaskStatus status;
    public TaskModel task;
    public QueryTaskResult(queryTaskStatus status,TaskModel task){
        this.status=status;
        this.task=task;
    }
    public TaskModel getTask(){
        return task;
    }
    public queryTaskStatus getStatus(){
        return status;
    }
}

package com.zzm.Result;
import com.zzm.TaskModel;

import java.util.Set;

public class CreateTaskResult {
    public enum createTaskStatus{
        ILLEGAL_NAME,//姓名输入不合法
        ILLEGAL_TIME,//时间格式不对
        SUCCESS//成功
    }
    public createTaskStatus status;
    public TaskModel task;
    public CreateTaskResult(createTaskStatus status,TaskModel task){
        this.status=status;
        this.task=task;
    }

   public TaskModel getTask(){
       return task;
   }
   public createTaskStatus getStatus(){
       return status;
   }
}

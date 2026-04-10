package com.zzm;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class TaskModel {
    public enum taskStatus{
        TO_DO,
        IN_PROGRESS,
        COMPLETED
    }
    private String taskId;
    private static long nextId=0L;
    private String taskName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    taskStatus status;
    DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");
    //LocalDateTime getStartTime;
    //LocalDateTime getEndTime;
    public TaskModel (String taskName,String startTimestr,String endTimestr){
        this.taskId=String.valueOf(nextId++);
        this.taskName=taskName;
        this.startTime=LocalDateTime.parse(startTimestr,formatter);
        this.endTime=LocalDateTime.parse(endTimestr,formatter);
        this.status=taskStatus.TO_DO;
    }

    public TaskModel(String taskId,String taskName,String startTimestr,String endTimestr){
        this.taskId=taskId;
        this.taskName=taskName;
        this.startTime=LocalDateTime.parse(startTimestr,formatter);
        this.endTime=LocalDateTime.parse(endTimestr,formatter);
        this.status=taskStatus.TO_DO;
    }
    public String getTaskId(){
        return taskId;
    }
    //getName返回name名称
    public String getName(){
        return taskName;
    }

    public LocalDateTime getStartTime(){
        return startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public static void setNextId(Long nextid){
        nextId=nextid;
    }
    @Override
    public String toString(){
        return "TaskId:"+taskId+" TaskName:"+taskName+" StartTime:"+startTime.format(formatter)+" EndTime:"+endTime.format(formatter)+" Status:"+status;
    }
}

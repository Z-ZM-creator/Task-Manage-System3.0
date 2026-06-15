package com.zzm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskModel {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");

    public enum taskStatus {
        TO_DO,
        IN_PROGRESS,
        COMPLETED
    }

    private String taskId;
    private static long nextId = 0L;
    private String taskName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    taskStatus status;

    public TaskModel(String taskName, String startTimestr, String endTimestr) {
        this.taskId = String.valueOf(nextId++);
        this.taskName = taskName;
        this.startTime = LocalDateTime.parse(startTimestr, FORMATTER);
        this.endTime = LocalDateTime.parse(endTimestr, FORMATTER);
        this.status = taskStatus.TO_DO;
    }

    public TaskModel(String taskId, String taskName, String startTimestr, String endTimestr) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.startTime = LocalDateTime.parse(startTimestr, FORMATTER);
        this.endTime = LocalDateTime.parse(endTimestr, FORMATTER);
        this.status = taskStatus.TO_DO;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getName() {
        return taskName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public taskStatus getStatus() {
        return status;
    }

    public void setStatus(taskStatus status) {
        this.status = status;
    }

    public static void setNextId(Long nextid) {
        nextId = nextid;
    }

    @Override
    public String toString() {
        return "TaskId:" + taskId + " TaskName:" + taskName + " StartTime:" + startTime.format(FORMATTER)
                + " EndTime:" + endTime.format(FORMATTER) + " Status:" + status;
    }
}

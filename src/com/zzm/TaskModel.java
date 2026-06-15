package com.zzm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 任务数据模型层。
 * 负责保存单个任务的 ID、名称、起止时间和状态，并提供基础访问方法。
 */
public class TaskModel {
    /** TaskModel 内部统一使用的时间解析和格式化规则。 */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");

    /**
     * 任务状态枚举。
     * TO_DO 表示未开始，IN_PROGRESS 表示进行中，COMPLETED 表示已完成。
     */
    public enum taskStatus {
        TO_DO,
        IN_PROGRESS,
        COMPLETED
    }

    /** 每个任务的唯一 ID，由 nextId 递增生成或从文件恢复。 */
    private String taskId;
    private static long nextId = 0L;
    /** 用户输入的任务名称。 */
    private String taskName;
    /** 任务开始时间和结束时间，供 Service 扫描任务状态使用。 */
    LocalDateTime startTime;
    LocalDateTime endTime;
    /** 当前任务状态。 */
    taskStatus status;

    /**
     * 新建任务构造方法。
     * 自动分配递增 ID，并把传入的时间字符串解析为 LocalDateTime。
     */
    public TaskModel(String taskName, String startTimestr, String endTimestr) {
        this.taskId = String.valueOf(nextId++);
        this.taskName = taskName;
        this.startTime = LocalDateTime.parse(startTimestr, FORMATTER);
        this.endTime = LocalDateTime.parse(endTimestr, FORMATTER);
        this.status = taskStatus.TO_DO;
    }

    /**
     * 文件恢复构造方法。
     * 使用文件中的已有 ID 创建任务，便于持久化数据重新加载。
     */
    public TaskModel(String taskId, String taskName, String startTimestr, String endTimestr) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.startTime = LocalDateTime.parse(startTimestr, FORMATTER);
        this.endTime = LocalDateTime.parse(endTimestr, FORMATTER);
        this.status = taskStatus.TO_DO;
    }

    /** 返回任务 ID。 */
    public String getTaskId() {
        return taskId;
    }

    /** 返回任务名称。 */
    public String getName() {
        return taskName;
    }

    /** 返回任务开始时间。 */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /** 返回任务结束时间。 */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /** 返回任务当前状态。 */
    public taskStatus getStatus() {
        return status;
    }

    /** 设置任务状态，供 Service 在扫描时间或完成任务时调用。 */
    public void setStatus(taskStatus status) {
        this.status = status;
    }

    /**
     * 设置下一个任务 ID。
     * 文件加载完成后根据已有最大 ID 调整，避免新任务 ID 与旧数据重复。
     */
    public static void setNextId(Long nextid) {
        nextId = nextid;
    }

    /**
     * 格式化任务信息。
     * 用于控制台查询输出，将时间转换回用户可读的字符串。
     */
    @Override
    public String toString() {
        return "TaskId:" + taskId + " TaskName:" + taskName + " StartTime:" + startTime.format(FORMATTER)
                + " EndTime:" + endTime.format(FORMATTER) + " Status:" + status;
    }
}

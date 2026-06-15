package com.zzm;

import com.zzm.Result.CompleteTaskResult;
import com.zzm.Result.CreateTaskResult;
import com.zzm.Result.QueryTaskResult;
import com.zzm.Result.RemoveTaskResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

/**
 * 任务业务服务层。
 * 负责维护内存中的任务集合，提供创建、查询、完成、删除、状态扫描和文件持久化能力。
 */
public class Service {
    /** 统一的任务时间格式，和用户输入、文件存储保持一致。 */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");
    /** 当前服务实例使用的任务存储文件路径，默认写入 taskList.txt。 */
    private String storageFilePath = "taskList.txt";
    /** 内存任务表，key 为任务 ID，value 为任务模型对象。 */
    HashMap<String, TaskModel> taskMap = new HashMap<>();

    /**
     * 从文件加载任务数据。
     * 每行按 id,name,startTime,endTime,status 解析，加载后同步恢复任务状态和下一个可用 ID。
     */
    public void LoadFromFile(String filePath) {
        storageFilePath = filePath;
        taskMap.clear();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
                TaskModel.setNextId(0L);
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            long maxId = -1L;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] lines = line.split(",");
                if (lines.length != 5) {
                    continue;
                }

                String taskId = lines[0];
                String taskName = lines[1];
                String startTimestr = lines[2];
                String endTimestr = lines[3];
                TaskModel.taskStatus status = TaskModel.taskStatus.valueOf(lines[4]);
                TaskModel task = new TaskModel(taskId, taskName, startTimestr, endTimestr);
                task.setStatus(status);
                taskMap.put(taskId, task);

                long nowId = Long.parseLong(task.getTaskId());
                if (nowId > maxId) {
                    maxId = nowId;
                }
            }
            TaskModel.setNextId(maxId + 1);
            reader.close();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存当前任务数据到文件。
     * 将内存中的任务集合转换成逗号分隔文本，写回当前 storageFilePath 指向的文件。
     */
    public void SaveToFile() {
        try {
            File file = new File(storageFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (TaskModel task : taskMap.values()) {
                String startTimestr = task.getStartTime().format(FORMATTER);
                String endTimestr = task.getEndTime().format(FORMATTER);
                String line = task.getTaskId() + "," + task.getName() + "," + startTimestr + "," + endTimestr + "," + task.getStatus();
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描指定任务的时间状态。
     * 根据当前时间和任务起止时间，把任务转换为 TO_DO、IN_PROGRESS 或 COMPLETED。
     */
    public void ScanTime(String taskId) {
        LocalDateTime now = LocalDateTime.now();
        TaskModel task = taskMap.get(taskId);
        if (task == null || task.getStatus() == TaskModel.taskStatus.COMPLETED) {
            return;
        }

        if (!task.endTime.isAfter(now)) {
            task.setStatus(TaskModel.taskStatus.COMPLETED);
        } else if (!task.startTime.isAfter(now)) {
            task.setStatus(TaskModel.taskStatus.IN_PROGRESS);
        } else {
            task.setStatus(TaskModel.taskStatus.TO_DO);
        }
    }

    /**
     * 全局状态扫描模块。
     * 遍历所有任务并调用 ScanTime，确保查询或保存前任务状态尽量接近当前时间。
     */
    public void GlobalScan() {
        for (TaskModel task : taskMap.values()) {
            ScanTime(task.getTaskId());
        }
    }

    /**
     * 判断任务 ID 是否可用。
     * 如果任务集合中不存在该 ID，说明它可以用于新任务。
     */
    public Boolean ifIdAvailable(String taskId) {
        return !taskMap.containsKey(taskId);
    }

    /**
     * 创建任务模块。
     * 校验名称和时间范围，校验通过后创建 TaskModel、放入内存集合并立即保存。
     */
    public CreateTaskResult createTask(String taskName, String startTimestr, String endTimestr) {
        if (taskName == null || taskName.trim().isEmpty()) {
            return new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_NAME, null);
        } else if (startTimestr == null || endTimestr == null) {
            return new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_TIME, null);
        }

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(startTimestr, FORMATTER);
            endTime = LocalDateTime.parse(endTimestr, FORMATTER);
        } catch (DateTimeParseException e) {
            return new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_TIME, null);
        }

        if (startTime.isAfter(endTime) || endTime.isBefore(LocalDateTime.now())) {
            return new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_TIME, null);
        }

        TaskModel task = new TaskModel(taskName, startTimestr, endTimestr);
        taskMap.put(task.getTaskId(), task);
        GlobalScan();
        SaveToFile();
        return new CreateTaskResult(CreateTaskResult.createTaskStatus.SUCCESS, task);
    }

    /**
     * 查询全部任务模块。
     * 查询前先扫描状态，并返回任务表副本，避免调用方直接篡改内部集合。
     */
    public HashMap<String, TaskModel> QueryAllTask() {
        GlobalScan();
        return new HashMap<>(taskMap);
    }

    /**
     * 查询单个任务模块。
     * 根据任务 ID 定位任务，找到后刷新该任务状态并包装为 QueryTaskResult 返回。
     */
    public QueryTaskResult QueryTask(String taskId) {
        if (taskId == null) {
            return new QueryTaskResult(QueryTaskResult.queryTaskStatus.ILLEGAL_ID, null);
        } else if (!taskMap.containsKey(taskId)) {
            return new QueryTaskResult(QueryTaskResult.queryTaskStatus.NOT_FOUND, null);
        }

        ScanTime(taskId);
        TaskModel task = taskMap.get(taskId);
        return new QueryTaskResult(QueryTaskResult.queryTaskStatus.SUCCESS, task);
    }

    /**
     * 完成任务模块。
     * 先确认任务存在并刷新状态，未完成的任务会被设置为 COMPLETED 并保存到文件。
     */
    public CompleteTaskResult CompleteTask(String taskId) {
        if (taskId == null) {
            return new CompleteTaskResult(CompleteTaskResult.completeTaskResult.ILLEGAL_ID, null);
        } else if (!taskMap.containsKey(taskId)) {
            return new CompleteTaskResult(CompleteTaskResult.completeTaskResult.TASK_NOT_FOUND, null);
        }

        ScanTime(taskId);
        TaskModel task = taskMap.get(taskId);
        if (task.getStatus() == TaskModel.taskStatus.COMPLETED) {
            return new CompleteTaskResult(CompleteTaskResult.completeTaskResult.FAIL_ALREADY_COMPLETE, task);
        }

        task.setStatus(TaskModel.taskStatus.COMPLETED);
        SaveToFile();
        return new CompleteTaskResult(CompleteTaskResult.completeTaskResult.SUCCESS, task);
    }

    /**
     * 删除任务模块。
     * 根据任务 ID 从内存集合中移除任务，随后保存文件并返回被删除的任务对象。
     */
    public RemoveTaskResult RemoveTask(String taskId) {
        if (taskId == null) {
            return new RemoveTaskResult(RemoveTaskResult.removeTaskResult.ILLEGAL_ID, null);
        } else if (!taskMap.containsKey(taskId)) {
            return new RemoveTaskResult(RemoveTaskResult.removeTaskResult.TASK_NOT_FOUND, null);
        }

        TaskModel task = taskMap.get(taskId);
        taskMap.remove(taskId);
        GlobalScan();
        SaveToFile();
        return new RemoveTaskResult(RemoveTaskResult.removeTaskResult.SUCCESS, task);
    }
}

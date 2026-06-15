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

public class Service {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");
    private String storageFilePath = "taskList.txt";
    HashMap<String, TaskModel> taskMap = new HashMap<>();

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

    public void GlobalScan() {
        for (TaskModel task : taskMap.values()) {
            ScanTime(task.getTaskId());
        }
    }

    public Boolean ifIdAvailable(String taskId) {
        return !taskMap.containsKey(taskId);
    }

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

    public HashMap<String, TaskModel> QueryAllTask() {
        GlobalScan();
        return new HashMap<>(taskMap);
    }

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

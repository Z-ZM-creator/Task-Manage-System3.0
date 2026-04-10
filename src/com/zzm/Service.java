package com.zzm;
import com.zzm.Result.CreateTaskResult;
import com.zzm.Result.QueryTaskResult;
import com.zzm.Result.CompleteTaskResult;
import com.zzm.Result.RemoveTaskResult;

import javax.swing.text.html.FormView;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.time.*;

public class Service {
    HashMap<String, TaskModel> taskMap = new HashMap<>();

    //LoadFromeFile用于从文件中加载任务
    public void LoadFromFile(String filePath){
        try{
            File file=new File(filePath);
            if(!file.exists()){
                file.createNewFile();
                return;
            }
            BufferedReader reader=new BufferedReader(new FileReader(file));
            String Line;
            Long maxId=0L;
            while((Line=reader.readLine())!=null){
                if(Line.trim().isEmpty()){
                    continue;
                }
                String lines[]=Line.split(",");
                if(lines.length!=5){
                    continue;
                }
                String taskId=lines[0];
                String taskName=lines[1];
                String startTimestr=lines[2];
                String endTimestr=lines[3];
                TaskModel.taskStatus status=TaskModel.taskStatus.valueOf(lines[4]);
                TaskModel task=new TaskModel(taskId,taskName,startTimestr,endTimestr);
                taskMap.put(taskId,task);
                Long nowId=Long.parseLong(task.getTaskId());
                if(nowId>maxId){
                    maxId=nowId;
                }
            }
            TaskModel.setNextId(maxId+1);
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //SaveToFile用于将任务保存到文件中
    public void SaveToFile(){
        try{
            File file=new File("taskList.txt");
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter writer=new BufferedWriter(new FileWriter(file));
            for(TaskModel task:taskMap.values()){
                DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");
                String startTimestr=task.getStartTime().format(formatter);
                String endTimestr=task.getEndTime().format(formatter);
                String line=task.getTaskId()+","+task.getName()+","+startTimestr+","+endTimestr+","+task.status;
                writer.write(line);
                writer.newLine();
            }
            writer.flush();//什么意思
            writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //ScanTime用于扫面任务是否到达特定时间节点，经过时间节点自动转换任务状态。
    public void ScanTime(String taskId) {
        LocalDateTime now = LocalDateTime.now();
        TaskModel task = taskMap.get(taskId);
        if (task.startTime.isBefore(now) && task.endTime.isAfter(now)) {
            task.status = TaskModel.taskStatus.IN_PROGRESS;
        } else if (task.endTime.isBefore(now)) {
            task.status = TaskModel.taskStatus.COMPLETED;
        }
    }

    //GlobalScan用于全局扫描，每秒调用一次ScanTime方法
    public void GlobalScan() {
        for (TaskModel task : taskMap.values()) {
            ScanTime(task.getTaskId());
        }
    }

    //isIdAvailable用于判断ID是否可用，如果可用则返回true，即在map中找不到对应的id
    public Boolean ifIdAvailable(String taskId) {
        return !taskMap.containsKey(taskId);
    }

    //createTask用于创建任务
    public CreateTaskResult createTask(String taskName, String startTimestr, String endTimestr) {
        CreateTaskResult result;
        if (taskName == null) {
            return result = new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_NAME, null);
        } else if (startTimestr == null || endTimestr == null) {
            return result = new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_TIME, null);
        }else if(LocalDateTime.parse(startTimestr,DateTimeFormatter.ofPattern("yyyy-M-d H:m:s")).isAfter(LocalDateTime.parse(endTimestr,DateTimeFormatter.ofPattern("yyyy-M-d H:m:s")))){
            return result = new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_TIME, null);
        }else if((LocalDateTime.parse(endTimestr,DateTimeFormatter.ofPattern("yyyy-M-d H:m:s")).isBefore(LocalDateTime.now()))){
            return result = new CreateTaskResult(CreateTaskResult.createTaskStatus.ILLEGAL_TIME, null);
        }
        TaskModel task = new TaskModel(taskName, startTimestr, endTimestr);
        taskMap.put(task.getTaskId(), task);
        GlobalScan();
        SaveToFile();
        return result = new CreateTaskResult(CreateTaskResult.createTaskStatus.SUCCESS, task);
    }

    //QueryAllTask用于查询所有任务
    public HashMap<String, TaskModel> QueryAllTask() {
        GlobalScan();
        return taskMap;
    }

    //QueryTask用于查询指定任务
    public QueryTaskResult QueryTask(String taskId) {
        QueryTaskResult result;
        if (taskId == null) {
            return result = new QueryTaskResult(QueryTaskResult.queryTaskStatus.ILLEGAL_ID, null);
        } else if (!taskMap.containsKey(taskId)) {
            return result = new QueryTaskResult(QueryTaskResult.queryTaskStatus.NOT_FOUND, null);
        } else {
            TaskModel task = taskMap.get(taskId);
            ScanTime(taskId);
            return result = new QueryTaskResult(QueryTaskResult.queryTaskStatus.SUCCESS, task);
        }
    }

    //CompleteTask用于完成指定任务
    public CompleteTaskResult CompleteTask(String taskId) {
        CompleteTaskResult result;
        if (taskId == null) {
            return result = new CompleteTaskResult(CompleteTaskResult.completeTaskResult.ILLEGAL_ID, null);
        } else if (!taskMap.containsKey(taskId)) {
            return result = new CompleteTaskResult(CompleteTaskResult.completeTaskResult.TASK_NOT_FOUND, null);
        } else if (taskMap.get(taskId).endTime.isBefore(LocalDateTime.now())) {
            return result = new CompleteTaskResult(CompleteTaskResult.completeTaskResult.FAIL_ALREADY_COMPLETE, null);
        } else {
            TaskModel task = taskMap.get(taskId);
            task.status = TaskModel.taskStatus.COMPLETED;
            GlobalScan();
            SaveToFile();
            return result = new CompleteTaskResult(CompleteTaskResult.completeTaskResult.SUCCESS, task);
        }
    }

    //RemoveTask用于删除指定任务
    public RemoveTaskResult RemoveTask(String taskId) {
        RemoveTaskResult result;
        if (taskId == null) {
            return result = new RemoveTaskResult(RemoveTaskResult.removeTaskResult.ILLEGAL_ID, null);
        } else if (!taskMap.containsKey(taskId)) {
            return result = new RemoveTaskResult(RemoveTaskResult.removeTaskResult.TASK_NOT_FOUND, null);
        } else {
            TaskModel task = taskMap.get(taskId);
            taskMap.remove(taskId);
            GlobalScan();
            SaveToFile();
            return result = new RemoveTaskResult(RemoveTaskResult.removeTaskResult.SUCCESS, task);
        }
    }
}

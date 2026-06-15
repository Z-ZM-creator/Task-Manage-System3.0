package com.zzm;

import java.util.Scanner;

import com.zzm.Result.CompleteTaskResult;
import com.zzm.Result.CreateTaskResult;
import com.zzm.Result.QueryTaskResult;
import com.zzm.Result.RemoveTaskResult;

/**
 * 控制台交互层。
 * 负责展示菜单、读取用户输入，并把具体任务操作转交给 Service 业务层处理。
 */
public class Main {
    /**
     * 程序入口。
     * 启动时从 taskList.txt 加载任务，随后通过循环菜单持续接收用户操作。
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Service service = new Service();
        System.out.println("Welcome to Task Manager!");
        service.LoadFromFile("taskList.txt");
        while (true) {
            System.out.println("1. Create a task");
            System.out.println("2. Query ALL tasks");
            System.out.println("3. Query a task");
            System.out.println("4. Complete a task");
            System.out.println("5. Remove a task");
            System.out.println("6.Save tasks");
            System.out.println("7.Exit");
            System.out.println("Please select an option:");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    CreateTaskInteraction(service, sc);
                    break;
                case 2:
                    QueryALLTaskInteraction(service);
                    break;
                case 3:
                    QueryTaskInteraction(service, sc);
                    break;
                case 4:
                    CompleteTaskInteraction(service, sc);
                    break;
                case 5:
                    RemoveTaskInteraction(service, sc);
                    break;
                case 6:
                    SaveTaskInteraction(service);
                    break;
                case 7:
                    System.out.println("Exiting...");
                    service.SaveToFile();
                    sc.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    /**
     * 创建任务交互模块。
     * 读取任务名称、开始时间和结束时间，再根据 Service 返回的结果输出创建状态。
     */
    public static void CreateTaskInteraction(Service service, Scanner sc) {
        System.out.println("Please enter the task name:");
        String taskName = sc.nextLine();
        System.out.println("Time format: yyyy-M-d H:m:s");
        System.out.println("Please enter the task start time:");
        String startTimestr = sc.nextLine();
        System.out.println("Please enter the task end time:");
        String endTimestr = sc.nextLine();
        CreateTaskResult result = service.createTask(taskName, startTimestr, endTimestr);
        switch (result.status) {
            case SUCCESS:
                System.out.println("Task created successfully! Task ID: " + result.task.getTaskId());
                break;
            case ILLEGAL_NAME:
                System.out.println("Task creation failed: Illegal task name.");
                break;
            case ILLEGAL_TIME:
                System.out.println("Task creation failed: Illegal time format.");
                break;
        }
    }

    /**
     * 查询全部任务交互模块。
     * 从 Service 获取任务副本并逐条输出，避免用户界面层直接修改内部任务集合。
     */
    public static void QueryALLTaskInteraction(Service service) {
        for (TaskModel task : service.QueryAllTask().values()) {
            System.out.println(task.toString());
            System.out.println("--------------------------------------------------");
        }
    }

    /**
     * 查询单个任务交互模块。
     * 读取任务 ID，调用 Service 查询，并按查询结果提示用户。
     */
    public static void QueryTaskInteraction(Service service, Scanner sc) {
        System.out.println("Please enter the task ID:");
        String taskId = sc.next();
        QueryTaskResult result = service.QueryTask(taskId);
        switch (result.status) {
            case SUCCESS:
                System.out.println(result.task.toString());
                break;
            case NOT_FOUND:
                System.out.println("Task not found.");
                break;
            case ILLEGAL_ID:
                System.out.println("Task query failed: Illegal task ID.");
                break;
        }
    }

    /**
     * 完成任务交互模块。
     * 读取任务 ID，调用 Service 完成任务，并处理不存在、已完成和非法 ID 等状态。
     */
    public static void CompleteTaskInteraction(Service service, Scanner sc) {
        System.out.println("Please enter the task ID:");
        String taskId = sc.next();
        CompleteTaskResult result = service.CompleteTask(taskId);
        switch (result.getStatus()) {
            case SUCCESS:
                System.out.println("Task completed successfully!");
                break;
            case TASK_NOT_FOUND:
                System.out.println("Task not found.");
                break;
            case FAIL_ALREADY_COMPLETE:
                System.out.println("Task completion failed: Task already completed.");
                break;
            case ILLEGAL_ID:
                System.out.println("Task completion failed: Illegal task ID.");
                break;
        }
    }

    /**
     * 删除任务交互模块。
     * 读取任务 ID，调用 Service 删除任务，并反馈删除是否成功。
     */
    public static void RemoveTaskInteraction(Service service, Scanner sc) {
        System.out.println("Please enter the task ID:");
        String taskId = sc.next();
        RemoveTaskResult result = service.RemoveTask(taskId);
        switch (result.getStatus()) {
            case SUCCESS:
                System.out.println("Task removed successfully!");
                break;
            case TASK_NOT_FOUND:
                System.out.println("Task not found.");
                break;
            case ILLEGAL_ID:
                System.out.println("Task removal failed: Illegal task ID.");
                break;
        }
    }

    /**
     * 保存任务交互模块。
     * 手动触发 Service 将当前任务集合写入持久化文件。
     */
    public static void SaveTaskInteraction(Service service) {
        System.out.println("Saving tasks...");
        service.SaveToFile();
        System.out.println("Tasks saved successfully!");
    }
}

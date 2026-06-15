package com.zzm;

import com.zzm.Result.CompleteTaskResult;
import com.zzm.Result.CreateTaskResult;
import com.zzm.Result.QueryTaskResult;
import com.zzm.Result.RemoveTaskResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Service 纯 Java 测试模块。
 * 不依赖第三方测试框架，通过 main 方法依次调用测试用例并用自定义断言验证结果。
 */
public class ServiceTest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");

    /**
     * 测试入口。
     * 逐项执行创建、校验、查询、完成、状态扫描、持久化、删除和集合保护测试。
     */
    public static void main(String[] args) throws IOException {
        testCreateTaskSuccess();
        testCreateTaskValidation();
        testQueryTask();
        testCompleteTask();
        testStatusScan();
        testPersistenceKeepsStatus();
        testRemoveTask();
        testQueryAllReturnsCopy();

        System.out.println("All Service tests passed.");
    }

    private static void testCreateTaskSuccess() throws IOException {
        Service service = newService();
        CreateTaskResult result = service.createTask("write tests", future(1), future(2));

        assertEquals(CreateTaskResult.createTaskStatus.SUCCESS, result.getStatus(), "valid task should be created");
        assertNotNull(result.getTask(), "created task should be returned");
        assertEquals(1, service.QueryAllTask().size(), "created task should be stored");
    }

    private static void testCreateTaskValidation() throws IOException {
        Service service = newService();

        assertEquals(CreateTaskResult.createTaskStatus.ILLEGAL_NAME,
                service.createTask(" ", future(1), future(2)).getStatus(),
                "blank task name should be rejected");
        assertEquals(CreateTaskResult.createTaskStatus.ILLEGAL_TIME,
                service.createTask("bad time", "not-a-time", future(2)).getStatus(),
                "invalid start time should be rejected");
        assertEquals(CreateTaskResult.createTaskStatus.ILLEGAL_TIME,
                service.createTask("bad range", future(3), future(2)).getStatus(),
                "start after end should be rejected");
        assertEquals(CreateTaskResult.createTaskStatus.ILLEGAL_TIME,
                service.createTask("past end", past(2), past(1)).getStatus(),
                "past end time should be rejected");
    }

    private static void testQueryTask() throws IOException {
        Service service = newService();
        CreateTaskResult created = service.createTask("query me", future(1), future(2));

        QueryTaskResult found = service.QueryTask(created.getTask().getTaskId());
        assertEquals(QueryTaskResult.queryTaskStatus.SUCCESS, found.getStatus(), "existing task should be found");
        assertEquals(created.getTask().getTaskId(), found.getTask().getTaskId(), "query should return requested task");

        QueryTaskResult missing = service.QueryTask("missing");
        assertEquals(QueryTaskResult.queryTaskStatus.NOT_FOUND, missing.getStatus(), "missing task should not be found");
    }

    private static void testCompleteTask() throws IOException {
        Service service = newService();
        CreateTaskResult created = service.createTask("complete me", future(1), future(2));
        String taskId = created.getTask().getTaskId();

        CompleteTaskResult completed = service.CompleteTask(taskId);
        assertEquals(CompleteTaskResult.completeTaskResult.SUCCESS, completed.getStatus(), "task should complete once");
        assertEquals(TaskModel.taskStatus.COMPLETED, completed.getTask().getStatus(), "completed task should keep completed status");

        CompleteTaskResult repeated = service.CompleteTask(taskId);
        assertEquals(CompleteTaskResult.completeTaskResult.FAIL_ALREADY_COMPLETE, repeated.getStatus(), "completed task should not complete twice");

        CompleteTaskResult missing = service.CompleteTask("missing");
        assertEquals(CompleteTaskResult.completeTaskResult.TASK_NOT_FOUND, missing.getStatus(), "missing task cannot be completed");
    }

    private static void testStatusScan() throws IOException {
        Service service = newService();

        TaskModel futureTask = new TaskModel("100", "future", future(1), future(2));
        service.taskMap.put(futureTask.getTaskId(), futureTask);
        service.ScanTime(futureTask.getTaskId());
        assertEquals(TaskModel.taskStatus.TO_DO, futureTask.getStatus(), "future task should stay to-do");

        TaskModel activeTask = new TaskModel("101", "active", past(1), future(1));
        service.taskMap.put(activeTask.getTaskId(), activeTask);
        service.ScanTime(activeTask.getTaskId());
        assertEquals(TaskModel.taskStatus.IN_PROGRESS, activeTask.getStatus(), "active task should be in progress");

        TaskModel endedTask = new TaskModel("102", "ended", past(2), past(1));
        service.taskMap.put(endedTask.getTaskId(), endedTask);
        service.ScanTime(endedTask.getTaskId());
        assertEquals(TaskModel.taskStatus.COMPLETED, endedTask.getStatus(), "ended task should be completed");
    }

    private static void testPersistenceKeepsStatus() throws IOException {
        Path file = Files.createTempFile("task-service-persist", ".txt");
        Service service = new Service();
        service.LoadFromFile(file.toString());

        CreateTaskResult created = service.createTask("persist me", future(1), future(2));
        service.CompleteTask(created.getTask().getTaskId());

        Service reloaded = new Service();
        reloaded.LoadFromFile(file.toString());
        TaskModel loaded = reloaded.QueryTask(created.getTask().getTaskId()).getTask();

        assertEquals(created.getTask().getTaskId(), loaded.getTaskId(), "loaded task should keep id");
        assertEquals("persist me", loaded.getName(), "loaded task should keep name");
        assertEquals(TaskModel.taskStatus.COMPLETED, loaded.getStatus(), "loaded task should keep completed status");
    }

    private static void testRemoveTask() throws IOException {
        Service service = newService();
        CreateTaskResult created = service.createTask("remove me", future(1), future(2));

        RemoveTaskResult removed = service.RemoveTask(created.getTask().getTaskId());
        assertEquals(RemoveTaskResult.removeTaskResult.SUCCESS, removed.getStatus(), "existing task should be removed");
        assertEquals(0, service.QueryAllTask().size(), "removed task should leave storage");

        RemoveTaskResult missing = service.RemoveTask("missing");
        assertEquals(RemoveTaskResult.removeTaskResult.TASK_NOT_FOUND, missing.getStatus(), "missing task cannot be removed");
    }

    private static void testQueryAllReturnsCopy() throws IOException {
        Service service = newService();
        service.createTask("copy protection", future(1), future(2));

        HashMap<String, TaskModel> tasks = service.QueryAllTask();
        tasks.clear();

        assertEquals(1, service.QueryAllTask().size(), "query all should not expose internal task map");
    }

    private static Service newService() throws IOException {
        Path file = Files.createTempFile("task-service-test", ".txt");
        Service service = new Service();
        service.LoadFromFile(file.toString());
        return service;
    }

    private static String future(long hours) {
        return LocalDateTime.now().plusHours(hours).format(FORMATTER);
    }

    private static String past(long hours) {
        return LocalDateTime.now().minusHours(hours).format(FORMATTER);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new AssertionError(message);
        }
    }
}

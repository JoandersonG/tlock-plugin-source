package com.joanderson.tlock.model;

public class Connection {

    public enum Type {
        IN,
        OUT;
    }
    private String name;
    private Type type;
    private String taskId;
    public Connection(String name, Type type, String taskId) {
        this.name = name;
        this.type = type;
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

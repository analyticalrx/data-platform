package com.primerx.task.dto;

public class UpdateTaskStatusRequest {

    private String status;

    public UpdateTaskStatusRequest() {
    }

    public UpdateTaskStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
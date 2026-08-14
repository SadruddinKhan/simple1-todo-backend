package com.skhan.todo.dto;

public class TodoStatsResponse {
    private long total;
    private long completed;
    private long pending;

    public TodoStatsResponse() {
    }

    public TodoStatsResponse(long total, long completed, long pending) {
        this.total = total;
        this.completed = completed;
        this.pending = pending;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }
}

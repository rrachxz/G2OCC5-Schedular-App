package com.grptwo.schedulerapp.models;

import java.time.LocalDateTime;

public class Recurrance {
    private Integer recId;
    private Integer interval;
    private Integer times;
    private LocalDateTime endDateTime;

    public Recurrance(Integer recId, Integer interval, Integer times, LocalDateTime endDateTime) {
        this.recId = recId;
        this.interval = interval;
        this.times = times;
        this.endDateTime = endDateTime;
    }

    public Integer getRecId() {
        return recId;
    }

    public void setRecId(Integer recId) {
        this.recId = recId;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
}


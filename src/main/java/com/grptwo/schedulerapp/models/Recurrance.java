package com.grptwo.schedulerapp.models;

import java.time.LocalDateTime;

public class Recurrance {
    private Integer interval;
    private Integer times;
    private LocalDateTime endDateTime;

    public Recurrance(Integer recId, Integer interval, Integer times, LocalDateTime endDateTime) {
        this.interval = interval;
        this.times = times;
        this.endDateTime = endDateTime;
    }

    public Integer getInterval() {
        return interval;
    }

    public Integer getTimes() {
        return times;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}


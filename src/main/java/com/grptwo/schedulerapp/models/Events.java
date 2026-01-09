package com.grptwo.schedulerapp.models;

import java.time.LocalDateTime;

public class Events {
    private int id;
    private String title;
    private String desc;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Recurrance recurrence; // NEW: Add this field

    public Events(int id, String title, String desc, LocalDateTime startDateTime,
                  LocalDateTime endDateTime, Recurrance recurrence) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.recurrence = recurrence;
    }

    public Events(int id, String title, String desc, LocalDateTime startDateTime,
                  LocalDateTime endDateTime) {
        this(id, title, desc, startDateTime, endDateTime, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Recurrance getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrance recurrence) {
        this.recurrence = recurrence;
    }
}
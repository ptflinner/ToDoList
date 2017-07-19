package com.example.patrick.todolist.data;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoItem {
    private long id;
    private String description;
    private String dueDate;
    private Integer completed;
    private String category;

    public ToDoItem(long id,String description, String dueDate,int completed,String category) {
        this.id=id;
        this.description = description;
        this.dueDate = dueDate;
        this.completed=completed;
        this.category=category;
    }

    public ToDoItem(ToDoItem copy){
        this.id=copy.getId();
        this.description=copy.getDescription();
        this.dueDate=copy.getDueDate();
        this.completed=copy.getCompleted();
        this.category=copy.getCategory();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

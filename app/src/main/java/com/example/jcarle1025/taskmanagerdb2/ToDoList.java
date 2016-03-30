package com.example.jcarle1025.taskmanagerdb2;

public class ToDoList {
    private boolean isSelected;
    private String name;

    public ToDoList(String name){
        this.name = name;
        this.isSelected = false;
    }

    public boolean isSelected(){
        return this.isSelected;
    }
    public void setSelected(boolean b){
        this.isSelected = b;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }
}

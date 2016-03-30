package com.example.jcarle1025.taskmanagerdb2;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable{
    private String title;
    private String description;
    private String date;
    private boolean isSelected;
    private int id;

    public Task(int id, String title, String description, String date){
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.isSelected = false;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String newTitle){
        this.title = newTitle;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String newDesc){
        this.description = newDesc;
    }

    public String toString(){
        return this.title +"Created on: "+this.date+"\n"+this.description;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(boolean s){
        this.isSelected = s;
    }

    public String getDate(){
        return this.date;
    }

    public void setDate(String d){ this.date = d; }
}

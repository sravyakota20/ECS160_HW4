package com.ecs160.hw2;

public class Record {
    private String text;

    public Record(){
        this.text = "";
    }

    public Record(String text){
        this.text = text;
    }

    public String getText(){
        return this.text;
    }

    public void setText(String text){
        this.text = text;
    }
}

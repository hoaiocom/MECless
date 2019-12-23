package com.hoant.mecless;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Min {

    @SerializedName("X")
    @Expose
    private Integer x;
    @SerializedName("Y")
    @Expose
    private Integer y;

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

}
package com.lavanidad.qiushui.map.bean;

import java.util.List;

/**
 * @Author : fzy
 * @Date : 2022/5/20
 * @Description :
 */
public class YamlBean {


    private String image;
    private double resolution;
    private List<Double> origin;
    private int negate;
    private double occupied_thresh;
    private double free_thresh;
    private int width;
    private int height;
    private String name;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public List<Double> getOrigin() {
        return origin;
    }

    public void setOrigin(List<Double> origin) {
        this.origin = origin;
    }

    public int getNegate() {
        return negate;
    }

    public void setNegate(int negate) {
        this.negate = negate;
    }

    public double getOccupied_thresh() {
        return occupied_thresh;
    }

    public void setOccupied_thresh(double occupied_thresh) {
        this.occupied_thresh = occupied_thresh;
    }

    public double getFree_thresh() {
        return free_thresh;
    }

    public void setFree_thresh(double free_thresh) {
        this.free_thresh = free_thresh;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

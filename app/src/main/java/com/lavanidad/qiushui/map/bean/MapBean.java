package com.lavanidad.qiushui.map.bean;

import java.util.Arrays;
import java.util.List;

/**
 * @Author : fzy
 * @Date : 2022/5/4
 * @Description :
 */
public class MapBean {

    private List<PoiDTO> poi;
    private List<WallDTO> wall;
    private List<AreaDTO> area;

    public List<PoiDTO> getPoi() {
        return poi;
    }

    public void setPoi(List<PoiDTO> poi) {
        this.poi = poi;
    }

    public List<WallDTO> getWall() {
        return wall;
    }

    public void setWall(List<WallDTO> wall) {
        this.wall = wall;
    }

    public List<AreaDTO> getArea() {
        return area;
    }

    public void setArea(List<AreaDTO> area) {
        this.area = area;
    }

    public static class PoiDTO {
        private int id;
        private String name;
        private float rotation;
        private float[] position;
        private int[] positionOrigin;
        private String desc;

        public float getRotation() {
            return rotation;
        }

        public void setRotation(float rotation) {
            this.rotation = rotation;
        }

        public int[] getPositionOrigin() {
            return positionOrigin;
        }

        public void setPositionOrigin(int[] positionOrigin) {
            this.positionOrigin = positionOrigin;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float[] getPosition() {
            return position;
        }

        public void setPosition(float[] position) {
            this.position = position;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "PoiDTO{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", rotation=" + rotation +
                    ", position=" + Arrays.toString(position) +
                    ", positionOrigin=" + Arrays.toString(positionOrigin) +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

    public static class WallDTO {
        private int id;
        private String name;
        private float rotation;
        private float[] position;
        private int[] positionOrigin;
        private String desc;

        public float getRotation() {
            return rotation;
        }

        public void setRotation(float rotation) {
            this.rotation = rotation;
        }

        public int[] getPositionOrigin() {
            return positionOrigin;
        }

        public void setPositionOrigin(int[] positionOrigin) {
            this.positionOrigin = positionOrigin;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float[] getPosition() {
            return position;
        }

        public void setPosition(float[] position) {
            this.position = position;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "WallDTO{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", rotation=" + rotation +
                    ", position=" + Arrays.toString(position) +
                    ", positionOrigin=" + Arrays.toString(positionOrigin) +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

    public static class AreaDTO {
        private int id;
        private String name;
        private float[] position;
        private int[] positionOrigin;
        private float rotation;
        private String desc;

        public int[] getPositionOrigin() {
            return positionOrigin;
        }

        public void setPositionOrigin(int[] positionOrigin) {
            this.positionOrigin = positionOrigin;
        }

        public float getRotation() {
            return rotation;
        }

        public void setRotation(float rotation) {
            this.rotation = rotation;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float[] getPosition() {
            return position;
        }

        public void setPosition(float[] position) {
            this.position = position;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "AreaDTO{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", position=" + Arrays.toString(position) +
                    ", positionOrigin=" + Arrays.toString(positionOrigin) +
                    ", rotation=" + rotation +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MapBean{" +
                "poi=" + poi +
                ", wall=" + wall +
                ", area=" + area +
                '}';
    }
}

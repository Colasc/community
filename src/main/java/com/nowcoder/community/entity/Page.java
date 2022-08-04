package com.nowcoder.community.entity;

/**
 * 封装分页相关的内容
 */
public class Page {
    //当前页，起始为0
     int current = 1;
    //总条数
     int row;
    //每页显示数据条数
     int limit = 10;
    //访问路径
     String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1){
            this.current = current;
        }

    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100){
            this.limit = limit;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    //获取起始坐标
    public int getOffset(){
        return (current-1)*limit;
    }
    //获取总页数
    public int getTotalPage(){
        if (row % limit == 0){
            return row / limit;
        }else {
            return row / limit + 1;
        }
    }
    //获取前端页码
    public int getFrom(){
        int from = current -2 ;
        return from < 1 ? 1 : from;
    }
    //获取分页的后端页码
    public int getTo(){
        int to = current + 2;
        int total = getTotalPage();
        return  to > total ? total : to;
    }
}

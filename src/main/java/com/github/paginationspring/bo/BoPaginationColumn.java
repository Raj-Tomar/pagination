package com.github.paginationspring.bo;

public class BoPaginationColumn {
    private String columnName;
    private String orderColumns;
    private String orderDirections;
    private Integer width;

    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    public String getOrderColumns() {
        return orderColumns;
    }
    public void setOrderColumns(String orderColumns) {
        this.orderColumns = orderColumns;
    }
    public String getOrderDirections() {
        return orderDirections;
    }
    public void setOrderDirections(String orderDirections) {
        this.orderDirections = orderDirections;
    }
    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }

}

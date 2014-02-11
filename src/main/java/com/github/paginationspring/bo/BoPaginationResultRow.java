package com.github.paginationspring.bo;

public abstract class BoPaginationResultRow<E> {
    public abstract E getPk();

    private int rowIndex;

    public int getRowIndex() {
		return rowIndex;
	}
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
    
}

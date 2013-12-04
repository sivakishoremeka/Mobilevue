package com.mobilevue.data;

import java.util.ArrayList;

public class GridViewData {

	private int pageCount;
	private int pageNumber;
	private ArrayList<MovieObj> movieListObj = new ArrayList<MovieObj>();

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public ArrayList<MovieObj> getMovieListObj() {
		return movieListObj;
	}

	public void setMovieListObj(ArrayList<MovieObj> movieListObj) {
		this.movieListObj = movieListObj;
	}

}

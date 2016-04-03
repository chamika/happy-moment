package com.chamika.happymoment.model;

import java.io.Serializable;
import java.util.ArrayList;

public class StateMainActivity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8074048951577643229L;
	private ArrayList<FBPhoto> rankedPhotos;
	private int currentIndex = 0;

	public ArrayList<FBPhoto> getRankedPhotos() {
		return rankedPhotos;
	}

	public void setRankedPhotos(ArrayList<FBPhoto> rankedPhotos) {
		this.rankedPhotos = rankedPhotos;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

}

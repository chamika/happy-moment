package com.chamika.happymoment.model;

public class User {

	private String name;
	private String profileUrl;
	private String coverUrl;

	public User() {
		super();
	}

	public User(String name, String profileUrl, String coverUrl) {
		super();
		this.name = name;
		this.profileUrl = profileUrl;
		this.coverUrl = coverUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

}

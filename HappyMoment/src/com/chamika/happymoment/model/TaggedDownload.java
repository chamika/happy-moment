package com.chamika.happymoment.model;

public class TaggedDownload {

	private String id;
	private long since;
	private long until;
	private boolean downloaded;

	public TaggedDownload() {
	}

	public TaggedDownload(String id, long since, long until, boolean downloaded) {
		super();
		this.id = id;
		this.since = since;
		this.until = until;
		this.downloaded = downloaded;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getSince() {
		return since;
	}

	public void setSince(long since) {
		this.since = since;
	}

	public long getUntil() {
		return until;
	}

	public void setUntil(long until) {
		this.until = until;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	@Override
	public String toString() {
		return "TaggedDownload [id=" + id + ", since=" + since + ", until=" + until + ", downloaded=" + downloaded
				+ "]";
	}

}

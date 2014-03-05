package com.gentics.changelogmanager.legacy;

public class AlohaVersion {

	String version;
	String url;
	String date;
	boolean isLegacy = false;

	/**
	 * Creates a aloha version object that contains information about the changelog url and the version and the release date.
	 * 
	 * @param url
	 * @param version
	 * @param date
	 */
	public AlohaVersion(String url, String version, String date) {
		this.url = url;
		this.version = version;
		this.date = date;
		if (version.indexOf("0.9") >= 0) {
			this.isLegacy = true;
		}
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String isLegacy() {
		return String.valueOf(isLegacy);
	}

}

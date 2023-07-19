package com.gentics.changelogmanager;

import java.io.File;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class Component {
	private String id;

	private String name;

	private String version;

	private File entriesDirectory;

	public String getId() {
		return id;
	}

	public Component setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Component setName(String name) {
		this.name = name;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public Component setVersion(String version) {
		this.version = version;
		return this;
	}

	public File getEntriesDirectory() {
		return entriesDirectory;
	}

	public Component setEntriesDirectory(File entriesDirectory) {
		this.entriesDirectory = entriesDirectory;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Component) {
			Component other = (Component) obj;
			return StringUtils.equals(id, other.id);
		} else {
			return false;
		}
	}
}

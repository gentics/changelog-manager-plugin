package com.gentics.changelogmanager;

import java.io.File;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * POJO for a component
 */
public class Component {
	/**
	 * Component ID
	 */
	private String id;

	/**
	 * Component name
	 */
	private String name;

	/**
	 * Component short name
	 */
	private String shortName;

	/**
	 * Component version
	 */
	private String version;

	/**
	 * Entries directory for the component's changelog
	 */
	private File entriesDirectory;

	/**
	 * Get the ID
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the ID
	 * @param id ID
	 * @return fluent API
	 */
	public Component setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Get the name
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name
	 * @param name name
	 * @return fluent API
	 */
	public Component setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get the short name
	 * @return short name
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Set the short name
	 * @param shortName short name
	 * @return fluent API
	 */
	public Component setShortName(String shortName) {
		this.shortName = shortName;
		return this;
	}

	/**
	 * Get the version
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set the version
	 * @param version version
	 * @return fluent API
	 */
	public Component setVersion(String version) {
		this.version = version;
		return this;
	}

	/**
	 * Get the entries directory
	 * @return entries directory
	 */
	public File getEntriesDirectory() {
		return entriesDirectory;
	}

	/**
	 * Set the entries directory
	 * @param entriesDirectory entries directory
	 * @return fluent API
	 */
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

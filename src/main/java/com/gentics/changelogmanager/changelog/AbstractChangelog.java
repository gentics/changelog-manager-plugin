package com.gentics.changelogmanager.changelog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.gentics.changelogmanager.ChangelogManagerException;

public abstract class AbstractChangelog {
	protected String version = new String();
	protected String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
	protected Properties genericProperties = new Properties();

	public AbstractChangelog(String version) throws ChangelogManagerException {
		// TODO parse the version and validate its format
		if (StringUtils.isEmpty(version)) {
			throw new ChangelogManagerException("The target version for the changelog was null or empty.");
		}
		this.version = version;
	}

	/**
	 * Gets the previously set date for this changelog. The date will normally be set when a changelog is created.
	 * 
	 * @return
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Sets the date for this changelog
	 * 
	 * @param date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Returns the target version for this changelog
	 * 
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the minor version for the changelog version
	 * @return
	 */
	public String getMinorVersion() {
		return VersionUtils.getMinorVersion(version);
	}

	/**
	 * Adds a new property to the collection of generic properties
	 * 
	 * @param key
	 * @param value
	 */
	public void addGenericProperty(String key, String value) {
		this.genericProperties.put(key, value);
	}

	public void setGenericProperties(Properties properties) {
		this.genericProperties.putAll(properties);
	}

	/**
	 * Returns the generic properties for this changelog
	 * 
	 * @return
	 */
	public Properties getGenericProperties() {
		return this.genericProperties;
	}
}

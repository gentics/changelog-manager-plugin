package com.gentics.changelogmanager.changelog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.entry.ChangelogEntry;
import com.gentics.changelogmanager.entry.ChangelogEntryUtils;
/**
 * A changelog is a mapping from one specific version to multiple changelog entries. The changelog contains some additional information like a date and some generic
 * properties that can be used when rendering the templates.
 * 
 * @author johannes2
 * 
 */
public class Changelog {

	private String version = new String();
	private String date;
	private List<String> changeLogEntryFileNames;
	private Properties genericProperties;

	/**
	 * Creates a new changelog
	 * 
	 * @param version
	 * @param changelogEntries
	 * @throws ChangelogManagerException
	 */
	public Changelog(String version, List<ChangelogEntry> changelogEntries) throws ChangelogManagerException {
		
		// TODO parse the version and validate its format
		if (StringUtils.isEmpty(version)) {
			throw new ChangelogManagerException("The target version for the changelog was null or empty.");
		}
		this.version = version;

		if (changelogEntries == null) {
			throw new ChangelogManagerException("Could not create changelog because the mapped changelog entry list was not specified.");
		}

		changeLogEntryFileNames = new ArrayList<String>();
		for (ChangelogEntry entry : changelogEntries) {
			changeLogEntryFileNames.add(entry.getFile().getName());
		}

		String dateString = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
		date = dateString;
		genericProperties = new Properties();
	}

	/**
	 * Creates a new changelog with no entries mapped to it yet
	 * 
	 * @param version
	 * @throws ChangelogManagerException
	 */
	public Changelog(String version) throws ChangelogManagerException {
		this(version, new ArrayList<ChangelogEntry>());
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

	/**
	 * Returns the mapped changelog entries for this changelog
	 * 
	 * @return changelog entries
	 * @throws ChangelogManagerException
	 */
	public List<ChangelogEntry> getChangelogEntries() throws ChangelogManagerException {
		return ChangelogEntryUtils.getChangelogEntryFiles(ChangelogConfiguration.getBaseDirectory(), changeLogEntryFileNames);
	}

	/**
	 * Add another changelog entry to this changelog
	 * 
	 * @param entry
	 * @throws ChangelogManagerException
	 */
	public void addChangelogEntry(ChangelogEntry entry) throws ChangelogManagerException {
		if (entry == null) {
			throw new ChangelogManagerException("Could not add changelog entry to changelog because the entry was null");
		}
		changeLogEntryFileNames.add(entry.getFile().getName());
	}
}

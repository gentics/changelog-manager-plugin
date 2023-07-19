package com.gentics.changelogmanager.changelog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
public class Changelog extends AbstractChangelog {

	private List<String> changeLogEntryFileNames;

	private String componentVersion;

	/**
	 * Creates a new changelog
	 * 
	 * @param version
	 * @param changelogEntries
	 * @throws ChangelogManagerException
	 */
	public Changelog(String version, List<ChangelogEntry> changelogEntries) throws ChangelogManagerException {
		super(version);

		if (changelogEntries == null) {
			throw new ChangelogManagerException("Could not create changelog because the mapped changelog entry list was not specified.");
		}

		changeLogEntryFileNames = new ArrayList<String>();
		for (ChangelogEntry entry : changelogEntries) {
			changeLogEntryFileNames.add(entry.getFile().getName());
		}
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
	 * Get the component version (if this is a component changelog)
	 * @return component version
	 */
	public String getComponentVersion() {
		return componentVersion;
	}

	/**
	 * Set the component version
	 * @param componentVersion component version
	 */
	public void setComponentVersion(String componentVersion) {
		this.componentVersion = componentVersion;
	}

	/**
	 * Returns the mapped changelog entries for this changelog
	 * 
	 * @return changelog entries
	 * @throws ChangelogManagerException
	 */
	public List<ChangelogEntry> getChangelogEntries() throws ChangelogManagerException {
		return getChangelogEntries(ChangelogConfiguration.getEntriesDirectory());
	}

	/**
	 * Returns the mapped changelog entries for this changelog
	 * 
	 * @param entriesDirectory entries directory
	 * @return changelog entries
	 * @throws ChangelogManagerException
	 */
	public List<ChangelogEntry> getChangelogEntries(File entriesDirectory) throws ChangelogManagerException {
		return ChangelogEntryUtils.getChangelogEntryFiles(entriesDirectory, changeLogEntryFileNames);
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

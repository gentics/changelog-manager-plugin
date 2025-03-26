package com.gentics.changelogmanager.changelog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.Component;
import com.gentics.changelogmanager.entry.ChangelogEntry;

public class ComponentChangelog extends AbstractChangelog {
	protected Map<String, Changelog> changelogForComponents = new LinkedHashMap<>();

	public ComponentChangelog(String version) throws ChangelogManagerException {
		super(version);
	}

	public void addChangelog(String componentId, Changelog changelog) {
		this.changelogForComponents.put(componentId, changelog);
	}

	/**
	 * Returns the mapped changelog entries for this changelog for a component
	 * 
	 * @return changelog entries
	 * @throws ChangelogManagerException
	 */
	public List<ChangelogEntry> getChangelogEntries(Component component) throws ChangelogManagerException {
		if (changelogForComponents.containsKey(component.getId())) {
			return changelogForComponents.get(component.getId()).getChangelogEntries(component.getEntriesDirectory());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Check whether the changelog contains any changelog entries for the component
	 * @param component component
	 * @return true if the changelog contains entries, false if not
	 */
	public boolean hasChangelogEntries(Component component) {
		if (changelogForComponents.containsKey(component.getId())) {
			return changelogForComponents.get(component.getId()).hasChangelogEntries();
		} else {
			return false;
		}
	}

	/**
	 * Get the component version (if available, otherwise return empty string)
	 * @param component component
	 * @return component version
	 */
	public String getComponentVersion(Component component) {
		if (changelogForComponents.containsKey(component.getId())) {
			return StringUtils.defaultIfEmpty(changelogForComponents.get(component.getId()).getComponentVersion(), "");
		} else {
			return "";
		}
	}
}

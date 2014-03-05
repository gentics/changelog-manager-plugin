package com.gentics.changelogmanager.entry;

import java.util.Comparator;
import java.util.List;

import com.gentics.changelogmanager.ChangelogConfiguration;

/**
 * Comparator implementation that compares two changelog entries according to their type. The type priority will be determined by examing the changelog types order
 * 
 * @author johannes2
 * 
 */
public class ChangelogEntryComparator implements Comparator<ChangelogEntry> {

	public int compare(ChangelogEntry o1, ChangelogEntry o2) {
		List<String> changelogTypes = ChangelogConfiguration.getChangelogTypes();

		// Extract the position of each type within the changelog types list
		int nTypeA = changelogTypes.indexOf(o1.getType());
		int nTypeB = changelogTypes.indexOf(o2.getType());

		// Compare the positions
		return (nTypeA > nTypeB ? 1 : (nTypeA == nTypeB ? 0 : -1));
	}

}

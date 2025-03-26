package com.gentics.changelogmanager.changelog;

import java.util.Comparator;

import com.gentics.changelogmanager.ChangelogManagerException;

/**
 * Compares two changelog versions
 * 
 * @author johannes2
 * 
 */
public class ChangelogComparator implements Comparator<AbstractChangelog> {

	private String[] getPartsFromVersion(AbstractChangelog changelog) throws ChangelogManagerException {
		return ChangelogUtils.parseVersion(changelog.getVersion());
	}

	/**
	 * Compare the given two version arrays
	 * 
	 * @param vals1
	 * @param vals2
	 * @return
	 */
	public static int compareVersion(String[] vals1, String[] vals2) {
		int i = 0;
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}

		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.compare(Integer.parseInt(vals1[i]), Integer.parseInt(vals2[i]));
			return diff < 0 ? 1 : diff == 0 ? 0 : -1;
		}
		return vals1.length < vals2.length ? 1 : vals1.length == vals2.length ? 0 : -1;
	}

	/**
	 * Compares the given two changelogs
	 */
	public int compare(AbstractChangelog o1, AbstractChangelog o2) {
		
		try {
			String[] vals1 = getPartsFromVersion(o1);
			String[] vals2 = getPartsFromVersion(o2);
			return compareVersion(vals1, vals2);
		} catch (ChangelogManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

}

package com.gentics.changelogmanager.changelog;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.entry.ChangelogEntry;

/**
 * Test for getting entries with skiplist, and mappings for same major/minor version and older major/minor version
 */
public class SkipListTest {
	public static final File testBaseDirectory = new File("src/test/resources/skiplist");

	@Test
	public void test() throws IOException, ChangelogManagerException {
		Changelog changelog = ChangelogUtils.createChangelogFromUnmappedEntries(new File(testBaseDirectory, "mappings"),
				new File(testBaseDirectory, "entries"), "3.0.1", "");
		assertThat(changelog.getChangelogEntries(new File(testBaseDirectory, "entries")))
				.usingElementComparatorOnFields("id")
				.containsOnly(new ChangelogEntry(null, 2L, null), new ChangelogEntry(null, 3L, null));
	}
}

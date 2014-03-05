package com.gentics.changelogmanager.changelog;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.gentics.changelogmanager.AbstractChangelogTest;
import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.entry.ChangelogEntry;

@Ignore
public class ChangelogTest extends AbstractChangelogTest {
	String version = "10.9.1";

	@BeforeClass
	public static void setupOnce() {
		ChangelogConfiguration.setBaseDirectory(testBaseDirectory, true);
	}

	@Test
	public void testNewChangelogFromUnmappedEntries() throws ChangelogManagerException, IOException {
		Changelog mapping = ChangelogUtils.createChangelogFromUnmappedEntries(testBaseDirectory, "1.2.5");
		mapping.addGenericProperty("alohaeditor-version", "0.9.3");
		ChangelogUtils.saveChangelogMapping(testBaseDirectory, mapping);
	}

	@Test
	public void testChangelogSkipListUsage() throws ChangelogManagerException, IOException {
		Changelog mapping = ChangelogUtils.createChangelogFromUnmappedEntries(testBaseDirectory, "5.13.1");
		for (ChangelogEntry changelogEntry : mapping.getChangelogEntries()) {
			System.out.println(changelogEntry.getFile());
		}
		// The 5.13.1 changelog should not contain the entries from 5.12.11 (12.bugfix, 13.bugfix) because they have already been mapped in 5.13.0
		String entryName = "13.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				!doesMappingContainEntry(entryName, mapping));
		entryName = "12.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				!doesMappingContainEntry(entryName, mapping));

		// The entries from 5.12.10 (10.bugfix, 11.bugfix) should be mapped because they have not been mapped yet in 5.13.x
		entryName = "10.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				doesMappingContainEntry(entryName, mapping));
		entryName = "11.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				doesMappingContainEntry(entryName, mapping));

		// Unmapped entries should also be listed
		entryName = "22.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				doesMappingContainEntry(entryName, mapping));
		entryName = "23.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				doesMappingContainEntry(entryName, mapping));

		// Entry on the skiplist should be handled correctly and not be added to the list
		entryName = "1.RT2213.manualchange";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				!doesMappingContainEntry(entryName, mapping));

	}

	private boolean doesMappingContainEntry(String entryName, Changelog mapping) throws ChangelogManagerException {
		for (ChangelogEntry changelogEntry : mapping.getChangelogEntries()) {
			if (entryName.equalsIgnoreCase(changelogEntry.getFile().getName())) {
				return true;
			}
		}

		return false;
	}

	@Test
	public void testChangelogNoSkipList() throws ChangelogManagerException, IOException {
		Changelog mapping = ChangelogUtils.createChangelogFromUnmappedEntries(testBaseDirectory, "5.17.4222");
		for (ChangelogEntry changelogEntry : mapping.getChangelogEntries()) {
			System.out.println(changelogEntry.getFile());
		}

		// Unmapped entries should be listed
		String entryName = "22.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				doesMappingContainEntry(entryName, mapping));
		entryName = "23.bugfix";
		assertTrue("The mapping with the name {" + entryName + "} could not be found within the given changelog.",
				doesMappingContainEntry(entryName, mapping));

	}

	@Test
	public void testChangelogFileMapping() throws ChangelogManagerException, IOException {

		List<ChangelogEntry> list = new ArrayList<ChangelogEntry>();
		ChangelogEntry entry = new ChangelogEntry(new File(testBaseDirectory, "entries/hotfix/3.RM2134.enhancement"), 2l, "feature");
		list.add(entry);

		Changelog mapping = new Changelog(version, list);
		ChangelogUtils.saveChangelogMapping(testBaseDirectory, mapping);
	}

	@Test
	public void testLoadChangeLogMapping() throws IOException, ChangelogManagerException {
		List<Changelog> mappings = ChangelogUtils.getChangelogs(testBaseDirectory, false);
		assertTrue(mappings.size() > 1);
	}

	@Test
	public void testSnapshotVersionComparison() throws ChangelogManagerException {
		ChangelogComparator comparator = new ChangelogComparator();
		Changelog o1 = new Changelog("5.12.0-SNAPSHOT");
		Changelog o2 = new Changelog("5.12.0");
		comparator.compare(o1, o2);
		comparator.compare(o2, o2);
	}

}

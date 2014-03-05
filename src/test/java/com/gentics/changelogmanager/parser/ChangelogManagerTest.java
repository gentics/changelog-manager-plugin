package com.gentics.changelogmanager.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.gentics.changelogmanager.AbstractChangelogTest;
import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.entry.ChangelogEntry;
import com.gentics.changelogmanager.entry.ChangelogEntryUtils;
import com.gentics.changelogmanager.parser.ChangelogFileParser;
import com.github.rjeschke.txtmark.Processor;

public class ChangelogManagerTest extends AbstractChangelogTest {

	@Test
	public void testChangeFileParser() throws ChangelogManagerException {

		ChangelogConfiguration.setBaseDirectory(testBaseDirectory, true);
		
		ChangelogFileParser parser = ChangelogFileParser.getInstance();
		List<ChangelogEntry> changelogEntries = new ArrayList<ChangelogEntry>();

		Collection<File> changelogEntryFiles = ChangelogEntryUtils.getChangelogEntryFiles(testBaseDirectory);
		for (File file : changelogEntryFiles) {
			System.out.println(file.getAbsolutePath());
			changelogEntries.add(parser.parserChangelogFile(file));
		}

		for (ChangelogEntry entry : changelogEntries) {
			System.out.println(entry.getId() + " " + entry.getType() + " " + entry.getTicketReference() + " = " + entry.getSource() + "\nMD:\n"
					+ Processor.process(entry.getSource()));
		}

	}

}

package com.gentics.changelogmanager.changelog;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gentics.changelogmanager.AbstractChangelogTest;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.entry.ChangelogEntry;
import com.gentics.changelogmanager.entry.ChangelogEntryUtils;

public class ChangelogEntryTest extends AbstractChangelogTest {

	@Test
	public void testChangelogEntryRendering() throws ChangelogManagerException, UnsupportedEncodingException {
		List<String> entryFileNames = new ArrayList<String>();
		entryFileNames.add("5.bugfix");
		List<ChangelogEntry> entryList = ChangelogEntryUtils.getChangelogEntryFiles(testBaseDirectory, entryFileNames);
		ChangelogEntry entry = entryList.get(0);

		String text = entry.getHTML();
		String encodedText = URLEncoder.encode(text, "UTF-8");
		System.out.println(encodedText);
		assertTrue("The pattern could not be found in the outputtext.", encodedText.contains("asdgadg%3EAdded+g"));
	}
}

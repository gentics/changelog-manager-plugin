package com.gentics.changelogmanager.entry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.parser.ChangelogFileParser;

/**
 * Class that contains some utility function that can be used when dealing with changelog entry files
 * 
 * @author johannes2
 * 
 */
public final class ChangelogEntryUtils {

	private static Map<File, Collection<File>> cachedChangelogEntryFiles = new HashMap<>();

	public static Collection<File> getChangelogEntryFiles(File baseDirectory) {
		return cachedChangelogEntryFiles.computeIfAbsent(baseDirectory, base -> {
			String[] changelogTypes = ChangelogConfiguration.getChangelogTypes().toArray(new String[0]);
			return FileUtils.listFiles(base, changelogTypes, true);
		});
	}

	/**
	 * This method will only return the changelog entries for the given list of changelog entry filenames
	 * 
	 * @param baseDirectory
	 * @param changelogEntryFilenames
	 * @return
	 * @throws ChangelogManagerException
	 */
	public static List<ChangelogEntry> getChangelogEntryFiles(File baseDirectory, List<String> changelogEntryFilenames)
			throws ChangelogManagerException {

		ChangelogFileParser parser = ChangelogFileParser.getInstance();

		Collection<File> allChangelogEntryFiles = getChangelogEntryFiles(baseDirectory);
		List<ChangelogEntry> entries = new ArrayList<ChangelogEntry>();
		// Add all changelog entries to the final array that match one of the changelog entry filenames
		for (File file : allChangelogEntryFiles) {
			if (changelogEntryFilenames.contains(file.getName())) {
				entries.add(parser.parserChangelogFile(file));
			}
		}

		Collections.sort(entries, new ChangelogEntryComparator());
		return entries;
	}
}

package com.gentics.changelogmanager.changelog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.logging.Log;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.entry.ChangelogEntry;
import com.gentics.changelogmanager.entry.ChangelogEntryUtils;
import com.gentics.changelogmanager.parser.ChangelogFileParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utils that help dealing with changelog/changelog mapping files
 * 
 * @author johannes2
 * 
 */
public class ChangelogUtils {

	public final static String CHANGELOG_MAPPING_FILE_EXTENSION = "json";

	private static Pattern versionPattern = Pattern.compile("([0-9]+).([0-9]+).([0-9]+)(.*)");

	private static Optional<Log> logger = Optional.empty();

	private static Map<Pair<File, String>, List<Changelog>> loadedChangelogs = new HashMap<>();

	public static void setLogger(Log logger) {
		ChangelogUtils.logger = Optional.ofNullable(logger);
	}

	/**
	 * Loads all changelog mappings
	 *
	 * @param mappingDirectory
	 *            directory that contains the changelog mappings
	 * @param prefix optional prefix of the changelog mappings files
	 * @param forceReload true to force reloading
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	public static List<Changelog> getChangelogs(File mappingDirectory, String prefix, boolean forceReload) throws IOException, ChangelogManagerException {
		if (!StringUtils.isEmpty(prefix)) {
			prefix = StringUtils.appendIfMissing(prefix, "_");
		}

		Pair<File, String> key = Pair.of(mappingDirectory, prefix);
		if (forceReload) {
			loadedChangelogs.remove(key);
		}

		// Only load the changelog mappings when they have not yet been loaded.
		if (!loadedChangelogs.containsKey(key)) {
			List<Changelog> mappingList = new ArrayList<Changelog>();
			Collection<File> mappingFiles = FileUtils.listFiles(mappingDirectory,
					new String[] { CHANGELOG_MAPPING_FILE_EXTENSION }, true);
			for (File mappingFile : mappingFiles) {
				if (prefix != null && !StringUtils.startsWith(mappingFile.getName(), prefix)) {
					continue;
				}
				logger.ifPresent(l -> l.debug("Loading mapping {" + mappingFile + "}"));
				String json = FileUtils.readFileToString(mappingFile, "UTF-8");
				Gson gson = new Gson();
				Changelog mapping = gson.fromJson(json, Changelog.class);
				mappingList.add(mapping);
			}

			Collections.sort(mappingList, new ChangelogComparator());
			loadedChangelogs.put(key, mappingList);
		}
		return loadedChangelogs.get(key);
	}

	/**
	 * Parses the given version string and returns an array which contains information about each version number field
	 * 
	 * @param versionString
	 * @return
	 * @throws ChangelogManagerException
	 */
	public static String[] parseVersion(String versionString) throws ChangelogManagerException {
		Matcher matcher = versionPattern.matcher(versionString);
		List<String> parts = new ArrayList<String>();
		try {
			matcher.find();
			for (int i = 1; i < matcher.groupCount(); i++) {
				parts.add(matcher.group(i));
			}
			return parts.toArray(new String[parts.size()]);
		} catch (IllegalStateException e) {
			throw new ChangelogManagerException("The version string {" + versionString + "} did not match the expected pattern {" + versionPattern
					+ "}", e);
		}
	}

	/**
	 * Returns a list of changelogs that lists the given changelog entry
	 * 
	 * @param entryFile
	 * @return
	 * @throws ChangelogManagerException
	 * @throws IOException
	 */
	private static List<Changelog> getChangelogsForEntry(File baseDirectory, File entryFile) throws ChangelogManagerException, IOException {
		if (entryFile == null) {
			throw new ChangelogManagerException("The changelog entry file can't be null");
		}
		List<Changelog> foundChangelogs = new ArrayList<Changelog>();
		List<Changelog> changelogList = getChangelogs(baseDirectory, null, false);
		for (Changelog changelog : changelogList) {
			for (ChangelogEntry entry : changelog.getChangelogEntries()) {
				if (entryFile.getName().equalsIgnoreCase(entry.getFile().getName())) {
					foundChangelogs.add(changelog);
				}
			}
		}
		return foundChangelogs;
	}

	/**
	 * Creates a new changelog which will only contain those changelog entries that are currently not mapped by any other changelogs
	 * 
	 * @param mappingsDirectory
	 * @param entriesDirectory
	 * @param version
	 *            the version that will be assigned to the new changelog
	 * @param prefix optional prefix
	 * @return created changelog
	 * @throws ChangelogManagerException
	 * @throws IOException
	 */
	public static Changelog createChangelogFromUnmappedEntries(File mappingsDirectory, File entriesDirectory,
			String version, String prefix) throws ChangelogManagerException, IOException {
		if (prefix == null) {
			prefix = "";
		}

		if (!StringUtils.isEmpty(prefix)) {
			prefix = StringUtils.appendIfMissing(prefix, "_");
		}

		Changelog changelog = new Changelog(version);

		// 1. Collect all changelog entry files for every changelog entry that has already been mapped
		List<Changelog> changelogList = getChangelogs(mappingsDirectory, prefix, false);
		List<File> mappedChangelogEntryFiles = new ArrayList<File>();
		for (Changelog currentChangelog : changelogList) {
			// Skip the changelog for the version we actually want to map.
			// This will basically lead to an update of the changelog mapping file for that particular version.
			if (currentChangelog.getVersion().equalsIgnoreCase(version)) {
				logger.ifPresent(l -> l.info("Skipping existing changelog mapping file for version " + version));
				continue;
			}
			for (ChangelogEntry currentEntry : currentChangelog.getChangelogEntries(entriesDirectory)) {
				mappedChangelogEntryFiles.add(currentEntry.getFile());
			}
		}

		// 2. Load the skiplist for the major version of the given version
		String versionParts[] = parseVersion(version);
		String majorVersion = versionParts[0] + "." + versionParts[1] + ".0";
		String skipListFilename = prefix + "skiplist_" + majorVersion + ".lst";
		logger.ifPresent(l -> l.debug("Build skiplist filename {" + skipListFilename + "} from version {" + version + "}"));
		File skipListFile = new File(mappingsDirectory, skipListFilename);
		List<String> skiplist = new ArrayList<String>();
		if (skipListFile.exists()) {
			skiplist = FileUtils.readLines(skipListFile, "UTF-8");
			int size = skiplist.size();
			logger.ifPresent(l -> l.debug(String.format("Skiplist contains %d entries", size)));
		}

		// TODO verify the changelog files and make sure there are no duplicates

		// 3. Fetch all entries that can be found
		Collection<File> allChangelogEntryFiles = ChangelogEntryUtils.getChangelogEntryFiles(entriesDirectory);

		// Read the ignorelist (if one exists) and remove all files, which are mentioned in the ignorelist
		String ignoreListFilename = prefix + "ignore.lst";
		File ignoreListFile = new File(mappingsDirectory, ignoreListFilename);
		List<String> ignoreList = new ArrayList<>();
		if (ignoreListFile.exists()) {
			ignoreList = FileUtils.readLines(ignoreListFile, "UTF-8");
			int size = ignoreList.size();
			logger.ifPresent(l -> l.debug(String.format("Ignorelist contains %d entries", size)));
		}

		// 4. Assume that all those entries are unmapped by adding them to the unmappedFile array
		Map<String, File> unmappedFiles = new HashMap<String, File>();
		for (File file : allChangelogEntryFiles) {
			if (ignoreList.contains(file.getName())) {
				logger.ifPresent(l -> l.debug(String.format("Ignoring file %s, because it is mentioned in the ignorelist", file.toString())));
				continue;
			}
			unmappedFiles.put(file.getName(), file);
		}

		// 5. Remove all files that have already been mapped except those that were not listed on the skiplist
		for (File file : mappedChangelogEntryFiles) {

			logger.ifPresent(l -> l.debug(String.format("Checking entry %s", file.getName())));

			// Check whether the file was listed in the skiplist. We skip those
			if (skiplist.contains(file.getName())) {
				logger.ifPresent(l -> l.debug("Skipping file {" + file
						+ "} because it is listed in the skiplist. It will be removed from the list of possible unmapped entries."));
				unmappedFiles.remove(file.getName());
				continue;
			}

			// All other entries have to be checked. We allow remapping of mapped entries when they were mapped in an older major version. (e.g changelog entries from
			// mapping 5.12.x are allowed if we are parsing the entries to generate the changelog for 5.13.1)
			if (unmappedFiles.containsKey(file.getName())) {

				if (skipListFile.exists()) {
					// Check whether the entry has been mapped in an older changelog
					List<Changelog> changelogsContainingEntryList = getChangelogsForEntry(entriesDirectory, file);
					boolean entryIsMappedToNewerChangelog = false;
					for (Changelog currentChangelog : changelogsContainingEntryList) {
						String majorVersionParts[] = parseVersion(majorVersion);
						logger.ifPresent(l -> l.debug("Comparing {" + majorVersion + "} with {" + currentChangelog.getVersion() + "}"));
						int compareValue = ChangelogComparator.compareVersion(majorVersionParts, ChangelogUtils.parseVersion(currentChangelog.getVersion()));
						if (compareValue >= 0) {
							logger.ifPresent(l -> l.debug("The entry {" + file.getName() + "} is mapped to an newer or equal changelog mapping for {" + majorVersion
									+ "}"));
							entryIsMappedToNewerChangelog = true;
						} else {
							logger.ifPresent(l -> l.debug("The entry {" + file.getName() + "} is mapped to an older changelog mapping than {" + majorVersion + "}"));
						}
					}

					if (entryIsMappedToNewerChangelog) {
						logger.ifPresent(l -> l.debug("Removing file {" + file
								+ "} from the list of unmapped changelog files because it is alreay mapped a changelog mapping that is >= {"
								+ version + "} / {" + majorVersion + "}"));
						unmappedFiles.remove(file.getName());
					}
				} else {
					logger.ifPresent(l -> l.debug("Removing file {" + file
							+ "} from the list of unmapped changelog files because it is already mapped to a changelog mapping."));
					unmappedFiles.remove(file.getName());
				}
			}
		}

		// Create new entry from the changelog entry files that are currently unmapped and add it to our new changelog
		ChangelogFileParser parser = ChangelogFileParser.getInstance();
		for (File currentFile : unmappedFiles.values()) {
			ChangelogEntry entry = parser.parserChangelogFile(currentFile);
			changelog.addChangelogEntry(entry);
		}

		return changelog;
	}

	/**
	 * Saves the given changelog object to a json file within the mappings directory relative to the given directory
	 * 
	 * @param changelog
	 * @param prefix optional prefix
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	public static void saveChangelogMapping(Changelog changelog, String prefix) throws IOException, ChangelogManagerException {
		if (prefix == null) {
			prefix = "";
		}

		if (!StringUtils.isEmpty(prefix)) {
			prefix = StringUtils.appendIfMissing(prefix, "_");
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(changelog);
		String version = changelog.getVersion();

		File changelogMappingFile = new File(ChangelogConfiguration.getChangelogMappingDirectory(), prefix + version + ".json");

		FileUtils.writeStringToFile(changelogMappingFile, json, "UTF-8");
	}
}

package com.gentics.changelogmanager.legacy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.changelog.Changelog;
import com.gentics.changelogmanager.entry.ChangelogEntry;
import com.gentics.changelogmanager.parser.ChangelogFileParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChangelogLegacyParser {

	public static final String NOTE = "note";
	public static final String HEADER = "header";
	public static final String ALOHA_FOOTER = "aloha-footer";
	private File targetFolder = new File("target/legacy");

	int nChange = 1;

	public static void main(String[] args) throws Exception {
		new ChangelogLegacyParser().run();
	}

	public void run() throws Exception {
		parseLegacyChangelogFiles();
	}

	public void parseCreatedFiles(String version, List<AlohaVersion> alohaVersions) throws ChangelogManagerException, IOException {
		ChangelogFileParser parser = ChangelogFileParser.getInstance();

		File entriesFolder = new File(targetFolder, "entries");
		entriesFolder.mkdirs();
		// System.out.println(version);
		File changelogFolder = new File(entriesFolder, version);
		Changelog changelog = new Changelog(version);

		// We also have changelogs that don't contain any entries.
		if (changelogFolder.exists()) {
			for (File currentFile : changelogFolder.listFiles()) {
				// System.out.println("Parsing: " + currentFile);
				ChangelogEntry entry = parser.parserChangelogFile(currentFile);
				changelog.addChangelogEntry(entry);
			}
		}
		Properties properties = new Properties();

		int i = 0;
		for (AlohaVersion alohaVersion : alohaVersions) {
			i++;
			if (alohaVersion.getDate() != null) {
				properties.put("alohaversion." + i + ".date", alohaVersion.getDate());
			}
			if (alohaVersion.getUrl() != null) {
				properties.put("alohaversion." + i + ".url", alohaVersion.getUrl());
			}
			if (alohaVersion.getVersion() != null) {
				properties.put("alohaversion." + i + ".version", alohaVersion.getVersion());
			}

			properties.put("alohaversion." + i + ".legacy", alohaVersion.isLegacy());

		}

		changelog.setGenericProperties(properties);
		saveChangelogMapping(changelog);
	}

	private void saveChangelogMapping(Changelog changelog) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(changelog);
		String version = changelog.getVersion();

		File mappingFolder = new File(targetFolder, "mappings");
		File changelogMappingFile = new File(mappingFolder, version + ".json");

		FileUtils.writeStringToFile(changelogMappingFile, json);
	}

	public void parseLegacyChangelogFiles() throws Exception {

		Collection<File> files = FileUtils.listFiles(new File("src/test/resources/testData/legacy"), new String[] { "textile" }, true);
		List<AlohaVersion> alohaVersions = null;

		for (File legacyFile : files) {
			String version = FilenameUtils.removeExtension(legacyFile.getName());
			List<String> lines = FileUtils.readLines(legacyFile);

			List<String> currentLines = new ArrayList<String>();
			String type = null;
			boolean end = false;
			for (String line : lines) {

				// We reached the end of the file
				if (line.startsWith("---") && end) {
					break;
				} else if (line.startsWith("---")) {
					parseLinesAs(currentLines, version, type);
					currentLines.clear();
					type = null;
				}
				if (type == null) {
					type = getLineType(line);
				}

				if (ALOHA_FOOTER.equalsIgnoreCase(type)) {
					end = true;
					if (getLineType(line) != null && getLineType(line).equalsIgnoreCase(ALOHA_FOOTER)) {
						alohaVersions = convertAlohaEditorLine(line);
						continue;
					}

				}
				if (!line.startsWith("---")) {
					currentLines.add(line);
				}

			}

			parseCreatedFiles(version, alohaVersions);
		}
	}

	public String getLineType(String line) {
		String type = null;

		if (line.startsWith("endprologue")) {
			type = HEADER;
		}
		if (line.startsWith("h4. !images/changelog_types/enhancement_24.png")) {
			type = ChangelogConfiguration.TYPE_ENHANCEMENT;
		}

		if (line.startsWith("h4. !images/changelog_types/security_24.png")) {
			type = ChangelogConfiguration.TYPE_SECURITY;
		}

		if (line.startsWith("h4. !images/changelog_types/bugfix_24.png")) {
			type = ChangelogConfiguration.TYPE_BUGFIX;
		}
		if (line.startsWith("WARNING: *Manual Change") || line.startsWith("WARNING: *Manual change*")) {
			type = ChangelogConfiguration.TYPE_MANUALCHANGE;
		}
		if (line.startsWith("NOTE: Potential Issue:")) {
			type = NOTE;
		}
		if (line.startsWith("WARNING: *Optional manual change*")) {
			type = ChangelogConfiguration.TYPE_OPTIONAL_MANUALCHANGE;
		}
		if (line.startsWith("NOTE: This Gentics Content.Node version includes") || line.startsWith("NOTE: For changes to Aloha Editor please see")
				|| line.startsWith("For changes to Aloha Editor")) {
			type = ALOHA_FOOTER;
		}

		return type;
	}

	private AlohaVersion handleSimplifiedAlohaLine(String line) {
		String version = "";
		String url = "";
		if (line.startsWith("http")) {
			String[] parts = line.split("Aloha Editor");
			url = parts[0];
			version = parts[1].trim();
			if (version.lastIndexOf(".") + 1 == version.length()) {
				version = version.substring(0, version.length() - 1);
			}
		} else {
			String parts[] = line.split("\":");
			url = parts[1];
			version = parts[0].replaceAll("\"Aloha Editor ", "");
		}

		String[] versionParts = version.split(" - ");
		String date = null;
		if (versionParts.length >= 2) {
			date = versionParts[1];
		}
		String alohaVersion = versionParts[0];
		return new AlohaVersion(url, alohaVersion, date);
	}

	private List<AlohaVersion> convertAlohaEditorLine(String line) throws IOException {
		List<AlohaVersion> alohaVersions = new ArrayList<AlohaVersion>();
		line = line.replaceAll("NOTE:", "");
		line = line.replaceAll("md . ", "md ");
		line = line.replaceAll("md. ", "md ");
		line = line.replaceAll("md  .", "md ");
		line = line.trim();
		line = line.replaceAll("This Gentics Content.Node version includes the ", "");
		line = line.replaceAll("This Gentics Content.Node version includes ", "");
		line = line.replaceAll("For changes to Aloha Editor please see the \"Aloha Editor Changelog\":", "");

		String[] parts = line.split("and ");
		if (parts.length == 2) {
			alohaVersions.add(handleSimplifiedAlohaLine(parts[0]));
			alohaVersions.add(handleSimplifiedAlohaLine(parts[1]));
		} else if (parts.length == 1) {
			line = parts[0];
			alohaVersions.add(handleSimplifiedAlohaLine(line));
		} else {
			throw new IOException("Unhandled case for line {" + line + "}");
		}

		return alohaVersions;
	}

	private String getTicketReference(String line) {
		String HEADLINE_REGEX = ".*\\*[\\w]*\\s?([^\\*]*)\\*.*";
		Pattern headLinePattern = Pattern.compile(HEADLINE_REGEX);
		Matcher headLineMatcher = headLinePattern.matcher(line);
		headLineMatcher.find();
		String ticketReference = headLineMatcher.group(1);
		ticketReference = ticketReference.replaceAll("\\/", "");
		return ticketReference;
	}

	/**
	 * Parses the given lines
	 * 
	 * @param lines
	 * @param version
	 * @param type
	 * @throws Exception
	 */
	private void parseLinesAs(List<String> lines, String version, String type) throws Exception {
		if (type == null) {
			for (String line : lines) {
				System.out.println("line: " + line);
			}
			System.out.println("######################");
			throw new Exception("Could not handle this part type {" + type + "} of {" + version + "} with lines: {" + lines + "}");
		}

		if (ALOHA_FOOTER.equalsIgnoreCase(type) || HEADER.equalsIgnoreCase(type)) {
			// Handle information as meta data or discard information
			return;
		}

		nChange++;

		StringBuffer buffer = new StringBuffer();

		String reference = "";
		for (String line : lines) {

			String currentType = getLineType(line);
			if (currentType == null) {
				// Normal changelog line
				buffer.append(line);
			} else if (!currentType.equalsIgnoreCase(HEADER) && !currentType.equalsIgnoreCase(ALOHA_FOOTER)) {
				// Changelog Entries

				if (ChangelogConfiguration.TYPE_NOTE.equalsIgnoreCase(currentType)) {

				} else if (ChangelogConfiguration.TYPE_MANUALCHANGE.equalsIgnoreCase(currentType)) {
					line = line.replaceAll("WARNING: *Manual Change* ", "");
					buffer.append(line);
					continue;
				} else {
					if (currentType.equalsIgnoreCase(ChangelogConfiguration.TYPE_BUGFIX)
							|| currentType.equalsIgnoreCase(ChangelogConfiguration.TYPE_ENHANCEMENT)) {
						reference = getTicketReference(line);
					}
				}
				// System.out.println("Headline: " + line);
			} else {
				throw new IOException("Unhandled type {" + currentType + "}");
			}
		}

		if (!StringUtils.isEmpty(reference)) {
			reference = reference.replaceAll("#", "");
			reference = "." + reference;
		}
		String filename = nChange + reference + "." + type.toLowerCase();
		String content = buffer.toString();
		File entriesFolder = new File(targetFolder, "entries");
		entriesFolder.mkdirs();
		File outputFolder = new File(entriesFolder, version);
		outputFolder.mkdirs();
		File changelogEntryFile = new File(outputFolder, filename);
		FileUtils.writeStringToFile(changelogEntryFile, content);
		// System.out.println(changelogEntryFile);

	}
}

package com.gentics.changelogmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ChangelogConfiguration {

	public static final String DEFAULT_BASE_DIRECTORY_PATH = "src/main/changelog";

	// Default directory names
	public static final String DEFAULT_STATIC_CONTENT_DIRECTORY_NAME = "static";
	public static final String DEFAULT_TEMPLATE_DIRECTORY_NAME = "templates";
	public static final String DEFAULT_OUTPUT_DIRECTORY_NAME = "changelog_output";
	public static final String DEFAULT_MAPPING_DIRECTORY_NAME = "mappings";
	public static final String DEFAULT_ENTRIES_DIRECTORY_NAME = "entries";

	public static final String DEFAULT_CHANGELOG_INDEX_TEMPLATE_FILENAME = "index.vm";
	public static final String DEFAULT_CHANGELOG_TEMPLATE_FILENAME = "changelog.vm";

	public static enum ParserType {
		TEXTILE, MARKDOWN
	}

	private static boolean isFoldNewlinesEnabled = true;
	private static ParserType parserType = ParserType.TEXTILE;

	private static File baseDirectory;
	private static File outputDirectory;
	private static File templateDirectory;
	private static File staticContentDirectory;
	private static File changelogMappingDirectory;
	private static File entriesDirectory;

	private static List<String> changelogTypes = new ArrayList<String>();
	private static List<String> overviewTemplateFileNames = new ArrayList<String>();
	private static List<String> perMajorVersionOverviewTemplateFileNames = new ArrayList<String>();
	private static String changelogTemplateFileName;

	private static List<Component> components;

	public static final String TYPE_ENHANCEMENT = "enhancement";
	public static final String TYPE_BUGFIX = "bugfix";
	public static final String TYPE_SECURITY = "security";
	public static final String TYPE_MANUALCHANGE = "manualchange";
	public static final String TYPE_FEATURE = "feature";
	public static final String TYPE_NOTE = "note";
	public static final String TYPE_OPTIONAL_MANUALCHANGE = "optional-manualchange";

	static {
		addOverviewTemplateFile(DEFAULT_CHANGELOG_INDEX_TEMPLATE_FILENAME);
		setChangelogTemplateFile(DEFAULT_CHANGELOG_TEMPLATE_FILENAME);
		// Default changelog types
		addChangelogTypes(TYPE_NOTE);
		addChangelogTypes(TYPE_MANUALCHANGE);
		addChangelogTypes(TYPE_OPTIONAL_MANUALCHANGE);
		addChangelogTypes(TYPE_SECURITY);
		addChangelogTypes(TYPE_FEATURE);
		addChangelogTypes(TYPE_ENHANCEMENT);
		addChangelogTypes(TYPE_BUGFIX);

		// Default base directory
		baseDirectory = new File(DEFAULT_BASE_DIRECTORY_PATH);
		updateDirectories();
		outputDirectory = new File("target/changelog_output");
	}

	/**
	 * Returns the configured parser type
	 * 
	 * @return
	 */
	public static ParserType getParserType() {
		return parserType;
	}

	public static void setParserType(ParserType type) {
		parserType = type;
	}

	/**
	 * Sets the other directories (templates, static..) relative to the base directory
	 */
	public static void updateDirectories() {
		templateDirectory = new File(baseDirectory, DEFAULT_TEMPLATE_DIRECTORY_NAME);
		staticContentDirectory = new File(baseDirectory, DEFAULT_STATIC_CONTENT_DIRECTORY_NAME);
		changelogMappingDirectory = new File(baseDirectory, DEFAULT_MAPPING_DIRECTORY_NAME);
		outputDirectory = new File(baseDirectory, DEFAULT_OUTPUT_DIRECTORY_NAME);
		entriesDirectory = new File(baseDirectory, DEFAULT_ENTRIES_DIRECTORY_NAME);
	}

	/**
	 * Returns the configured directory that contains the changelog mappings
	 * 
	 * @return
	 * @throws ChangelogManagerException
	 */
	public static File getChangelogMappingDirectory() throws ChangelogManagerException {

		if (!changelogMappingDirectory.exists() && !changelogMappingDirectory.mkdirs()) {
			throw new ChangelogManagerException("Could not create changelog mappings directory {" + changelogMappingDirectory + "}");
		}
		return changelogMappingDirectory;
	}

	/**
	 * Sets the changelog mapping directory
	 * 
	 * @param directory
	 */
	public static void setChangelogMappingDirectory(File directory) {
		changelogMappingDirectory = directory;
	}

	/**
	 * Returns the directory for static content
	 * 
	 * @return
	 */
	public static File getStaticContentDirectory() {
		return staticContentDirectory;
	}

	/**
	 * Sets the directory that contains the static content
	 * 
	 * @param directory
	 */
	public static void setStaticContentDirectory(File directory) {
		staticContentDirectory = directory;
	}

	/**
	 * Returns the changelog template directory
	 * 
	 * @return
	 * @throws ChangelogManagerException
	 */
	public static File getTemplateDirectory() throws ChangelogManagerException {

		if (!templateDirectory.exists()) {
			throw new ChangelogManagerException("Could not find the template directory {" + templateDirectory + "}");
		}
		return templateDirectory;
	}

	/**
	 * Sets the template directory
	 * 
	 * @param templateDirectory
	 * @throws ChangelogManagerException
	 */
	public static void setTemplateDirectory(File directory) throws ChangelogManagerException {
		if (!directory.exists()) {
			throw new ChangelogManagerException("Could not find the template directory {" + directory + "}");
		}
		templateDirectory = directory;
	}

	/**
	 * Returns a list of configured changelog types.
	 * 
	 * @return
	 */
	public static List<String> getChangelogTypes() {
		return changelogTypes;
	}

	/**
	 * Adds the given changelog type to the list of listed changelog types
	 * 
	 * @param type
	 */
	public static void addChangelogTypes(String type) {
		changelogTypes.add(type);
	}

	/**
	 * Sets the changelog types. Please note that all previously added changelog types will be removed.
	 * 
	 * @param types
	 */
	public static void setChangelogTypes(List<String> types) {
		changelogTypes = types;
	}

	public static File getBaseDirectory() throws ChangelogManagerException {
		if (baseDirectory == null) {
			throw new ChangelogManagerException("The basedirectory was not set");
		}
		if (!baseDirectory.exists()) {
			throw new ChangelogManagerException("The base directory {" + baseDirectory + "} could not be found.");
		}
		return baseDirectory;
	}

	/**
	 * Sets the base directory for the input files
	 * 
	 * @param directory
	 * @param updateOtherDirectories
	 *            whether all other directories should also be updated so that they are relative to the given base directory
	 */
	public static void setBaseDirectory(File directory, boolean updateOtherDirectories) {
		baseDirectory = directory;
		if (updateOtherDirectories) {
			updateDirectories();
		}
	}

	/**
	 * Sets the output directory for the html output
	 * 
	 * @param directory
	 */
	public static void setOutputDirectory(File directory) {
		outputDirectory = directory;
	}

	/**
	 * Returns the output directory in which the output will be written
	 * 
	 * @return
	 * @throws ChangelogManagerException
	 */
	public static File getOutputDirectory() throws ChangelogManagerException {

		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new ChangelogManagerException("Could create the output directory {" + outputDirectory + "}");
		}
		return outputDirectory;

	}

	/**
	 * Set the entries directory
	 * @param directory entries directory
	 */
	public static void setEntriesDirectory(File directory) {
		entriesDirectory = directory;
	}

	/**
	 * Get the entries directory. This will also check for existence and whether it is a directory (and throw a {@link ChangelogManagerException} if not)
	 * @return entries directory
	 * @throws ChangelogManagerException if the directory does not exist or is no directory
	 */
	public static File getEntriesDirectory() throws ChangelogManagerException {
		if (!entriesDirectory.exists()) {
			throw new ChangelogManagerException(String.format("Entries directory %s does not exist", entriesDirectory.toString()));
		}
		if (!entriesDirectory.isDirectory()) {
			throw new ChangelogManagerException(String.format("Entries directory %s is no directory", entriesDirectory.toString()));
		}
		return entriesDirectory;
	}

	/**
	 * Returns the list of template files
	 * 
	 * @return
	 * @throws ChangelogManagerException
	 */
	public static List<File> getOverviewTemplateFiles() throws ChangelogManagerException {

		List<File> files = new ArrayList<File>();
		for (String fileName : overviewTemplateFileNames) {
			File templateFile = new File(templateDirectory, fileName);
			if (!templateFile.exists() || !templateFile.isFile()) {
				throw new ChangelogManagerException("Could not find template file {" + templateFile + "}");
			}
			files.add(templateFile);
		}

		return files;
	}

	/**
	 * 
	 * @return
	 */
	public static File getChangelogTemplateFile() {
		File templateFile = new File(templateDirectory, changelogTemplateFileName);
		return templateFile;
	}

	/**
	 * Returns the template files that should be used for each major version
	 * 
	 * @return
	 * @throws ChangelogManagerException
	 */
	public static List<File> getPerMinorVersionOverviewTemplateFile() throws ChangelogManagerException {
		List<File> files = new ArrayList<File>();
		for (String fileName : perMajorVersionOverviewTemplateFileNames) {
			File templateFile = new File(templateDirectory, fileName);
			if (!templateFile.exists() || !templateFile.isFile()) {
				throw new ChangelogManagerException("Could not find template file {" + templateFile + "}");
			}
			files.add(templateFile);
		}
		return files;
	}

	/**
	 * Adds the given file to the list of overview template files. It is expected that this file exists within the configured template folder
	 * 
	 * @param fileName
	 */
	public static void addOverviewTemplateFile(String fileName) {
		overviewTemplateFileNames.add(fileName.trim());
	}

	public static void clearOverviewTemplateFileNames() {
		overviewTemplateFileNames.clear();
	}

	public static void setChangelogTemplateFile(String name) {
		changelogTemplateFileName = name;
	}

	public static void addPerMajorVersionOverviewTemplateFile(String name) {
		perMajorVersionOverviewTemplateFileNames.add(name.trim());
	}

	/**
	 * Set the flag for newline folding
	 * 
	 * @param flag
	 */
	public static void setFoldNewlinesEnabled(boolean flag) {
		isFoldNewlinesEnabled = flag;
	}

	/**
	 * Returns the config flag whether newlines should be folded during rendering of the changelog entry code
	 * 
	 * @return
	 */
	public static boolean isFoldNewlinesEnabled() {
		return isFoldNewlinesEnabled;
	}

	public static void setComponents(List<Component> components) {
		ChangelogConfiguration.components = components;
	}

	public static List<Component> getComponents() {
		return components;
	}

	public static boolean hasComponents() {
		return components != null;
	}
}

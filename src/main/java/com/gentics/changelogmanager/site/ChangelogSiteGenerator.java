package com.gentics.changelogmanager.site;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.Component;
import com.gentics.changelogmanager.changelog.AbstractChangelog;
import com.gentics.changelogmanager.changelog.Changelog;
import com.gentics.changelogmanager.changelog.ChangelogComparator;
import com.gentics.changelogmanager.changelog.ChangelogUtils;
import com.gentics.changelogmanager.changelog.ComponentChangelog;
import com.gentics.changelogmanager.changelog.VersionUtils;

/**
 * This class is responsible for rendering the velocity templates to html files.
 * 
 * @author johannes2
 * 
 */
public class ChangelogSiteGenerator {

	public static final String LOGGER_NAME = "console";

	private static Logger log = Logger.getLogger(ChangelogSiteGenerator.class);

	private String title;
	private boolean strictMode = true;

	/**
	 * List of general changelog (if no components are used)
	 */
	private List<Changelog> changelogs;

	/**
	 * List of component changelogs
	 */
	private List<ComponentChangelog> componentChangelogs;

	/**
	 * Returns a list of ordered changelogs
	 * 
	 * @return
	 */
	public List<Changelog> getChangelogs() {
		return changelogs;
	}

	public List<ComponentChangelog> getComponentChangelogs() {
		return componentChangelogs;
	}

	/**
	 * Get a sorted map containing the changelogs for all minor versions
	 * @return map of version to list of changelogs
	 */
	public Map<String, List<Changelog>> getMinorVersions() {
		Map<String, List<Changelog>> minorVersions = new HashMap<>();

		for (Changelog changelog : getChangelogs()) {
			String version = changelog.getVersion();
			if (version.indexOf("SNAPSHOT") < 0) {
				version = VersionUtils.getMinorVersion(version);
				minorVersions.computeIfAbsent(version, key -> new ArrayList<>()).add(changelog);
			}
		}

		return sortByComparator(minorVersions);
	}

	/**
	 * Get a sorted map containing the changelogs for all minor versions
	 * @return
	 */
	public Map<String, List<ComponentChangelog>> getMinorVersionsForComponents() {
		Map<String, List<ComponentChangelog>> minorVersions = new HashMap<>();

		for (ComponentChangelog changelog : getComponentChangelogs()) {
			String version = changelog.getVersion();
			if (version.indexOf("SNAPSHOT") < 0) {
				version = VersionUtils.getMinorVersion(version);
				minorVersions.computeIfAbsent(version, key -> new ArrayList<>()).add(changelog);
			}
		}

		return sortByComparator(minorVersions);
	}

	private static <T> Map<String, T> sortByComparator(Map<String, T> unsortMap) {

		List<Map.Entry<String, T>> list = new LinkedList<>(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator<Map.Entry<String, T>>() {
			public int compare(Map.Entry<String, T> o1, Map.Entry<String, T> o2) {
				String key = o1.getKey();
				String key2 = o2.getKey();
				try {
					String[] vals1 = ChangelogUtils.parseVersion(key);
					String[] vals2 = ChangelogUtils.parseVersion(key2);
					return ChangelogComparator.compareVersion(vals1, vals2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}
		});

		Map<String, T> sortedMap = new LinkedHashMap<>();
		for (Map.Entry<String, T> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	/**
	 * Creates a new changelog site generator. The changelog mappings are loaded at this stage.
	 * 
	 * @param baseDirectory
	 * @param outputDirectory
	 * @param title
	 * @throws IOException
	 */
	public ChangelogSiteGenerator(String title) throws IOException, ChangelogManagerException {

		if (StringUtils.isEmpty(title)) {
			throw new ChangelogManagerException("Please specifiy a valid title for the changelog. The current title is empty or null.");
		}
		this.title = title;

		File outputDirectory = ChangelogConfiguration.getOutputDirectory();
		// Create output directory
		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new ChangelogManagerException("Could not create output directory.");
		}

		if (ChangelogConfiguration.hasComponents()) {
			Map<String, ComponentChangelog> componentChangelogsPerVersion = new HashMap<>();

			for (Component component : ChangelogConfiguration.getComponents()) {
				List<Changelog> changes = ChangelogUtils.getChangelogs(ChangelogConfiguration.getChangelogMappingDirectory(), component.getId(), true);
				if (log.isDebugEnabled()) {
					log.debug(String.format("Found %d changelog mappings for component %s", changes.size(), component.getName()));
				}
				if (changes.size() == 0) {
					throw new ChangelogManagerException(String.format(
							"There are no changelogs to be generated for component %s. No changelog mapping was found in the mappings directory.",
							component.getName()));
				}

				for (Changelog changelog : changes) {
					String version = changelog.getVersion();
					componentChangelogsPerVersion.computeIfAbsent(version, key -> {
						try {
							ComponentChangelog compChangelog = new ComponentChangelog(key);
							compChangelog.setDate(changelog.getDate());
							compChangelog.setGenericProperties(changelog.getGenericProperties());
							return compChangelog;
						} catch (ChangelogManagerException e) {
							throw new RuntimeException(e);
						}
					}).addChangelog(component.getId(), changelog);
				}
			}

			// transform to sorted list
			componentChangelogs = new ArrayList<>(componentChangelogsPerVersion.values());
			Collections.sort(componentChangelogs, new ChangelogComparator());
		} else {
			changelogs = ChangelogUtils.getChangelogs(ChangelogConfiguration.getChangelogMappingDirectory(), null, true);
			if (log.isDebugEnabled()) {
				log.debug("Found " + changelogs.size() + " changelog mappings.");
			}
			if (changelogs.size() == 0) {
				throw new ChangelogManagerException("There are no changelogs to be generated. No changelog mapping was found in the mappings directory.");
			}
		}
	}

	/**
	 * Enable the strict rendering mode
	 */
	public void enableStrictMode() {
		log.info("Enabling strict mode for velocity rendering. Buckle up!");
		strictMode = true;
	}

	/**
	 * Disable the strict rendering mode
	 */
	public void disableStrictMode() {
		log.info("Disabling strict mode for velocity rendering.");
		strictMode = false;
	}

	/**
	 * Invokes the site generator that will generate the html files
	 * 
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	public void run() throws IOException, ChangelogManagerException {
		if (ChangelogConfiguration.hasComponents()) {
			Map<String, List<ComponentChangelog>> minorVersions = getMinorVersionsForComponents();

			for (File templateFile : ChangelogConfiguration.getOverviewTemplateFiles()) {
				String outputFilename = FilenameUtils.removeExtension(templateFile.getName()) + ".html";
				File outputFile = new File(ChangelogConfiguration.getOutputDirectory(), outputFilename);
				VelocityContext context = new VelocityContext();
				context.put("components", ChangelogConfiguration.getComponents());
				context.put("changelogs", getComponentChangelogs());
				context.put("minorVersions", minorVersions);
				try {
					renderTemplate(templateFile, outputFile, context);
				} catch (Exception e) {
					throw new ChangelogManagerException("Exception occured while rendering template {" + templateFile + "} to {" + outputFile + "}", e);
				}
			}

			for (String minorVersion : minorVersions.keySet()) {
				List<ComponentChangelog> changelogs = minorVersions.get(minorVersion);
				File outputDir = new File(ChangelogConfiguration.getOutputDirectory(), minorVersion);
				for (File perMinorVersionTemplateFile : ChangelogConfiguration.getPerMinorVersionOverviewTemplateFile()) {
					String outputFilename = FilenameUtils.removeExtension(perMinorVersionTemplateFile.getName()) + ".html";
					File outputFile = new File(outputDir, outputFilename);
					VelocityContext context = new VelocityContext();
					context.put("changelogsSubset", minorVersions.get(minorVersion));
					context.put("minorversion", minorVersion);
					renderTemplate(perMinorVersionTemplateFile, outputFile, context);
				}

				generateChangelogs(outputDir, changelogs);
			}
		} else {
			Map<String, List<Changelog>> minorVersions = getMinorVersions();
			for (File templateFile : ChangelogConfiguration.getOverviewTemplateFiles()) {
				String outputFilename = FilenameUtils.removeExtension(templateFile.getName()) + ".html";
				File outputFile = new File(ChangelogConfiguration.getOutputDirectory(), outputFilename);
				VelocityContext context = new VelocityContext();
				context.put("changelogs", getChangelogs());
				context.put("minorVersions", minorVersions);
				try {
					renderTemplate(templateFile, outputFile, context);
				} catch (Exception e) {
					throw new ChangelogManagerException("Exception occured while rendering template {" + templateFile + "} to {" + outputFile + "}", e);
				}
			}

			for (String minorVersion : minorVersions.keySet()) {
				List<Changelog> changelogs = minorVersions.get(minorVersion);
				File outputDir = new File(ChangelogConfiguration.getOutputDirectory(), minorVersion);
				for (File perMinorVersionTemplateFile : ChangelogConfiguration.getPerMinorVersionOverviewTemplateFile()) {
					String outputFilename = FilenameUtils.removeExtension(perMinorVersionTemplateFile.getName()) + ".html";
					File outputFile = new File(outputDir, outputFilename);
					VelocityContext context = new VelocityContext();
					context.put("changelogsSubset", minorVersions.get(minorVersion));
					context.put("minorversion", minorVersion);
					renderTemplate(perMinorVersionTemplateFile, outputFile, context);
				}
				generateChangelogs(outputDir, changelogs);
			}
		}
		copyStaticContentToOutputDirectory();
	}

	/**
	 * Renders the given templateFile to the defined outputfile using the given velocity context
	 * 
	 * @param templateFile
	 * @param outputFile
	 * @param context
	 * @throws ParseErrorException
	 * @throws MethodInvocationException
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	private void renderTemplate(File templateFile, File outputFile, VelocityContext context) throws ParseErrorException, MethodInvocationException,
			ResourceNotFoundException, IOException, ChangelogManagerException {

		context.put("changelogTitle", title);

		StringWriter writer = new StringWriter();

		VelocityEngine vEngine = new VelocityEngine();

		// Configure the logger
		vEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
		vEngine.setProperty("runtime.log.logsystem.log4j.logger", LOGGER_NAME);

		vEngine.setProperty("runtime.references.strict", strictMode);
		vEngine.init();

		boolean wasRenderedSuccessful = vEngine.evaluate(context, writer, "com.gentics.changelogmanager",
				new StringReader(FileUtils.readFileToString(templateFile, "UTF-8")));
		if (!wasRenderedSuccessful) {
			throw new ChangelogManagerException("The template {" + templateFile + "} did not render successfully.");
		}
		String output = writer.toString();
		outputFile.getParentFile().mkdirs();
		FileUtils.writeStringToFile(outputFile, output, "UTF-8");
	}

	/**
	 * Generates a changelog html file for each changelog mapping
	 * 
	 * @param outputdir
	 *            The output directory
	 * @param changelogs
	 *            Changelogs that should be rendered to the outputdirectory
	 * 
	 * @throws ParseErrorException
	 * @throws MethodInvocationException
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	private void generateChangelogs(File outputdir, List<? extends AbstractChangelog> changelogSubSet) throws ParseErrorException, MethodInvocationException,
			ResourceNotFoundException, IOException, ChangelogManagerException {

		for (AbstractChangelog changelog : changelogSubSet) {
			VelocityContext context = new VelocityContext();
			context.put("changelog", changelog);
			context.put("changelogsSubset", changelogSubSet);
			context.put("components", ChangelogConfiguration.getComponents());
			File templateFile = ChangelogConfiguration.getChangelogTemplateFile();
			File outputFile = new File(outputdir, changelog.getVersion() + ".html");
			renderTemplate(templateFile, outputFile, context);
		}
	}

	/**
	 * Copies all the static content like javascript and css to the output directory
	 * 
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	private void copyStaticContentToOutputDirectory() throws IOException, ChangelogManagerException {
		File staticContentDirectory = ChangelogConfiguration.getStaticContentDirectory();
		File outputDirectory = ChangelogConfiguration.getOutputDirectory();
		FileUtils.copyDirectoryToDirectory(staticContentDirectory, outputDirectory);
	}

}

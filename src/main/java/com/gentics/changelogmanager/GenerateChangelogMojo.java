package com.gentics.changelogmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.gentics.changelogmanager.changelog.Changelog;
import com.gentics.changelogmanager.changelog.ChangelogUtils;
import com.gentics.changelogmanager.site.ChangelogSiteGenerator;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public class GenerateChangelogMojo extends AbstractMojo {

	public static final String DEFAULT_OUTPUT_DIRECTORY_NAME = "output";

	@Parameter
	private boolean includeProjectProperties;

	@Parameter
	private boolean foldNewlinesInEntries;
	
	@Parameter
	private File baseDirectory;

	@Parameter
	private File outputDirectory;

	@Parameter
	private String changelogTitle;

	@Parameter
	private String changelogTypes;

	@Parameter
	private String overviewTemplateFiles;

	@Parameter
	private String perMajorVersionTemplateFiles;

	@Parameter
	private String perMajorVersionChangelogFile;

	@Parameter
	private String changelogVersion;

	@Parameter
	private boolean strictRenderMode;

	@Parameter
	private boolean allowEmptyChangelog;

	@Parameter
	private Properties properties;

	@Parameter(defaultValue = "${project}")
	private MavenProject mavenProject;

	private void configure() throws MojoExecutionException {
		if (baseDirectory != null) {
			ChangelogConfiguration.setBaseDirectory(baseDirectory, true);
		}

		if (outputDirectory != null) {
			ChangelogConfiguration.setOutputDirectory(outputDirectory);
		}

		ChangelogConfiguration.setFoldNewlinesEnabled(foldNewlinesInEntries);

		// TODO set and configure the parser (Textile or Markdown)

		try {
			getLog().info(
					"Generating changelog files from {" + ChangelogConfiguration.getBaseDirectory() + "} into output directory {"
							+ ChangelogConfiguration.getOutputDirectory() + "}");
		} catch (ChangelogManagerException e) {
			throw new MojoExecutionException("Error occured while handling configured directories.", e);
		}

		// Add changelog types if some were specified
		if (!StringUtils.isEmpty(changelogTypes)) {
			String[] types = changelogTypes.split(",");
			ChangelogConfiguration.setChangelogTypes(new ArrayList<String>());
			for (String type : types) {
				ChangelogConfiguration.addChangelogTypes(type);
			}
		}

		// Add overview template files if some were specified
		if (!StringUtils.isEmpty(overviewTemplateFiles)) {
			String[] fileNames = overviewTemplateFiles.split(",");

			// Remove the default template files
			ChangelogConfiguration.clearOverviewTemplateFileNames();
			for (String fileName : fileNames) {
				ChangelogConfiguration.addOverviewTemplateFile(fileName);
			}
		}

		// Add perMajorVersion files
		if (!StringUtils.isEmpty(perMajorVersionTemplateFiles)) {
			String[] fileNames = perMajorVersionTemplateFiles.split(",");

			// Remove the default template files
			for (String fileName : fileNames) {
				ChangelogConfiguration.addPerMajorVersionOverviewTemplateFile(fileName);
			}
		}

		if (!StringUtils.isEmpty(perMajorVersionChangelogFile)) {
			ChangelogConfiguration.setChangelogTemplateFile(perMajorVersionChangelogFile);
		}
	}

	/**
	 * Validates the maven plugin configuration
	 * 
	 * @throws MojoExecutionException
	 */
	private void validateConfiguration() throws MojoExecutionException {
		if (StringUtils.isEmpty(changelogVersion)) {
			throw new MojoExecutionException(
					"No 'changelogVersion' parameter was specified. This parameter is used to assign new changelog entries to this version.");
		}

	}

	private void mapNewEntriesToChangelog() throws MojoExecutionException {
		Changelog changelog;
		try {
			changelog = ChangelogUtils.createChangelogFromUnmappedEntries(baseDirectory, changelogVersion);

			Properties genericProperties = new Properties();
			if (properties != null) {
				genericProperties.putAll(properties);
			}
			if (includeProjectProperties) {
				genericProperties.putAll(mavenProject.getProperties());
			}

			changelog.setGenericProperties(genericProperties);

			// Only create a new changelog mapping when the changelog contains at least one new entry and when we allow empty changelogs
			if (allowEmptyChangelog || changelog.getChangelogEntries().size() != 0) {
				ChangelogUtils.saveChangelogMapping(baseDirectory, changelog);
			}

		} catch (Exception e) {
			throw new MojoExecutionException("Error occured while mapping new changelog entries to version {" + changelogVersion + "}", e);
		}
	}

	private void renderTemplates() throws MojoExecutionException {
		try {
			ChangelogSiteGenerator generator = new ChangelogSiteGenerator(changelogTitle);
			if (strictRenderMode) {
				generator.enableStrictMode();
			} else {
				generator.disableStrictMode();
			}
			// TODO validate changelog according to predefined rules
			// TODO build plain version of the changelog
			// TODO append legacy changelog
			generator.run();
		} catch (Exception e) {
			throw new MojoExecutionException("Error while building new changelog.", e);
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		configure();
		validateConfiguration();
		mapNewEntriesToChangelog();
		renderTemplates();
	}
}
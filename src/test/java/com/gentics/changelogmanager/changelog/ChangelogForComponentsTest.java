package com.gentics.changelogmanager.changelog;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.Component;
import com.gentics.changelogmanager.site.ChangelogSiteGenerator;

/**
 * Test cases for generation of changelog for components
 */
public class ChangelogForComponentsTest {
	protected final static File baseDir = new File("src/test/resources/component_base");
	protected final static File outputDir = new File("target/component_changelog_output");

	@BeforeClass
	public static void setupOnce() {
		ChangelogConfiguration.setBaseDirectory(baseDir, true);
		ChangelogConfiguration.setOutputDirectory(outputDir);
		ChangelogConfiguration.addPerMajorVersionOverviewTemplateFile("majorversion/index.vm");

		ChangelogConfiguration.setComponents(
			Arrays.asList(
				new Component().setId("cms").setName("Gentics CMS").setEntriesDirectory(new File("src/test/resources/cms/entries")),
				new Component().setId("mesh").setName("Gentics Mesh").setEntriesDirectory(new File("src/test/resources/mesh/entries")),
				new Component().setId("meshplugins").setName("Gentics Mesh Plugins").setEntriesDirectory(new File("src/test/resources/meshplugins/entries"))
			));
	}

	@AfterClass
	public static void tearDownOnce() {
		ChangelogConfiguration.setComponents(null);
	}

	@Before
	public void setup() throws IOException {
		if (FileUtils.isDirectory(outputDir)) {
			FileUtils.cleanDirectory(outputDir);
		}
	}

	@Test
	public void test() throws IOException, ChangelogManagerException {
		ChangelogSiteGenerator generator = new ChangelogSiteGenerator("Gentics CMP Changelog");
		generator.run();
	}
}

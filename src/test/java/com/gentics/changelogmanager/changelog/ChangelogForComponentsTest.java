package com.gentics.changelogmanager.changelog;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.Component;
import com.gentics.changelogmanager.entry.ChangelogEntry;
import com.gentics.changelogmanager.site.ChangelogSiteGenerator;

/**
 * Test cases for generation of changelog for components
 */
public class ChangelogForComponentsTest {
	protected final static File baseDir = new File("src/test/resources/component_base");
	protected final static File outputDir = new File("target/component_changelog_output");

	protected final static Map<String, List<ChangelogEntry>> unmappedEntriesPerComponent = new HashMap<>();

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

		unmappedEntriesPerComponent.put("cms", Arrays.asList());
		unmappedEntriesPerComponent.put("mesh", Arrays.asList(
				new ChangelogEntry(new File("src/test/resources/mesh/entries/4.unmapped-1.bugfix"), 4L, "bugfix")
						.setSource("This is a currently unmapped bugfix.").setTicketReference("unmapped-1")));
		unmappedEntriesPerComponent.put("meshplugins", Arrays.asList());
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
	public void testSiteGeneration() throws IOException, ChangelogManagerException {
		ChangelogSiteGenerator generator = new ChangelogSiteGenerator("Gentics CMP Changelog");
		generator.run();

		// TODO assertions
	}

	@Test
	public void testGetUnmappedChanges() throws ChangelogManagerException, IOException {
		String changelogVersion = "8.1.0";
		for (Component component : ChangelogConfiguration.getComponents()) {
			Changelog componentChangelog = ChangelogUtils.createChangelogFromUnmappedEntries(
					ChangelogConfiguration.getChangelogMappingDirectory(), component.getEntriesDirectory(),
					changelogVersion, component.getId());
			List<ChangelogEntry> unmappedComponentChangelogEntries = componentChangelog
					.getChangelogEntries(component.getEntriesDirectory());
			assertThat(unmappedComponentChangelogEntries)
					.as(String.format("Unmapped changelog entries for %s", component.getId()))
					.containsOnlyElementsOf(unmappedEntriesPerComponent.get(component.getId()));
		}
	}
}

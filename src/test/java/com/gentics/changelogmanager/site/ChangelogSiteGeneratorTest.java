package com.gentics.changelogmanager.site;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gentics.changelogmanager.AbstractChangelogTest;
import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.changelog.Changelog;
import com.gentics.changelogmanager.changelog.ChangelogUtils;

public class ChangelogSiteGeneratorTest extends AbstractChangelogTest {

	/**
	 * Tests the changelog generation using the gcn changelog data
	 * 
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	@Test
	public void testGCNChangelogRendering() throws IOException, ChangelogManagerException {
		File testBaseDirectory = new File("src/test/resources/aloha-testdata");
		ChangelogConfiguration.setBaseDirectory(testBaseDirectory, true);
		File outputDirectory = new File("target/aloha_output");

		ChangelogConfiguration.setOutputDirectory(outputDirectory);
		ChangelogConfiguration.addOverviewTemplateFile("index.vm");
		ChangelogConfiguration.addOverviewTemplateFile("plain_merged_changelog.vm");
		ChangelogConfiguration.addOverviewTemplateFile("merged_changelog.vm");

		ChangelogConfiguration.addPerMajorVersionOverviewTemplateFile("majorversion/index.vm");
		ChangelogConfiguration.addPerMajorVersionOverviewTemplateFile("majorversion/merged_changelog.vm");
		ChangelogConfiguration.setChangelogTemplateFile("majorversion/changelog.vm");

		List<String> types = new ArrayList<String>();
		types.add("note");
		types.add("manualchange");
		types.add("optional-manualchange");
		types.add("security");
		types.add("feature");
		types.add("enhancement");
		types.add("bugfix");

		Changelog changelog = ChangelogUtils.createChangelogFromUnmappedEntries(
				ChangelogConfiguration.getChangelogMappingDirectory(), ChangelogConfiguration.getEntriesDirectory(),
				"5.14.1", null);

		// Only create a new changelog mapping when the changelog contains at least one new entry and when we allow empty changelogs
		if (changelog.getChangelogEntries().size() != 0) {
			ChangelogUtils.saveChangelogMapping(changelog, null);
		}

		ChangelogConfiguration.setChangelogTypes(types);
		ChangelogSiteGenerator generator = new ChangelogSiteGenerator("GCN Test");
		generator.disableStrictMode();
		generator.run();

	}

//	@Test
//	public void testChangelogRendering() throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException,
//			ChangelogManagerException {
//
//		ChangelogConfiguration.setBaseDirectory(testBaseDirectory, true);
//		File outputDirectory = new File("target/legacy_output");
//		ChangelogConfiguration.setOutputDirectory(outputDirectory);
//		ChangelogConfiguration.addOverviewTemplateFileName("merged_changelog.vm");
//		ChangelogConfiguration.addOverviewTemplateFileName("plain_merged_changelog.vm");
//		ChangelogSiteGenerator generator = new ChangelogSiteGenerator("testSite");
//		generator.disableStrictMode();
//		generator.run();
//
//		File testFile = new File(outputDirectory, "index.html");
//		assertTrue("The {" + testFile + "} file was not generated.", testFile.exists());
//		testFile = new File(outputDirectory, "merged_changelog.html");
//		assertTrue("The {" + testFile + "} file was not generated.", testFile.exists());
//
//	}

}

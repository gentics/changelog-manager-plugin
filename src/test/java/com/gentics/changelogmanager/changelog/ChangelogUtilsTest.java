package com.gentics.changelogmanager.changelog;

import org.junit.Test;

import com.gentics.changelogmanager.ChangelogManagerException;

import static org.junit.Assert.*;

public class ChangelogUtilsTest {

	
	@Test
	public void testChangeLogVersionParser() throws ChangelogManagerException {
		String version = "4.1.8-RC1";
		String parts[] = ChangelogUtils.parseVersion(version);
		assertEquals("4", parts[0]);
		assertEquals("1", parts[1]);
		assertEquals("8", parts[2]);
	}
}

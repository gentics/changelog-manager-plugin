package com.gentics.changelogmanager.changelog;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionUtilsTest {

	@Test
	public void testVersionUtils() {
		String version = VersionUtils.getMinorVersion("5.15.1");
		assertEquals(version, "5.15.0");
		version = VersionUtils.getMinorVersion("5.15.1-SNAPSHOT");
		assertEquals(version, "5.15.1-SNAPSHOT");
		version = VersionUtils.getMinorVersion("5.15.0");
		assertEquals(version, "5.15.0");
	}
}

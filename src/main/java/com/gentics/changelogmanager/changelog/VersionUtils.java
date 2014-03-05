package com.gentics.changelogmanager.changelog;

public final class VersionUtils {

	/**
	 * Returns the minor version for the given version string
	 * @param version
	 * @return
	 */
	public static String getMinorVersion(String version) {
		if (version.indexOf("SNAPSHOT") < 0) {
			String[] vals = version.split("\\.");
			vals[2] = "0";
			version = vals[0] + "." + vals[1] + "." + vals[2];
		}
		return version;
	}
}

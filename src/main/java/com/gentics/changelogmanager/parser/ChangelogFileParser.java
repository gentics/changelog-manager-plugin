package com.gentics.changelogmanager.parser;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.gentics.changelogmanager.entry.ChangelogEntry;

/**
 * 
 * @author johannes2
 * 
 */
public class ChangelogFileParser {

	/**
	 * Regex that should be used to validate ticket references within a changelog entry filename.
	 */
	private String ticketReferenceValidationRegex = "\\w\\w[\\d]+.*";

	/**
	 * Regex to parse the changelog filename
	 */
	private static final String CHANGELOG_FILE_REGEX = "([^\\.]*)\\.?([^\\.]*)\\.([^\\.]*)";

	private static ChangelogFileParser parser;

	private Pattern changelogRegexPattern;
	private Pattern changelogReferenceRegexPattern;

	public ChangelogFileParser() {
		changelogRegexPattern = Pattern.compile(CHANGELOG_FILE_REGEX);
		changelogReferenceRegexPattern = Pattern.compile(ticketReferenceValidationRegex);
	}

	/**
	 * Returns a changelog file parser instance.
	 * 
	 * @return changelog file parser
	 */
	public static ChangelogFileParser getInstance() {
		if (parser == null) {
			parser = new ChangelogFileParser();
		}
		return parser;
	}

	/**
	 * Parses the given changelog file and returns a parsed and validated changelog entry.
	 * 
	 * @param file
	 * @return
	 */
	public ChangelogEntry parserChangelogFile(File file) throws ChangelogManagerException {

		String fileName = file.getName();
		Matcher fileNameMatcher = changelogRegexPattern.matcher(fileName);
		Long id = new Long(0);
		try {
			fileNameMatcher.find();
			id = Long.parseLong(fileNameMatcher.group(1));
		} catch (IllegalStateException e) {
			throw new ChangelogManagerException("Could not extract changelog id from filename {" + fileName + "}");
		}

		// Determine the changelog type
		String type = fileNameMatcher.group(3);
		if (type == null || !ChangelogConfiguration.getChangelogTypes().contains(type.toLowerCase())) {
			throw new ChangelogManagerException("Error occured while parsing the changelog type from changelog entry file {" + file.getAbsolutePath()
					+ "}. The type {" + type + "} null or unkown.");
		}

		ChangelogEntry changelogEntry = new ChangelogEntry(file, id, type);

		// Validate the ticket reference when one was specified
		String ticketReference = fileNameMatcher.group(2);
		if (!StringUtils.isEmpty(ticketReference) && !changelogReferenceRegexPattern.matcher(ticketReference).matches()) {
			throw new ChangelogManagerException("The ticket reference {" + ticketReference + "} from file {" + file.getAbsolutePath()
					+ "} does not match the defined pattern {" + ticketReferenceValidationRegex + "}.");
		} else {
			changelogEntry.setTicketReference(ticketReference);
		}

		// TODO verify that this is the correct charset.
		try {
			String source = FileUtils.readFileToString(file, "UTF-8");
			// TODO validate the source using the source validator
			changelogEntry.setSource(source);

		} catch (IOException e) {
			throw new ChangelogManagerException("Error occured while reading the changelog file.", e);
		}

		return changelogEntry;
	}
}

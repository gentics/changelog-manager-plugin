package com.gentics.changelogmanager.entry;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import net.sf.textile4j.Textile;

import com.gentics.changelogmanager.ChangelogConfiguration;
import com.gentics.changelogmanager.ChangelogConfiguration.ParserType;
import com.gentics.changelogmanager.ChangelogManagerException;
import com.github.rjeschke.txtmark.Processor;

public class ChangelogEntry {

	// Fields are marked as transient so that gson will ignore them
	public transient String source;
	public transient String type;
	public transient String ticketReference;

	public File file;
	public long id;

	/**
	 * Returns the first line within the source of this changelog entry
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getHeadline() throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(this.source));
		return reader.readLine();
	}

	/**
	 * Returns the html version of the first line within the source of this changelog entry
	 * 
	 * @return
	 * @throws IOException
	 * @throws ChangelogManagerException
	 */
	public String getHTMLHeadline() throws IOException, ChangelogManagerException {

		String html = parse(getHeadline(), ChangelogConfiguration.isFoldNewlinesEnabled());
		// Ugly hack to remove <p></p>
		// TODO use regex to remove this
		ParserType type = ChangelogConfiguration.getParserType();
		if (ParserType.MARKDOWN == type) {
			html = html.substring(3);
			html = html.substring(0, html.length() - 5);
		}
		return html;
	}

	/**
	 * Creates a new changelog entry object
	 * 
	 * @param file
	 * @param id
	 * @param type
	 */
	public ChangelogEntry(File file, Long id, String type) {
		this.file = file;
		this.id = id;
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public String getSourceWithoutHeadline() {
		String trimmedSource = source.substring(source.indexOf('\n') + 1);
		return trimmedSource.replaceAll("[^\\x00-\\x7F]", "");
	}

	/**
	 * Returns the ticket reference that was assigned to this changelog entry. Please note that this can be null because the ticket refernce is an optional parameter.
	 * 
	 * @return ticket reference string
	 */
	public String getTicketReference() {
		return ticketReference;
	}

	/**
	 * Sets the ticket reference to this changelog entry.
	 * 
	 * @param ticketReference
	 */
	public void setTicketReference(String ticketReference) {
		this.ticketReference = ticketReference;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Set the source for this changelog entry
	 * 
	 * @param source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Returns the converted html for the source
	 * 
	 * @return
	 * @throws ChangelogManagerException
	 */
	public String getHTML() throws ChangelogManagerException {
		return parse(getSource(), ChangelogConfiguration.isFoldNewlinesEnabled()).replaceAll("[^\\x00-\\x7F]", "");
	}

	/**
	 * Parses the given content
	 * 
	 * @param content
	 * @param foldNewlines
	 *            Whether the newlines should be folded
	 * @return
	 * @throws ChangelogManagerException
	 */
	private static String parse(String content, boolean foldNewlines) throws ChangelogManagerException {
		if (foldNewlines) {
			content = content.replaceAll("(?m)^[ \t]*\r?\n", "###REPLACEME###");
			content = content.replaceAll("\n", " ");
			content = content.replaceAll("###REPLACEME###", "\n");
		}
		ParserType type = ChangelogConfiguration.getParserType();
		if (ParserType.TEXTILE == type) {
			Textile textile = new Textile();
			return textile.process(content);
		} else if (ParserType.MARKDOWN == type) {
			return Processor.process(content);
		}
		throw new ChangelogManagerException("Parsertype " + type + " can be handled.");
	}

	/**
	 * Returns the html without the header line
	 * 
	 * @return
	 * @throws ChangelogManagerException
	 */
	public String getHTMLWithoutHeadline() throws ChangelogManagerException {
		return parse(getSourceWithoutHeadline(), ChangelogConfiguration.isFoldNewlinesEnabled()).replaceAll("[^\\x00-\\x7F]", "");
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}

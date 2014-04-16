package com.gentics.changelogmanager.parser;

import java.util.Scanner;

import net.sf.textile4j.Textile;

import org.junit.Test;

public class ChangelogRenderingTest {

	// @Test
	// public void testRendering() {
	// Textile textile = new Textile();
	// String testText = "\"linktext\":http://www.jotschi.de";
	// System.out.println(textile.process(testText));
	// testText = "[This link](http://example.net/)";
	// System.out.println(Processor.process(testText));
	//
	// }

	@Test
	public void testRenderingParagraphs() {
		Textile textile = new Textile();
		String content = "This is a text\nWith\nLots\nof\nnewlines\n\nBut only one empty line\nJow\n* blar\n* blub\n* bli\n\nblar2";
		StringBuilder builder = new StringBuilder();
		content = content.replaceAll("(?m)^[ \t]*\r?\n", "###REPLACEME###");
		Scanner scanner = new Scanner(content);
		boolean inCodeBlock = false;
		boolean isList = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.matches(".*<code>.*") || line.matches("<pre>.*") || line.matches(".*<blockquote>.*")) {
				inCodeBlock = true;
			} else if (inCodeBlock && line.matches(".*</code>.*") || line.matches(".*</pre>.*") || line.matches(".*</blockquote>.*")) {
				inCodeBlock = false;
			}
			boolean currentLineisList = line.matches("\\s*\\*.*");
			if (isList == false && currentLineisList) {
				line = "\n" + line + "\n";
				isList = true;
			} else if (!currentLineisList) {
				line = line + " ";
				isList = false;
			} else {
				line = line + "\n";
			}
			builder.append(line);
		}
		content = builder.toString();

		content = content.replaceAll("###REPLACEME###", "\n");
		System.out.println(content);
		System.out.println("-------------");
		System.out.println(textile.process(content));
	}
}

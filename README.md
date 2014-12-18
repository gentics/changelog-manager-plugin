# Maven Changelog Plugin

The maven changelog plugin can be used to create custom changelogs.

The following components/directories are used to create the final changelog:

* `entries`: 
 Entries are the basic building blocks. An entry is basically a file that 
 contains the changelog message. Each changelog entry is a separate file. 
 The file extension and filename determines the type of the changelog entry. 
 There are for example bugfix, enhancement, manualchange and so on.

* `mappings`:
A mapping is a json file that contains the information what entries are combined 
to form one changelog. The mapping also contains additional properties that can 
be used within the rendering of the changelog template file. Skip lists also go
here (see below).

* `templates`:
The templates define how the mapping information and the entries should be 
transformed into html. You can configure the changelog overview templates by
setting the overviewTemplateFileNames tag value.
Additionally there is an 'changelog.vm' template. This template is used generate 
a single html file for each mapping.

* `static`:
This directory just contains basic static files like css and images that are used
to style the generated html changelog.


## Skip lists and the re-use of entries across branches
 
The `generate` goal will try to find unmapped changelog entries for a new changelog.
Normally, it does this by examining all existing mappings to figure out which
entries are not yet mapped.

If your project has a workflow with multiple branches that receive the same bugfixes,
you can use skip lists. The plugin distinguishes different branches by the major
and minor version numbers of the `changelogVersion` property.

Skip lists are files stored in the `mappings` folder and are named like `skiplist_x.y.0.lst`,
where x and y are the major and minor version numbers of the associated branch. The file
consists of filenames of changelog entries (without any paths), one per line.

A skip list has the following effects:

* If a skip list exists for the current branch, an entry is considered "not yet mapped",
even if it occurs in the changelog of an earlier minor version (e.g. an entry is considered
unmapped for 3.1.5, even if it is already mapped for version 3.0.27 and/or 1.2.7).

* Changelog entries mentioned in the skip list for the current branch are not considered,
even if they are unmapped.


## Example

	<plugin>
		<groupId>com.gentics</groupId>
		    <artifactId>changelog-manager-plugin</artifactId>
		<version>1.0-SNAPSHOT</version>
		<executions>
			<execution>
				<phase>generate-resources</phase>
				<goals>
					<goal>generate</goal>
				</goals>
			</execution>
		</executions>
		<configuration>
			<!-- Flag that changes the folding behaviour of newlines. When enabled newlines will be folded (Except newlines in empty lines) -->
			<foldNewlinesInEntries>true</foldNewlinesInEntries>

			<!-- Defines the base directory where the entries, mappings, templates and static folder exists -->
			<baseDirectory>${basedir}/src/changelog</baseDirectory>

			<!-- Defines the output directory for the created changelog files -->
			<outputDirectory>${basedir}/target/output</outputDirectory>

			<!-- Title of the changelog -->
			<changelogTitle>Gentics Content.Node Changelog</changelogTitle>

			<!-- This defines which entries types should be handled. Please note that the order of the entries also affects
				the sorting of the entries within the final changelog. -->
			<changelogTypes>manualchange,feature,enhancement,bugfix</changelogTypes>

			<!-- Defines the version for the new mapping that will be created for changelog entries without an existing mapping -->
			<changelogVersion>1.2.112</changelogVersion>

			<!-- Should the velocity renderer fail when eg. a null assignment is performed? -->
			<strictRenderMode>false</strictRenderMode>

			<!-- The names of the overview template files -->
			<overviewTemplateFileNames>index.vm, merged_changelog.vm, plain_merged_changelog.vm</overviewTemplateFileNames>

			<!-- Should all project properties be included? Those properties can than be used within the velocity template -->
			<includeProjectProperties>false</includeProjectProperties>
			
			<!-- Custom properties --> 
			<properties>
				<property>
					<name>alohaeditor-version</name>
					<value>0.9.3</value>
				</property>
				<property>
					<name>alohaeditor-date</name>
					<value>2012/12/12</value>
				</property>
			</properties>
		</configuration>
	</plugin>

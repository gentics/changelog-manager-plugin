= Maven Changelog Plugin =

The maven changelog plugin can be used to create custom changelogs.

The following components/directories are used to create the final changelog:

* entries: 
 Entries are the basic building blocks. An entry is basically a file that 
 contains the changelog message. Each changelog entry is a separate file. 
 The file extension and filename determines the type of the changelog entry. 
 There are for example bugfix, enhancement, manualchange and so on.

* mappings:
A mapping is a json file that contains the information what entries are combined 
to form one changelog. The mapping also contains additional properties that can 
be used within the rendering of the changelog template file.

* templates:
The templates define how the mapping information and the entries should be 
transformed into html. You can configure the changelog overview templates by
setting the overviewTemplateFileNames tag value.
Additionally there is an 'changelog.vm' template. This template is used generate 
a single html file for each mapping.

* static:
This directory just contains basic static files like css and images that are used
to style the generated html changelog.


The plugin will try to find unmapped changelog entries. It does this by examining 
the existing mappings to figure out which entries are unmapped.

== Example ==

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
		<!-- Defines the base directory where the entries, mappings, templates and static folder exists -->
		<baseDirectory>${basedir}/src/changelog</baseDirectory>
		<!-- Defines the output directory for the created changelog files -->
		<outputDirectory>${basedir}/target/output</outputDirectory>
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
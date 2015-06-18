/**
 * Copyright (C) 2013 Florian Hirsch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adorsys.forge.plugins.boottheme;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.shell.Shell;

/**
 * Fetches and Extracts External Resources
 * @author Florian Hirsch
 */
public class ResourceFetcher {

	@Inject
	private Shell shell;
	
	@Inject
	private Project project;
	
	Node resources;
	
	@PostConstruct
	public void onPostConstruct() {
		resources = XMLParser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("external-resources-config.xml"));
	}
	
	/**
	 * fetch for all Resources specified in src/main/resrouces/external-resources-config.xml
	 */
	public void fetchAll() {
		for (String resource : getAvailableResources()) {
			fetch(resource);
		}
	}
	
	/**
	 * Download a Resource like bootstrap or jquery and copy the filtered resources
	 * as in src/main/resrouces/external-resources-config.xml specified
	 * @param resourceId
	 */
	public void fetch(String resourceId) {
		Node resource = null;
		for (Node resourceNode : resources.getChildren()) {
			if (resourceNode.getSingle("id").getText().equalsIgnoreCase(resourceId)) {
				resource = resourceNode;
				break;
			}
		}
		if (resource == null) {
			MsgHandler.error(shell, String.format("No ExternalResource-Config found for '%s'", resourceId));
			return;
		}
		File downloadedFile = null;
		for (Node location : resource.getSingle("locations").getChildren()) {
			String name = null;
			try {
				name = location.getSingle("name").getText();
				downloadedFile = download(name, location.getSingle("url").getText());			
				copyAndFilter(downloadedFile, location.getSingle("filters").getChildren());
				MsgHandler.success(shell, String.format("Fetched %s successfully", name));
			} catch (IOException ex) {
				MsgHandler.error(shell, String.format("Could not fetch resource '%s'", name), ex);
				break;
			} finally {
				cleanUp(downloadedFile);
			}
		}
	}
	
	public List<String> getAvailableResources() {
		List<String> availableResources = new ArrayList<String>();
		for (Node resource : resources.getChildren()) {
			availableResources.add(resource.getSingle("id").getText());
		}
		return availableResources;
	}
	
	public Object formatedAvailableResources() {
		StringBuilder sb = new StringBuilder();
		for (String resource : getAvailableResources()) {
			sb.append("\n* ").append(resource);
		}
		return sb.toString();
	}
	
	private File download(String identifier, String url) throws IOException {	
		DirectoryResource tmpDirectoryResource = project.getProjectRoot().createTempResource();
		File tmpDir = tmpDirectoryResource.getUnderlyingResourceObject();
		File fetchedFile = new File(tmpDir, identifier);
		FileUtils.copyURLToFile(new URL(url), fetchedFile);
		return fetchedFile.getName().endsWith(".tar.gz") || fetchedFile.getName().endsWith(".zip") 
				? unzip(fetchedFile) : fetchedFile;
	}
	
	private File unzip(File file) throws IOException {
		File unzippedDir = new File(file.getParentFile().getAbsolutePath() + "-unzipped");
		if (unzippedDir.exists()) {
			FileUtils.cleanDirectory(unzippedDir);
		} else {
			unzippedDir.mkdirs();
		}
		ArchiveInputStream inputStream = null; 
		try {
			inputStream = file.getName().endsWith(".tar.gz")
					? new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))))
					: new ZipArchiveInputStream(new FileInputStream(file));
			unzip(inputStream, unzippedDir);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		if (unzippedDir.listFiles().length == 0) {
			MsgHandler.warn(shell, String.format("Downloaded file '%s' was empty!", file.getAbsolutePath()));
		}
		return unzippedDir;
	}
	
	private void unzip(ArchiveInputStream inputStream, File destDir) throws IOException {
		ArchiveEntry entry = null;
		while ((entry = inputStream.getNextEntry()) != null) {
			File outputFile = new File(destDir, entry.getName());
			if (entry.isDirectory() && !outputFile.exists()) {
				if (!outputFile.mkdirs()) {
					throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
				}
			} else {
				OutputStream outputFileStream = new FileOutputStream(outputFile);
				IOUtils.copy(inputStream, outputFileStream);
				outputFileStream.close();
			}
		}
	}
	
	private void copyAndFilter(File file, List<Node> filters) throws IOException {
		for (Node filter : filters) {
			Pattern pattern = Pattern.compile(filter.getSingle("pattern").getText());
			File destination = new File(project.getProjectRoot().getUnderlyingResourceObject(), filter.getSingle("destination").getText());;
			if (file.isFile()) {
				if (pattern.matcher(file.getAbsolutePath()).matches()) {
					FileUtils.copyFileToDirectory(file, destination);
				}
			} else {
				// list all files recursivly and make a own matching on the whole path (commons matches filename only) 
				for (File f : FileUtils.listFiles(file, FileFilterUtils.trueFileFilter(), FileFilterUtils.directoryFileFilter())) {
					if (pattern.matcher(f.getAbsolutePath()).matches()) {
						FileUtils.copyFileToDirectory(f, destination);
					}
				}
			}
		}
	}
	
	private void cleanUp(File file) {
		if (file == null) {
			return;
		}
		if (!file.isDirectory()) {
			FileUtils.deleteQuietly(file);
			return;
		}
		try {
			FileUtils.deleteDirectory(file);
		} catch (IOException e) {
			MsgHandler.warn(shell, String.format("Could not cleanUp tmpDir '%s'", file.getAbsolutePath()));
		}
	}
		
}

/**
 * Copyright (C) 2013 Florian Hirsch fhi@adorsys.de
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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

/**
 * A plugin that helps to customize bootstrap
 * @author Florian Hirsch
 */
@Alias("boottheme")
@Help("A plugin that helps to customize Bootstrap")
@RequiresProject
@RequiresFacet(WebResourceFacet.class)
public class BootthemePlugin implements Plugin {

	@Inject
	private Shell shell;
	
	@Inject
	private Event<InstallFacets> event;
	
	@Inject
	private Project project;
	
	@Inject
	private ResourceFetcher fetcher;
	
	@Inject
	private ThemeSetup setup;
	
	@SetupCommand
	@Command(value = "setup", help = "Setup a boottheme project")
	public void setup() {
		if (!project.hasFacet(WebResourceFacet.class)) {
			event.fire(new InstallFacets(WebResourceFacet.class));
		}
		setup.setup();
		fetcher.fetchAll();
		MsgHandler.info(shell, "What's next? Look at src/main/webapp/index.html and customize Bootstrap");
	}

	@Command(value = "update", help = "updates one or all external resources")
	public void update(@Option(name = "resource", description = "name of the resource" , 
						defaultValue = "all", required = false, 
						completer = ResourceFetcherCompleter.class) String resourceId, PipeOut out) {
		if (StringUtils.isBlank(resourceId) || "all".equalsIgnoreCase(resourceId)) {
			fetcher.fetchAll();
			return;
		}
		if (!fetcher.getAvailableResources().contains(resourceId)) {
			MsgHandler.error(shell, String.format("Resource '%s' not found, try one of%s", 
					resourceId, fetcher.formatedAvailableResources()));
			return;
		}
		fetcher.fetch(resourceId);
	}	
	
	@Command(value = "versions", help = "shows the versions of the used resources")
	public void versions() {
		Pattern pattern = Pattern.compile("(?!\\.)(\\d+(\\.\\d+)+)(?![\\d\\.])");
		File webresources = project.getFacet(WebResourceFacet.class).getWebRootDirectory().getUnderlyingResourceObject();
		StringBuilder versions = new StringBuilder("Using Versions:\n");
		for (File file : new File(webresources, "resources/theme/js").listFiles()) {
			try {
				String content = IOUtils.toString(file.toURI().toURL());
				Matcher matcher = pattern.matcher(content);
				if (matcher.find()) {
					String lib = file.getName().split("\\.")[0];
					String version = matcher.group(1);
					versions.append(String.format("* %s: %s\n", lib, version));
				}
			} catch (Exception ex) {
				MsgHandler.error(shell, "Could not parse Versions", ex);
			}
		}
		MsgHandler.info(shell, versions.toString());
	}
	
}

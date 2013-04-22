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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.shell.Shell;

/**
 * does the setup for you theme
 * @author Florian Hirsch
 */
public class ThemeSetup {
	
	@Inject
	private Shell shell;
	
	@Inject
	private Project project;

	private VelocityEngine velocityEngine;
	
	@PostConstruct
	public void onPostConstruct() {
		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.JdkLogChute" );
	}
	
	/**
	 * Copys the scaffold-resources and enriches the pom.xml
	 */
	public void setup() {
		try {
			copyResources();
		} catch (IOException ex) {
			MsgHandler.error(shell, "Could not copy resources", ex);
		} catch (URISyntaxException ex) {
			MsgHandler.error(shell, "Could not copy resources", ex);
		}
		configurePom();
	}

	private void copyResources() throws IOException, URISyntaxException {
		WebResourceFacet webResourceFacet = project.getFacet(WebResourceFacet.class);
		VelocityContext ctx = createVelocityContext();
		
		URL resourceDir = Thread.currentThread().getContextClassLoader().getResource("scaffold");
		if (! "jar".equals(resourceDir.getProtocol())) {
			MsgHandler.warn(shell, "Resources not found in JAR-File");
			return; // seems to be a unit test :/
		}
		String jarPath = resourceDir.getPath().substring(5, resourceDir.getPath().indexOf("!"));
		JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
        	JarEntry entry = entries.nextElement();
        	String filename = entry.getName();     	
        	if (!filename.contains("scaffold/") || filename.endsWith("/")) {
        		continue;
        	}
        	StringWriter writer = new StringWriter();
        	if (filename.endsWith(".vm")) {
				velocityEngine.mergeTemplate(filename, "UTF-8", ctx, writer);
				filename = filename.replaceAll(".vm", "");
			} else {
				IOUtils.copy(jar.getInputStream(entry), writer, "UTF-8");
			}
        	webResourceFacet.createWebResource(writer.toString(), filename.replaceFirst("scaffold", ""));
        }
        jar.close();
        
		MsgHandler.success(shell, "copied webresources");
	}
	
	private VelocityContext createVelocityContext() {
		VelocityContext velocityContext = new VelocityContext();
		Model pom = project.getFacet(MavenCoreFacet.class).getPOM();
		String title = pom.getName();
		if (title == null) {
			title = pom.getArtifactId();
		}
		velocityContext.put("title", title);
		String description = pom.getDescription();
		if (description == null) {
			description = "Your customized Bootstrap Theme";
		}
		velocityContext.put("description", description);
		velocityContext.put("version", pom.getVersion());
		return velocityContext;
	}
	
	/**
	 * adds https://github.com/marceloverdijk/lesscss-maven-plugin
	 */
	private void configurePom() {
		MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mavenCoreFacet.getPOM();
		List<Plugin> plugins = pom.getBuild().getPlugins();
		
		try {
			addLessPlugin(plugins);
			configureWarPlugin(plugins);
		} catch (XmlPullParserException ex) {
			MsgHandler.error(shell, "Could not Configure pom.xml", ex);
			return;
		} catch (IOException ex) {
			MsgHandler.error(shell, "Could not Configure pom.xml", ex);
			return;
		}
		
		mavenCoreFacet.setPOM(pom);
		MsgHandler.success(shell, "configured pom.xml");
	}
	
	private void addLessPlugin(List<Plugin> plugins) throws XmlPullParserException, IOException {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.lesscss");
		plugin.setArtifactId("lesscss-maven-plugin");
		
		if (plugins.contains(plugin)) {
			MsgHandler.info(shell, "lesscss-maven-plugin already configured in pom!");
			return;
		}
		
		plugin.setVersion("1.3.3");

		InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream("lesscss-plugin-config.xml");
		plugin.setConfiguration(Xpp3DomBuilder.build(config, "UTF-8"));

		PluginExecution execution = new PluginExecution();
		execution.setGoals(Arrays.asList("compile"));
		plugin.setExecutions(Arrays.asList(execution));
		
		plugins.add(plugin);
	}

	private void configureWarPlugin(List<Plugin> plugins) throws XmlPullParserException, IOException {
		Plugin warPlugin = null;
		for (Plugin plugin : plugins) {
			if ("maven-war-plugin".equals(plugin.getArtifactId())) {
				warPlugin = plugin;
				break;
			}
		}

		InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream("war-plugin-config.xml");
		Xpp3Dom existingConfig = (Xpp3Dom) warPlugin.getConfiguration();
		Xpp3Dom mergedConfig = Xpp3DomUtils.mergeXpp3Dom(Xpp3DomBuilder.build(config, "UTF-8"), existingConfig);
		warPlugin.setConfiguration(mergedConfig);
	}
	
}
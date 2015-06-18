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

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.Plugin;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.junit.Test;

/**
 * @author Florian Hirsch
 */
public class ThemeSetupTest extends AbstractBaseTest {

	@Inject
	private ThemeSetup setup;
	
	@Inject
	private FileChecker checker;
	
	@Test
	public void testSetup() throws Exception {
		initializeProject(PackagingType.WAR);
		queueInputLines("y");
		setup.setup();

		List<Plugin> plugins = getProject().getFacet(MavenCoreFacet.class).getPOM().getBuild().getPlugins();
		
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.lesscss");
		plugin.setArtifactId("lesscss-maven-plugin");	
		assertTrue("lesscss-maven-plugin not found in pom.xml", plugins.contains(plugin));
	}

}

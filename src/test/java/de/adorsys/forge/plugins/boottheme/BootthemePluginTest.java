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

import org.jboss.forge.project.packaging.PackagingType;
import org.junit.Test;

/**
 * @author Florian Hirsch
 */
public class BootthemePluginTest extends AbstractBaseTest {
	
	@Test
	public void testSetup() throws Exception {
		initializeProject(PackagingType.WAR);
		queueInputLines("y");
		getShell().execute("boottheme setup");
	}
	
	@Test
	public void testUpdate() throws Exception {
		initializeProject(PackagingType.WAR);
		queueInputLines("y");
		getShell().execute("boottheme update");
	}
	
	@Test
	public void testVersions() throws Exception {
		initializeProject(PackagingType.WAR);
		queueInputLines("y");
		getShell().execute("boottheme setup");
		getShell().execute("boottheme versions");
	}
	
}

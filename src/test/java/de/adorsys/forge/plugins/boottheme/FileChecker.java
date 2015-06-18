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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.project.Project;

/**
 * @author Florian Hirsch
 */
public class FileChecker {

	@Inject
	private Project project;
	
	public void checkFile(String relativePath) {
		File file = new File(project.getProjectRoot().getUnderlyingResourceObject(), relativePath);
		assertTrue(String.format("'%s' not found", file.getAbsolutePath()), file.exists());
	}
	
	public void countFiles(String relativePath, int expected) {
		File dir = new File(project.getProjectRoot().getUnderlyingResourceObject(), relativePath);
		assertEquals(String.format("Found wrong number of files in '%s'", dir.getAbsolutePath()), expected, dir.list().length);
	}
	
}

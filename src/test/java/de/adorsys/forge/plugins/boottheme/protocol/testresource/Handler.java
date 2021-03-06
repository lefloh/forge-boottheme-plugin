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
package de.adorsys.forge.plugins.boottheme.protocol.testresource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * URLHandler for /src/test/resources
 * avoids serving the external resources per http in tests
 * @author Florian Hirsch
 */
public class Handler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		String file = String.format("file://%s/src/test/resources/%s", System.getProperty("user.dir"), url.getPath());
		return new URL(file).openConnection();
	}

}

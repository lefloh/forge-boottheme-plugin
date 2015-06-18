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

import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;

/**
 * @author Florian Hirsch
 */
public class MsgHandler {

	public static void success(ShellPrintWriter out, String msg) {
		ShellMessages.success(out, msg);
	}
	
	public static void info(ShellPrintWriter out, String msg) {
		ShellMessages.info(out, msg);
	}
	
	public static void warn(ShellPrintWriter out, String msg) {
		ShellMessages.warn(out, msg);
	}
	
	public static void error(ShellPrintWriter out, String msg) {
		ShellMessages.error(out, msg);
	}
	
	public static void error(ShellPrintWriter out, Exception ex) {
		ShellMessages.error(out, ex.getClass().getSimpleName() + ":\n" + ex.getMessage());
	}
	
	public static void error(ShellPrintWriter out, String msg, Exception ex) {
		ShellMessages.error(out, msg + "\n" + ex.getClass().getSimpleName() + ":\n" + ex.getMessage());
	}
}

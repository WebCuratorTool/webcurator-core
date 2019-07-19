/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.common.ui.profiles.renderers;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.webcurator.core.profiles.ProfileElement;
import org.webcurator.ui.profiles.renderers.RendererFilter;

/**
 * Renders a text area input value.
 * @author bbeaumont
 *
 */
public class TextFieldRenderer extends Renderer {

	/* (non-Javadoc)
	 * @see org.webcurator.common.ui.profiles.renderers.Renderer#render(org.webcurator.core.profiles.ProfileElement, javax.servlet.jsp.PageContext, org.webcurator.ui.profiles.renderers.RendererFilter)
	 */
	public void render(ProfileElement element, PageContext context, RendererFilter filter)
			throws IOException {
		JspWriter out = context.getOut();

		out.print("<textarea name=\"");
		out.print(element.getAbsoluteName());
		out.print("\">");
		out.print(element.getValue());
		out.print("</textarea><br/>");
	}

}

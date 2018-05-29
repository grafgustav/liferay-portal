/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.frontend.taglib.dynamic.section;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.taglib.servlet.PipingServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * @author Matthew Tambara
 */
public abstract class BaseJSPDynamicSection implements DynamicSection {

	@Override
	public StringBundler modify(StringBundler sb, PageContext pageContext)
		throws IOException, ServletException {

		ServletContext servletContext = getServletContext();

		RequestDispatcher requestDispatcher =
			servletContext.getRequestDispatcher(getJspPath());

		String result = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			HttpServletResponse httpServletResponse = new PipingServletResponse(
				(HttpServletResponse)pageContext.getResponse(), outputStream);

			requestDispatcher.include(
				pageContext.getRequest(), httpServletResponse);

			result = new String(outputStream.toByteArray());
		}

		return new StringBundler(result);
	}

	protected abstract String getJspPath();

	protected abstract ServletContext getServletContext();

}
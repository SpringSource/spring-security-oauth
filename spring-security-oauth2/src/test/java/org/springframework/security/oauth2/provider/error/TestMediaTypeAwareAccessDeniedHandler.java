/*
 * Copyright 2006-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.security.oauth2.provider.error;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

/**
 * @author Dave Syer
 * 
 */
public class TestMediaTypeAwareAccessDeniedHandler {

	private MediaTypeAwareAccessDeniedHandler handler = new MediaTypeAwareAccessDeniedHandler();

	private MockHttpServletRequest request = new MockHttpServletRequest();

	private MockHttpServletResponse response = new MockHttpServletResponse();

	@Test
	public void testHandleWithJson() throws Exception {
		request.addHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
		handler.handle(request, response, new AccessDeniedException("Bad"));
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		assertEquals("{\"error\":\"Bad\"}", response.getContentAsString());
		assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
		assertEquals(null, response.getErrorMessage());
	}

	@Test
	public void testHandleWithXml() throws Exception {
		request.addHeader("Accept", MediaType.APPLICATION_XML_VALUE);
		handler.handle(request, response, new AccessDeniedException("Bad"));
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		assertEquals("<error>Bad</error>", response.getContentAsString());
		assertEquals(MediaType.APPLICATION_XML_VALUE, response.getContentType());
		assertEquals(null, response.getErrorMessage());
	}

	@Test
	public void testHandleWithEmptyAccept() throws Exception {
		handler.handle(request, response, new AccessDeniedException("Bad"));
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		assertEquals("Bad", response.getErrorMessage());
	}

	@Test
	public void testHandleWithHtmlAccept() throws Exception {
		request.addHeader("Accept", MediaType.TEXT_HTML_VALUE);
		handler.handle(request, response, new AccessDeniedException("Bad"));
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		assertEquals("Bad", response.getErrorMessage());
	}

	@Test
	public void testHandleWithHtmlAndJsonAccept() throws Exception {
		request.addHeader("Accept", String.format("%s,%s", MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON));
		handler.handle(request, response, new AccessDeniedException("Bad"));
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		assertEquals(null, response.getErrorMessage());
	}

}

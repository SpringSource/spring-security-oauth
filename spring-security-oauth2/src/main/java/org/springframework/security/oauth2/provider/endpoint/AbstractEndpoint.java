/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.provider.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author Dave Syer
 * 
 */
public class AbstractEndpoint implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private WebResponseExceptionTranslator providerExceptionHandler = new DefaultWebResponseExceptionTranslator();

	private TokenGranter tokenGranter;

	public void afterPropertiesSet() throws Exception {
		Assert.state(tokenGranter != null, "TokenGranter must be provided");
	}

	public void setProviderExceptionHandler(WebResponseExceptionTranslator providerExceptionHandler) {
		this.providerExceptionHandler = providerExceptionHandler;
	}

	public void setTokenGranter(TokenGranter tokenGranter) {
		this.tokenGranter = tokenGranter;
	}
	
	protected TokenGranter getTokenGranter() {
		return tokenGranter;
	}

	@ExceptionHandler(OAuth2Exception.class)
	public HttpEntity<OAuth2Exception> handleException(OAuth2Exception e, ServletWebRequest webRequest) throws Exception {
		return providerExceptionHandler.translate(e);
	}

}
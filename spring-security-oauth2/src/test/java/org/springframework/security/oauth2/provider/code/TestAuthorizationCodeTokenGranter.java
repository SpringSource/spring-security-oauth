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

package org.springframework.security.oauth2.provider.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.security.oauth2.provider.OAuth2Request.REDIRECT_URI;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.StoredRequest;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.InMemoryTokenStore;

/**
 * @author Dave Syer
 * 
 */
public class TestAuthorizationCodeTokenGranter {

	private DefaultTokenServices providerTokenServices = new DefaultTokenServices();

	private BaseClientDetails client = new BaseClientDetails("foo", "resource", "scope", "authorization_code",
			"ROLE_CLIENT");

	private ClientDetailsService clientDetailsService = new ClientDetailsService() {
		public ClientDetails loadClientByClientId(String clientId) throws OAuth2Exception {
			return client;
		}
	};

	private AuthorizationCodeServices authorizationCodeServices = new InMemoryAuthorizationCodeServices();
	
	private OAuth2RequestFactory requestFactory = new DefaultOAuth2RequestFactory(clientDetailsService);

	private Map<String, String> parameters = new HashMap<String, String>();

	public TestAuthorizationCodeTokenGranter() {
		providerTokenServices.setTokenStore(new InMemoryTokenStore());
	}

	@Test
	public void testAuthorizationCodeGrant() {
		
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("marissa", "koala",
				AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
		
		parameters.clear();
		parameters.put(OAuth2Request.CLIENT_ID, "foo");
		parameters.put(OAuth2Request.SCOPE, "scope");
		StoredRequest storedRequest = new StoredRequest(parameters, "foo", null, true, Collections.singleton("scope"), null, null);
		
		String code = authorizationCodeServices.createAuthorizationCode(new OAuth2Authentication(
				storedRequest, userAuthentication));
		parameters.putAll(storedRequest.getRequestParameters());
		parameters.put("code", code);
		
		TokenRequest tokenRequest = requestFactory.createTokenRequest(parameters);
				
		AuthorizationCodeTokenGranter granter = new AuthorizationCodeTokenGranter(providerTokenServices,
				authorizationCodeServices, clientDetailsService, requestFactory);
		OAuth2AccessToken token = granter.grant("authorization_code", tokenRequest);
		assertTrue(providerTokenServices.loadAuthentication(token.getValue()).isAuthenticated());
	}

	@Test
	public void testAuthorizationParametersPreserved() {
		
		parameters.clear();
		parameters.put("foo", "bar");
		parameters.put(OAuth2Request.CLIENT_ID, "foo");
		parameters.put(OAuth2Request.SCOPE, "scope");
		StoredRequest storedRequest = new StoredRequest(parameters, "foo", null, true, Collections.singleton("scope"), null, null);
		
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("marissa", "koala",
				AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
		String code = authorizationCodeServices.createAuthorizationCode(new OAuth2Authentication(
				storedRequest, userAuthentication));

		parameters.put("code", code);
		TokenRequest tokenRequest = requestFactory.createTokenRequest(parameters);
		
		AuthorizationCodeTokenGranter granter = new AuthorizationCodeTokenGranter(providerTokenServices,
				authorizationCodeServices, clientDetailsService, requestFactory);
		OAuth2AccessToken token = granter.grant("authorization_code", tokenRequest);
		StoredRequest finalRequest = providerTokenServices.loadAuthentication(token.getValue())
				.getClientAuthentication();
		assertEquals(code, finalRequest.getRequestParameters().get("code"));
		assertEquals("bar", finalRequest.getRequestParameters().get("foo"));
	}

	@Test
	public void testAuthorizationRequestPreserved() {
		
		parameters.clear();
		parameters.put(OAuth2Request.CLIENT_ID, "foo");
		parameters.put(OAuth2Request.SCOPE, "read");
		StoredRequest storedRequest = new StoredRequest(parameters, "foo", null, true, Collections.singleton("read"), Collections.singleton("resource"), null);
		
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("marissa", "koala",
				AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
		String code = authorizationCodeServices.createAuthorizationCode(new OAuth2Authentication(
				storedRequest, userAuthentication));

		parameters.put("code", code);
		TokenRequest tokenRequest = requestFactory.createTokenRequest(parameters);
		
		AuthorizationCodeTokenGranter granter = new AuthorizationCodeTokenGranter(providerTokenServices,
				authorizationCodeServices, clientDetailsService, requestFactory);
		OAuth2AccessToken token = granter.grant("authorization_code", tokenRequest);
		StoredRequest finalRequest = providerTokenServices.loadAuthentication(token.getValue())
				.getClientAuthentication();
		assertEquals("[read]", finalRequest.getScope().toString());
		assertEquals("[resource]", finalRequest.getResourceIds().toString());
		assertTrue(finalRequest.isApproved());
	}

	@Test
	public void testAuthorizationCodeGrantWithNoClientAuthorities() {
		
		parameters.clear();
		parameters.put(OAuth2Request.CLIENT_ID, "foo");
		parameters.put(OAuth2Request.SCOPE, "scope");
		StoredRequest storedRequest = new StoredRequest(parameters, "foo", Collections.<GrantedAuthority> emptySet(), true, Collections.singleton("scope"), null, null);
		
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("marissa", "koala",
				AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
		String code = authorizationCodeServices.createAuthorizationCode(new OAuth2Authentication(
				storedRequest, userAuthentication));
		parameters.put("code", code);
		TokenRequest tokenRequest = requestFactory.createTokenRequest(parameters);
		AuthorizationCodeTokenGranter granter = new AuthorizationCodeTokenGranter(providerTokenServices,
				authorizationCodeServices, clientDetailsService, requestFactory);
		OAuth2AccessToken token = granter.grant("authorization_code", tokenRequest);
		assertTrue(providerTokenServices.loadAuthentication(token.getValue()).isAuthenticated());
	}

	@Test
	public void testAuthorizationRedirectMismatch() {
		Map<String, String> initialParameters = new HashMap<String, String>();
		initialParameters.put(REDIRECT_URI, "https://redirectMe");
		//OAuth2Request initialRequest = createFromParameters(initialParameters);
		// we fake a valid resolvedRedirectUri because without the client would never come this far
		//initialRequest.setRedirectUri(initialParameters.get(REDIRECT_URI));

		parameters.clear();
		parameters.put(OAuth2Request.REDIRECT_URI, "https://redirectMe");
		parameters.put(OAuth2Request.CLIENT_ID, "foo");
		StoredRequest storedRequest = new StoredRequest(parameters, "foo", null, true, null, null, "https://redirectMe");
		
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("marissa", "koala",
				AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
		String code = authorizationCodeServices.createAuthorizationCode(new OAuth2Authentication(storedRequest,
				userAuthentication));

		Map<String, String> authorizationParameters = new HashMap<String, String>();
		authorizationParameters.put("code", code);
	
		//OAuth2Request oAuth2Request = createFromParameters(initialParameters);
		//oAuth2Request.setRequestParameters(authorizationParameters);

		TokenRequest tokenRequest = requestFactory.createTokenRequest(parameters);
		tokenRequest.setRequestParameters(authorizationParameters);
		
		AuthorizationCodeTokenGranter granter = new AuthorizationCodeTokenGranter(providerTokenServices,
				authorizationCodeServices, clientDetailsService, requestFactory);
		try {
			granter.getOAuth2Authentication(tokenRequest);
			fail("RedirectMismatchException because of null redirect_uri in authorizationRequest");
		}
		catch (RedirectMismatchException e) {
		}
	}

}

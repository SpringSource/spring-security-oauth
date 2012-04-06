/*
 * Copyright 2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.provider.token;

import java.util.Set;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;

/**
 * @author Ryan Heaton
 * @author Dave Syer
 */
public interface AuthorizationServerTokenServices {

	/**
	 * Create an access token associated with the specified credentials.
	 * 
	 * @param authentication The credentials associated with the access token.
	 * @return The access token.
	 * @throws AuthenticationException If the credentials are inadequate.
	 */
	OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException;

	/**
	 * Perform any necessary enhancements to the access token. This will be implementation-specific.
	 * 
	 * @param token the token to enhance
	 * @return the enhanced token
	 */
	void enhanceAccessToken(OAuth2AccessToken token, AuthorizationRequestHolder requestHolder);
	
	/**
	 * Perform any necessary finishing steps to the access token.
	 * 
	 * @param token the token to enhance
	 * @return the enhanced token
	 */
	void finishAccessToken(OAuth2AccessToken token);
	
	/**
	 * Refresh an access token.
	 * 
	 * @param refreshToken The details about the refresh token.
	 * @param scope the scopes requested (or null or empty to use the default)
	 * @return The (new) access token.
	 * @throws AuthenticationException If the refresh token is invalid or expired.
	 */
	OAuth2AccessToken refreshAccessToken(String refreshToken, Set<String> scope) throws AuthenticationException;

	/**
	 * Retrieve an access token stored against the provided authentication key, if it exists.
	 * 
	 * @param authentication the authentication key for the access token
	 * 
	 * @return the access token or null if there was none
	 */
	OAuth2AccessToken getAccessToken(OAuth2Authentication authentication);

}
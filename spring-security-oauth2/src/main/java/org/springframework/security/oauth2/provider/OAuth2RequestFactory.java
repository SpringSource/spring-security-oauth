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

package org.springframework.security.oauth2.provider;

import java.util.Map;

/**
 * Strategy for managing OAuth2 requests: {@link AuthorizationRequest}, {@link TokenRequest}, {@link StoredRequest}.
 * 
 * @author Dave Syer
 * @author Amanda Anganes
 * 
 */
public interface OAuth2RequestFactory {

	/**
	 * Create a new {@link AuthorizationRequest} extracting all the needed information from the incoming parameter map, and 
	 * initializing all individual fields on the {@link AuthorizationRequest} to reasonable values. When a class uses
	 * the factory to create an {@link AuthorizationRequest}, it should not need to access the parameter map directly afterwards.
	 * 
	 * Typical implementations would initialize the individual fields on the {@link AuthorizationRequest} with the values
	 * requested in the original parameter map. It may also load the client details from the client id provided and 
	 * validate the grant type and scopes, populating any fields in the request that are known only to the authorization server.
	 * 
	 * @param authorizationParameters the parameters in the request
	 * @return a new AuthorizationRequest
	 */
	AuthorizationRequest createAuthorizationRequest(Map<String, String> authorizationParameters);
	
	/**
	 * Create a new {@link StoredOAuth2Request} by extracting the needed information from the current {@link AuthorizationRequest} object.
	 * 
	 * @param request the request to be converted
	 * @return an immutable object for storage
	 */
	StoredOAuth2Request createStoredAuthorizationRequest(AuthorizationRequest request);
	
	/**
	 * Create a new {@link StoredOAuth2Request} by extracting the needed information from the current {@link TokenRequest} object.
	 * 
	 * @param tokenRequest the request to be converted
	 * @return am immutable object for storage
	 */
	StoredOAuth2Request createStoredTokenRequest(TokenRequest tokenRequest);
	
	/**
	 * Create a new {@link TokenRequest} by extracted the needed information from the incoming request parameter map.
	 * 
	 * @param requestParameters the parameters in the request
	 * @return a new TokenRequest
	 */
	TokenRequest createTokenRequest(Map<String, String> requestParameters);

	/**
	 * Create a new {@link TokenRequest} from an {@link AuthorizationRequest}. Used by the AuthorizationEndpoint during the
	 * implicit flow.
	 * 
	 * @param authorizationRequest the incoming request
	 * @return a new TokenRequest
	 */
	TokenRequest createTokenRequestFromAuthorizationRequest(AuthorizationRequest authorizationRequest);

	
	
}
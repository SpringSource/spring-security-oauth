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

package org.springframework.security.oauth2.provider.exchange;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ryan Murfitt
 */
public class TokenExchangeTokenGranter extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "token-exchange";

    private final TokenExchangeService tokenExchangeService;

    public TokenExchangeTokenGranter(TokenExchangeService tokenExchangeService,
                                     AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        this(tokenExchangeService, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    protected TokenExchangeTokenGranter(TokenExchangeService tokenExchangeService, AuthorizationServerTokenServices tokenServices,
                                        ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.tokenExchangeService = tokenExchangeService;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {

        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        String subjectToken = parameters.get("subject_token");
        String subjectTokenType = parameters.get("subject_token_type");
        String actorToken = parameters.get("actor_token"); //TODO
        String actorTokenType = parameters.get("actor_token_type"); //TODO

        TokenExchangeAuthenticationToken tokenAuth = new TokenExchangeAuthenticationToken(subjectToken, subjectTokenType);
        tokenAuth.setDetails(parameters);
        Authentication userAuth;
        try {
            userAuth = tokenExchangeService.loadUserAuthFromToken(tokenAuth);
        } catch (AccountStatusException ase) {
            //covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
            throw new InvalidGrantException(ase.getMessage());
        } catch (InvalidTokenException e) {
            // If the supplied subject token is invalid
            throw new InvalidGrantException(e.getMessage());
        }
        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException("Could not authenticate user");
        }

        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }
}

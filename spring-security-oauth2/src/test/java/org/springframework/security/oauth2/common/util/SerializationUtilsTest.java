package org.springframework.security.oauth2.common.util;

import org.company.oauth2.CustomOAuth2AccessToken;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Artem Smotrakov
 */
public class SerializationUtilsTest {

    @Test
    public void deserializeAllowedClasses() {
        deserializeAllowedClasses(new DefaultOAuth2AccessToken("access-token-" + UUID.randomUUID()));

        deserializeAllowedClasses(new OAuth2Authentication(
                new OAuth2Request(Collections.<String, String>emptyMap(), "clientId", Collections.<GrantedAuthority>emptyList(),
                        false, Collections.<String>emptySet(),
                        new HashSet<String>(Arrays.asList("resourceId-1", "resourceId-2")), "redirectUri",
                        Collections.<String>emptySet(), Collections.<String, Serializable>emptyMap()),
                new UsernamePasswordAuthenticationToken("test", "N/A")));

        deserializeAllowedClasses(new DefaultExpiringOAuth2RefreshToken(
                "access-token-" + UUID.randomUUID(), new Date()));

        deserializeAllowedClasses("xyz");
        deserializeAllowedClasses(new HashMap<String, String>());
    }

    private void deserializeAllowedClasses(Object object) {
        byte[] bytes = SerializationUtils.serialize(object);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        Object clone = SerializationUtils.deserialize(bytes);
        assertNotNull(clone);
        assertEquals(object, clone);
    }

    @Test
    public void deserializeCustomClasses() {
        OAuth2AccessToken accessToken = new CustomOAuth2AccessToken("FOO");
        byte[] bytes = SerializationUtils.serialize(accessToken);
        OAuth2AccessToken clone = SerializationUtils.deserialize(bytes);
        assertNotNull(clone);
        assertEquals(accessToken, clone);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserializeNotAllowedCustomClasses() {
        OAuth2AccessToken accessToken = new CustomOAuth2AccessToken("FOO");
        WhitelistedSerializationStrategy newStrategy = new WhitelistedSerializationStrategy();
        SerializationStrategy oldStrategy = SerializationUtils.getSerializationStrategy();
        try {
            SerializationUtils.setSerializationStrategy(newStrategy);
            byte[] bytes = SerializationUtils.serialize(accessToken);
            OAuth2AccessToken clone = SerializationUtils.deserialize(bytes);
            assertNotNull(clone);
            assertEquals(accessToken, clone);
        } finally {
            SerializationUtils.setSerializationStrategy(oldStrategy);
        }
    }
}
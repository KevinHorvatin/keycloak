/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.broker.oidc;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.ClientConnection;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.util.SimpleHttp;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.FederatedIdentity;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.flows.Flows;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pedro Igor
 */
public abstract class AbstractOAuth2IdentityProvider<C extends OAuth2IdentityProviderConfig> extends AbstractIdentityProvider<C> {
    protected static final Logger logger = Logger.getLogger(AbstractOAuth2IdentityProvider.class);

    public static final String OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String FEDERATED_ACCESS_TOKEN = "FEDERATED_ACCESS_TOKEN";
    public static final String FEDERATED_REFRESH_TOKEN = "FEDERATED_REFRESH_TOKEN";
    public static final String FEDERATED_TOKEN_EXPIRATION = "FEDERATED_TOKEN_EXPIRATION";
    protected static ObjectMapper mapper = new ObjectMapper();

    public static final String OAUTH2_PARAMETER_ACCESS_TOKEN = "access_token";
    public static final String OAUTH2_PARAMETER_SCOPE = "scope";
    public static final String OAUTH2_PARAMETER_STATE = "state";
    public static final String OAUTH2_PARAMETER_RESPONSE_TYPE = "response_type";
    public static final String OAUTH2_PARAMETER_REDIRECT_URI = "redirect_uri";
    public static final String OAUTH2_PARAMETER_CODE = "code";
    public static final String OAUTH2_PARAMETER_CLIENT_ID = "client_id";
    public static final String OAUTH2_PARAMETER_CLIENT_SECRET = "client_secret";
    public static final String OAUTH2_PARAMETER_GRANT_TYPE = "grant_type";


    public AbstractOAuth2IdentityProvider(C config) {
        super(config);

        if (config.getDefaultScope() == null || config.getDefaultScope().isEmpty()) {
            config.setDefaultScope(getDefaultScopes());
        }
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event);
    }

    @Override
    public Response handleRequest(AuthenticationRequest request) {
        try {
            URI authorizationUrl = createAuthorizationUrl(request).build();

            return Response.temporaryRedirect(authorizationUrl).build();
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not create authentication request.", e);
        }
    }

    @Override
    public Response retrieveToken(FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).build();
    }

    @Override
    public C getConfig() {
        return super.getConfig();
    }

    protected String extractTokenFromResponse(String response, String tokenName) {
    	  if(response == null)
    	  	return null;
    	  
        if (response.startsWith("{")) {
            try {
            		JsonNode node = mapper.readTree(response);
            		if(node.has(tokenName)){
            			String s = node.get(tokenName).getTextValue();
            			if(s == null || s.trim().isEmpty())
            				return null;
                  return s;
            		} else {
            			return null;
            		}
            } catch (IOException e) {
                throw new IdentityBrokerException("Could not extract token [" + tokenName + "] from response [" + response + "] due: " + e.getMessage(), e);
            }
        } else {
            Matcher matcher = Pattern.compile(tokenName + "=([^&]+)").matcher(response);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    protected FederatedIdentity getFederatedIdentity(Map<String, String> notes, String response) {
        String accessToken = extractTokenFromResponse(response, OAUTH2_PARAMETER_ACCESS_TOKEN);

        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }

        return doGetFederatedIdentity(accessToken);
    }


    protected FederatedIdentity doGetFederatedIdentity(String accessToken) {
        return null;
    }


    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        return UriBuilder.fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam(OAUTH2_PARAMETER_STATE, request.getState())
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());
    }

    /**
     * Get JSON property as text. JSON numbers and booleans are converted to text. Empty string is converted to null. 
     * 
     * @param jsonNode to get property from
     * @param name of property to get
     * @return string value of the property or null.
     */
    protected String getJsonProperty(JsonNode jsonNode, String name) {
        if (jsonNode.has(name) && !jsonNode.get(name).isNull()) {
        	  String s = jsonNode.get(name).asText();
        	  if(s != null && !s.isEmpty())
        	  		return s;
        	  else
      	  			return null;
        }

        return null;
    }

    protected JsonNode asJsonNode(String json) throws IOException {
        return mapper.readTree(json);
    }

    protected abstract String getDefaultScopes();

    protected class Endpoint {
        protected AuthenticationCallback callback;
        protected RealmModel realm;
        protected EventBuilder event;

        @Context
        protected KeycloakSession session;

        @Context
        protected ClientConnection clientConnection;

        @Context
        protected HttpHeaders headers;

        @Context
        protected UriInfo uriInfo;

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
        }

        @GET
        public Response authResponse(@QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                                     @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                                     @QueryParam(OAuth2Constants.ERROR) String error) {
            if (error != null) {
                //logger.error("Failed " + getConfig().getAlias() + " broker login: " + error);
                event.event(EventType.LOGIN);
                event.error(error);
                return Flows.forwardToSecurityFailurePage(session, realm, uriInfo, headers, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
            }

            try {

                if (authorizationCode != null) {
                    String response = generateTokenRequest(authorizationCode).asString();

                    HashMap<String, String> userNotes = new HashMap<String, String>();
                    FederatedIdentity federatedIdentity = getFederatedIdentity(userNotes, response);

                    if (getConfig().isStoreToken()) {
                        federatedIdentity.setToken(response);
                    }

                    return callback.authenticated(userNotes, getConfig(), federatedIdentity, state);
                }
            } catch (Exception e) {
                logger.error("Failed to make identity provider oauth callback", e);
            }
            event.event(EventType.LOGIN);
            event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return Flows.forwardToSecurityFailurePage(session, realm, uriInfo, headers, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        }

        public SimpleHttp generateTokenRequest(String authorizationCode) {
            return SimpleHttp.doPost(getConfig().getTokenUrl())
                    .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                    .param(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                    .param(OAUTH2_PARAMETER_CLIENT_SECRET, getConfig().getClientSecret())
                    .param(OAUTH2_PARAMETER_REDIRECT_URI, uriInfo.getAbsolutePath().toString())
                    .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        }
    }
}

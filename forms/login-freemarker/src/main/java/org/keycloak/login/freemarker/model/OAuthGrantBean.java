/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.keycloak.login.freemarker.model;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RoleModel;

import javax.ws.rs.core.MultivaluedMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 */
public class OAuthGrantBean {

    private final String accessRequestMessage;
    private List<RoleModel> realmRolesRequested;
    private MultivaluedMap<String, RoleModel> resourceRolesRequested;
    private String code;
    private ClientModel client;
    private List<String> claimsRequested;

    public OAuthGrantBean(String code, ClientSessionModel clientSession, ClientModel client, List<RoleModel> realmRolesRequested, MultivaluedMap<String, RoleModel> resourceRolesRequested, String accessRequestMessage) {
        this.code = code;
        this.client = client;
        this.realmRolesRequested = realmRolesRequested;
        this.resourceRolesRequested = resourceRolesRequested;
        this.accessRequestMessage = accessRequestMessage;

        // todo support locale
        List<String> claims = new LinkedList<String>();
        if (clientSession != null) {
            for (ProtocolMapperModel model : client.getProtocolMappers()) {
                if (model.isConsentRequired() && model.getProtocol().equals(clientSession.getAuthMethod()) && model.getConsentText() != null) {
                    claims.add(model.getConsentText());
                }
            }
        }
        if (claims.size() > 0) this.claimsRequested = claims;
    }

    public String getCode() {
        return code;
    }

    public MultivaluedMap<String, RoleModel> getResourceRolesRequested() {
        return resourceRolesRequested;
    }

    public List<RoleModel> getRealmRolesRequested() {
        return realmRolesRequested;
    }

    public String getClient() {
        return client.getClientId();
    }

    public List<String> getClaimsRequested() {
        return claimsRequested;
    }

    public String getAccessRequestMessage() {
        return this.accessRequestMessage;
    }
}

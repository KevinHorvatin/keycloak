package org.keycloak.representations.idm;

import java.util.List;
import java.util.Map;

public class OrganizationRepresentation {

    protected String id;
    protected String name;
    protected String companyName;
    protected String description;
    protected List<String> realmRoles;
    protected Map<String, List<String>> applicationRoles;
    //TODO: This will need to change to Map<AttributeType, String>
    protected Map<String, String> attributes;
    protected boolean enabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRealmRoles() {
        return realmRoles;
    }

    public void setRealmRoles(List<String> realmRoles) {
        this.realmRoles = realmRoles;
    }

    public Map<String, List<String>> getApplicationRoles() {
        return applicationRoles;
    }

    public void setApplicationRoles(Map<String, List<String>> applicationRoles) {
        this.applicationRoles = applicationRoles;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

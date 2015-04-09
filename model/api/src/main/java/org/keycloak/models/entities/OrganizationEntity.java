package org.keycloak.models.entities;

import java.util.List;

/**
 * @author <a href="mailto:dane.barentine@software.dell.com">Dane Barentine</a>
 */
public class OrganizationEntity {
    private String id;
    private String name;
    private String description;
    private List<String> roleIds;
    //TODO: This will need to change to Map<AttributeType, String>
    //private Map<String, String> attributes;
    private boolean enabled;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    /*public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }*/

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

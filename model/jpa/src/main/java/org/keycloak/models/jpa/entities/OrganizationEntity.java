package org.keycloak.models.jpa.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="ORGANIZATION")
@NamedQueries({
        @NamedQuery(name="findOrganizationByName", query="select organization from OrganizationEntity organization where organization.name = :name")
})
public class OrganizationEntity {
    @Id
    @Column(name="ID", length = 36)
    protected String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    @Column(name="NAME")
    protected String name;

    @Column(name="DESCRIPTION")
    protected String description;

    //private List<String> roleIds;
    //TODO: This will need to change to Map<AttributeType, String>
    //private Map<String, String> attributes;

    @Column(name="ENABLED")
    private boolean enabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RealmEntity getRealm() {
        return this.realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
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

    /*public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public Map<String, String> getAttributes() {
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
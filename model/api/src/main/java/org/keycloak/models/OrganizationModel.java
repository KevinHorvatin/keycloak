package org.keycloak.models;

/**
 * @author <a href="mailto:dane.barentine@software.dell.com">Dane Barentine</a>
 */

public class OrganizationModel {
    private String id;
    private String name;
    private String description;
    private boolean enabled;

    public OrganizationModel() {

    }

    public OrganizationModel(OrganizationModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.enabled = model.isEnabled();
    }

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /*void setAttribute(String name, String value);

    void removeAttribute(String name);

    String getAttribute(String name);

    Map<String, String> getAttributes();

    Set<RoleModel> getRealmRoleMappings();
    Set<RoleModel> getApplicationRoleMappings(ApplicationModel app);
    boolean hasRole(RoleModel role);
    void grantRole(RoleModel role);
    Set<RoleModel> getRoleMappings();
    void deleteRoleMapping(RoleModel role);*/
}

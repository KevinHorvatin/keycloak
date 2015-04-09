package org.keycloak.connections.mongo.updater.impl.updates;

import com.mongodb.*;
import org.keycloak.Config;
import org.keycloak.connections.mongo.impl.types.MapMapper;
import org.keycloak.migration.MigrationProvider;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class Update1_3_0_Beta1 extends Update {

    @Override
    public String getId() {
        return "1.3.0.Beta1";
    }

    @Override
    public void update(KeycloakSession session) {
        addNewAdminRoles();
    }


    private void addNewAdminRoles() {
        DBCollection realms = db.getCollection("realms");
        String adminRealmName = Config.getAdminRealm();

        DBCursor realmsCursor = realms.find();
        try {
            while (realmsCursor.hasNext()) {
                BasicDBObject realm = (BasicDBObject) realmsCursor.next();
                if (adminRealmName.equals(realm.get("name"))) {
                    addNewAdminRolesToMasterRealm(realm);
                } else {
                    addNewAdminRolesToRealm(realm);
                }
            }
        } finally {
            realmsCursor.close();
        }
    }

    private void addNewAdminRolesToMasterRealm(BasicDBObject adminRealm) {
        DBCollection realms = db.getCollection("realms");
        DBCollection applications = db.getCollection("applications");
        DBCollection roles = db.getCollection("roles");

        DBCursor realmsCursor = realms.find();
        try {
            while (realmsCursor.hasNext()) {
                BasicDBObject currentRealm = (BasicDBObject) realmsCursor.next();
                String masterAdminAppName = currentRealm.getString("name") + "-realm";

                BasicDBObject masterAdminApp = (BasicDBObject) applications.findOne(new BasicDBObject().append("realmId", adminRealm.get("_id")).append("name", masterAdminAppName));

                String viewIdProvidersRoleId = insertApplicationRole(roles, AdminRoles.VIEW_ORGANIZATIONS, masterAdminApp.getString("_id"));
                String manageIdProvidersRoleId = insertApplicationRole(roles, AdminRoles.MANAGE_ORGANIZATIONS, masterAdminApp.getString("_id"));

                BasicDBObject adminRole = (BasicDBObject) roles.findOne(new BasicDBObject().append("realmId", adminRealm.get("_id")).append("name", AdminRoles.ADMIN));
                BasicDBList adminCompositeRoles = (BasicDBList) adminRole.get("compositeRoleIds");
                adminCompositeRoles.add(viewIdProvidersRoleId);
                adminCompositeRoles.add(manageIdProvidersRoleId);
                roles.save(adminRole);

                log.debugv("Added roles {0} and {1} to application {2}", AdminRoles.VIEW_ORGANIZATIONS, AdminRoles.MANAGE_ORGANIZATIONS, masterAdminAppName);
            }
        } finally {
            realmsCursor.close();
        }
    }

    private void addNewAdminRolesToRealm(BasicDBObject currentRealm) {
        DBCollection applications = db.getCollection("applications");
        DBCollection roles = db.getCollection("roles");

        BasicDBObject adminApp = (BasicDBObject) applications.findOne(new BasicDBObject().append("realmId", currentRealm.get("_id")).append("name", "realm-management"));

        String viewIdProvidersRoleId = insertApplicationRole(roles, AdminRoles.VIEW_ORGANIZATIONS, adminApp.getString("_id"));
        String manageIdProvidersRoleId = insertApplicationRole(roles, AdminRoles.MANAGE_ORGANIZATIONS, adminApp.getString("_id"));

        BasicDBObject adminRole = (BasicDBObject) roles.findOne(new BasicDBObject().append("applicationId", adminApp.get("_id")).append("name", AdminRoles.REALM_ADMIN));
        BasicDBList adminCompositeRoles = (BasicDBList) adminRole.get("compositeRoleIds");
        adminCompositeRoles.add(viewIdProvidersRoleId);
        adminCompositeRoles.add(manageIdProvidersRoleId);

        roles.save(adminRole);
        log.debugv("Added roles {0} and {1} to application realm-management of realm {2}", AdminRoles.VIEW_ORGANIZATIONS, AdminRoles.MANAGE_ORGANIZATIONS, currentRealm.get("name"));
    }

    private String insertApplicationRole(DBCollection roles, String roleName, String applicationId) {
        BasicDBObject role = new BasicDBObject();
        String roleId = KeycloakModelUtils.generateId();
        role.append("_id", roleId);
        role.append("name", roleName);
        role.append("applicationId", applicationId);
        role.append("nameIndex", applicationId + "//" + roleName);
        roles.insert(role);
        return roleId;
    }
}

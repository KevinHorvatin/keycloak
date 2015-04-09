package org.keycloak.services.resources.admin;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.services.resources.KeycloakApplication;
import org.keycloak.services.resources.flows.Flows;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class OrganizationResource {
    private static Logger logger = Logger.getLogger(OrganizationResource.class);

    protected RealmModel realm;
    private RealmAuth auth;
    protected OrganizationModel organizationModel;
    protected KeycloakSession session;

    @Context
    protected UriInfo uriInfo;

    @Context
    protected KeycloakApplication keycloak;

    protected KeycloakApplication getKeycloakApplication() {
        return keycloak;
    }

    public OrganizationResource(RealmAuth auth, RealmModel realm, KeycloakSession session, OrganizationModel organizationModel) {
        this.realm = realm;
        this.session = session;
        this.organizationModel = organizationModel;
        this.auth = auth;
    }

    @GET
    @NoCache
    @Produces("application/json")
    public OrganizationRepresentation getOrganization() {
        OrganizationRepresentation rep = ModelToRepresentation.toRepresentation(this.organizationModel);
        return rep;
    }

    @DELETE
    @NoCache
    public Response delete() {
        this.auth.requireManage();

        realm.removeOrganizationByName(this.organizationModel.getName());

        return Response.noContent().build();
    }

    @PUT
    @Consumes("application/json")
    public Response update(OrganizationRepresentation rep) {
        this.auth.requireManage();

        try {
            this.realm.updateOrganization(RepresentationToModel.toModel(rep));

            return Response.noContent().build();
        } catch (ModelDuplicateException e) {
            return Flows.errors().exists("Organization " + rep.getName() + " already exists");
        }
    }
}


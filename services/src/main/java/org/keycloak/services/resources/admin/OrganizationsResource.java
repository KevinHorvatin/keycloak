package org.keycloak.services.resources.admin;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.services.managers.ResourceAdminManager;
import org.keycloak.services.resources.flows.Flows;
import org.keycloak.social.SocialIdentityProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class OrganizationsResource {
    private final RealmModel realm;
    private final KeycloakSession session;
    private RealmAuth auth;

    public OrganizationsResource(RealmModel realm, KeycloakSession session, RealmAuth auth) {
        this.realm = realm;
        this.session = session;
        this.auth = auth;
        this.auth.init(RealmAuth.Resource.ORGANIZATION);
    }

    @GET
    @NoCache
    @Produces("application/json")
    public List<OrganizationRepresentation> getOrganizations() {
        this.auth.requireView();

        List<OrganizationRepresentation> representations = new ArrayList<>();

        for (OrganizationModel organizationModel : realm.getOrganizations()) {
            representations.add(ModelToRepresentation.toRepresentation(organizationModel));
        }

        return representations;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Context UriInfo uriInfo, OrganizationRepresentation representation) {
        this.auth.requireManage();

        try {
            this.realm.addOrganization(RepresentationToModel.toModel(representation));

            return Response.created(uriInfo.getAbsolutePathBuilder().path(representation.getName()).build()).build();
        } catch (ModelDuplicateException e) {
            return Flows.errors().exists("Organization " + representation.getName() + " already exists");
        }
    }

    @Path("{name}")
    public OrganizationResource getIdentityProvider(@PathParam("name") String name) {
        OrganizationModel organizationModel = getOrganizationByPathParam(name);

        if(organizationModel == null) {
            throw new NotFoundException("Could not find organization: " + name);
        }

        OrganizationResource organizationResource = new OrganizationResource(auth, realm, session, organizationModel);

        return organizationResource;
    }

    protected OrganizationModel getOrganizationByPathParam(String name) {
        return realm.getOrganizationByName(name);
    }
}

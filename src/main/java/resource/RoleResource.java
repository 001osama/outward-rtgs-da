package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.Role;
import repository.RoleRepository;

import java.util.List;
import org.jboss.logging.Logger;


@Path("/role")
@Produces(MediaType.APPLICATION_JSON)
public class RoleResource {

    @Inject
    Logger logger;

    @Inject
    RoleRepository roleRepo;

    @GET
    @Path("/getRoles")
    public List<Role> getRoles() 
    {
        logger.info("message : =====> getRoles method started");
        return roleRepo.getAll();
    }
}

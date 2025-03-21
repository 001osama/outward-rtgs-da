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
public class MainResource {

    @Inject
    Logger logger;

    @Inject
    RoleRepository roleRepo;

    @GET
    @Path("/getdetail")
    public List<Role> getDetail() 
    {
        logger.info("message : =====> getDetail method started");
        return roleRepo.getAll();
    }
}

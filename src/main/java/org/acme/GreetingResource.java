package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import service.ItoBatchService;

@Path("/hello")
public class GreetingResource {

    @Inject
    ItoBatchService itoBatchService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        String result = itoBatchService.getDetail();
        return "Hello RESTEasy";
    }
}

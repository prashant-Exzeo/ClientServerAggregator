package com.knightRider.config.rs;

import com.knightRider.config.AppConfig;
import com.knightRider.config.RestServiceDetails;
import com.knightRider.config.model.Person;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceInstance;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;

@Path(PeopleRestService.PEOPLE_PATH)
public class PeopleRestService {
    public static final String PEOPLE_PATH = "/people";

    @Inject
    private JaxRsApiApplication application;
    @Inject
    private ServiceDiscovery<RestServiceDetails> discovery;
    @Inject
    private Environment environment;

    @PostConstruct
    public void init() throws Exception {
        final ServiceInstance<RestServiceDetails> instance =
                ServiceInstance.<RestServiceDetails>builder()
                        .name("people")
                        .payload(new RestServiceDetails("1.0"))
                        .port(environment.getProperty(AppConfig.SERVER_PORT, Integer.class))
                        .uriSpec(application.getUriSpec(PEOPLE_PATH))
                        .build();

        discovery.registerService(instance);
    }

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Collection<Person> getPeople(@QueryParam("page") @DefaultValue("1") final int page) {
        return Arrays.asList(
                new Person("Tom", "Bombadil"),
                new Person("Jim", "Tommyknockers")
        );
    }
}

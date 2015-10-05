package com.knightRider.client;

import com.knightRider.config.RestServiceDetails;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceInstance;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

public class ClientStarter {
    public static void main(final String[] args) throws Exception {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ClientConfig.class)) {
            @SuppressWarnings("unchecked")
            final ServiceDiscovery<RestServiceDetails> discovery = context.getBean(ServiceDiscovery.class);
            final Client client = ClientBuilder.newClient();

            final Collection<ServiceInstance<RestServiceDetails>> services = discovery.queryForInstances("people");
            for (final ServiceInstance<RestServiceDetails> service : services) {
                final String uri = service.buildUriSpec();

                final Response response = client
                        .target(uri)
                        .request(MediaType.APPLICATION_JSON)
                        .get();

                System.out.println(uri + ": " + response.readEntity(String.class));
                System.out.println("API version: " + service.getPayload().getVersion());

                response.close();
            }
        }
    }
}

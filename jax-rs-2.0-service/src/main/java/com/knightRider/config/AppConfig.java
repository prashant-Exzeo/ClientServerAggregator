package com.knightRider.config;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.knightRider.config.rs.JaxRsApiApplication;
import com.knightRider.config.rs.PeopleRestService;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceDiscoveryBuilder;
import com.netflix.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Arrays;

@Configuration
public class AppConfig {
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_HOST = "server.host";
    public static final String CONTEXT_PATH = "rest";
    private static final String ZK_HOST = "localhost";

    @Bean(destroyMethod = "shutdown")
    public SpringBus cxf() {
        return new SpringBus();
    }

    @Bean
    @DependsOn("cxf")
    public Server jaxRsServer() {
        JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance().createEndpoint(jaxRsApiApplication(), JAXRSServerFactoryBean.class);
        factory.setServiceBeans(Arrays.<Object>asList(peopleRestService()));
        factory.setAddress(factory.getAddress());
        factory.setProviders(Arrays.<Object>asList(jsonProvider()));
        return factory.create();
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curator() {
        return CuratorFrameworkFactory.newClient(ZK_HOST, new ExponentialBackoffRetry(1000, 3));
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public ServiceDiscovery<RestServiceDetails> discovery() {
        JsonInstanceSerializer<RestServiceDetails> serializer =
                new JsonInstanceSerializer<RestServiceDetails>(RestServiceDetails.class);

        return ServiceDiscoveryBuilder.builder(RestServiceDetails.class)
                .client(curator())
                .basePath("services")
                .serializer(serializer)
                .build();
    }

    @Bean
    public JaxRsApiApplication jaxRsApiApplication() {
        return new JaxRsApiApplication();
    }

    @Bean
    public PeopleRestService peopleRestService() {
        return new PeopleRestService();
    }

    @Bean
    public JacksonJsonProvider jsonProvider() {
        return new JacksonJsonProvider();
    }
}

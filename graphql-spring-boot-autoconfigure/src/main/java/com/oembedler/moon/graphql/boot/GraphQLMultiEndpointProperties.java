package com.oembedler.moon.graphql.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "graphql")
public class GraphQLMultiEndpointProperties {
    private Map<String, GraphQLEndpointProperties> multiEndpoints;

    public Map<String, GraphQLEndpointProperties> getMultiEndpoints() {
        return multiEndpoints;
    }

    public void setMultiEndpoints(Map<String, GraphQLEndpointProperties> multiEndpoints) {
        this.multiEndpoints = multiEndpoints;
    }
}

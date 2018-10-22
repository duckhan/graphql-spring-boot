package com.oembedler.moon.graphql.boot;

public class GraphQLEndpointProperties {
    private String schemaLocationPattern;
    private String endpoint;

    public String getSchemaLocationPattern() {
        return schemaLocationPattern;
    }

    public void setSchemaLocationPattern(String schemaLocationPattern) {
        this.schemaLocationPattern = schemaLocationPattern;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}

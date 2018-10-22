package com.oembedler.moon.graphql.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"graphql.servlet.mapping=/test", "graphql.multiEndpoints.client.schemaLocationPattern=*.graphqls", "graphql.multiEndpoints.client.endpoint=/client"})
public class GraphQLMultiEndpointPropertiesTest {
    @Autowired(required = false)
    GraphQLMultiEndpointProperties properties;

    @Test
    public void contain_multi_properties() {
        Assert.notNull(properties);
        Assert.notNull(properties.getMultiEndpoints());
        Assert.notNull(properties.getMultiEndpoints().get("client"));
    }
}

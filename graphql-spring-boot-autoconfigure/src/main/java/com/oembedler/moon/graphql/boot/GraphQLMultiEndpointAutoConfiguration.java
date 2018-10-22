package com.oembedler.moon.graphql.boot;

import com.coxautodev.graphql.tools.*;
import graphql.schema.GraphQLScalarType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

import static com.coxautodev.graphql.tools.SchemaParserOptions.newOptions;

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(GraphQLMultiEndpointProperties.class)
@AutoConfigureAfter(GraphQLWebAutoConfiguration.class)
public class GraphQLMultiEndpointAutoConfiguration implements InitializingBean {
    @Autowired
    GraphQLMultiEndpointProperties multiEndpointProperties;
    @Autowired(required = false)
    SchemaParserDictionary dictionary;
    @Autowired
    DefaultListableBeanFactory beanFactory;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired(required = false)
    private GraphQLScalarType[] scalars;

    @Autowired(required = false)
    private SchemaParserOptions options;
    @Override
    public void afterPropertiesSet() throws Exception {

    }

    private SchemaParser schemaParser(
            List<GraphQLResolver<?>> resolvers,
            SchemaStringProvider schemaStringProvider,
            PerFieldObjectMapperProvider perFieldObjectMapperProvider
    ) throws IOException {
        SchemaParserBuilder builder = dictionary != null ? new SchemaParserBuilder(dictionary) : new SchemaParserBuilder();

        List<String> schemaStrings = schemaStringProvider.schemaStrings();
        schemaStrings.forEach(builder::schemaString);

        if (scalars != null) {
            builder.scalars(scalars);
        }

        if (options != null) {
            builder.options(options);
        } else if (perFieldObjectMapperProvider != null) {
            final SchemaParserOptions.Builder optionsBuilder =
                    newOptions().objectMapperProvider(perFieldObjectMapperProvider);
            optionsBuilder.introspectionEnabled(introspectionEnabled);
            builder.options(optionsBuilder.build());
        }

        return builder
                .resolvers(resolvers)
                .build();
    }

}

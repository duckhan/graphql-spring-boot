package khan.example.examplegraphqlmultiendpoint;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

@Component
public class GraphQLQuery implements GraphQLQueryResolver {
    public String version() {
        return "1.0";
    }

    public boolean isClient() {
        return true;
    }

    public boolean isDefault() {
        return true;
    }
}

package swd392.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server httpsServer = new Server();
        httpsServer.setUrl("https://swd392-api.taiduc1001.net");
        httpsServer.setDescription("Production");

        Server httpServer = new Server();
        httpServer.setUrl("http://localhost:8080");
        httpServer.setDescription("Local");

        return new OpenAPI().servers(List.of(httpsServer, httpServer));
    }
}

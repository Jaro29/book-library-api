package pl.jaro.restapiworkshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:4200", "https://afterword.coffe.ink")
//                .allowedMethods("GET", "POST", "PATCH", "DELETE")
//                .allowedHeaders("*");
    }
}
package pl.jaro.restapiworkshop.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final DataSource dataSource;

    public DevDataSeeder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        new ResourceDatabasePopulator(new ClassPathResource("data-dev.sql"))
                .execute(dataSource);
    }
}
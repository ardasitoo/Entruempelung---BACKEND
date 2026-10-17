package de.ardasitoo.entruempelung.config;

import de.ardasitoo.entruempelung.customerrequest.CustomerRequestDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostgresConfigurationTests {
    private ApplicationContextRunner postgres() {
        return new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.profiles.active=postgres")
                .withUserConfiguration(CustomerRequestDataSeeder.class);
    }

    @Test
    void postgresUsesExternalDatabaseAndDisablesDemoData() {
        postgres().withPropertyValues("DB_HOST=database.internal", "DB_NAME=nexrum",
                "DB_USERNAME=test-user", "DB_PASSWORD=test-password").run(context -> {
            var environment = context.getEnvironment();
            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://database.internal:5432/nexrum?sslmode=require");
            assertThat(environment.getProperty("spring.datasource.driver-class-name"))
                    .isEqualTo("org.postgresql.Driver");
            assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
            assertThat(environment.getProperty("spring.h2.console.enabled")).isEqualTo("false");
            assertThat(context).doesNotHaveBean(CustomerRequestDataSeeder.class);
            assertThat(Class.forName("org.postgresql.Driver")).isNotNull();
        });
    }

    @Test
    void missingDatabaseDetailsCannotResolveToH2() {
        postgres().run(context -> {
            assertThatThrownBy(() -> context.getEnvironment().getRequiredProperty("spring.datasource.url"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(context).doesNotHaveBean(CustomerRequestDataSeeder.class);
        });
    }
}

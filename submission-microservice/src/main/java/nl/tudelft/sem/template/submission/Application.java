package nl.tudelft.sem.template.submission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Example microservice application.
 */
@EntityScan("nl.tudelft.sem.template.model")
@ComponentScan(basePackages = {"nl.tudelft.sem.template.submission.components",
        "nl.tudelft.sem.template.submission.controllers",
        "nl.tudelft.sem.template.submission.services"
})
@EnableJpaRepositories("nl.tudelft.sem.template.submission.repositories")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

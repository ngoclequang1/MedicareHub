package fit.se2.medicarehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedicareHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicareHubApplication.class, args);
    }

}

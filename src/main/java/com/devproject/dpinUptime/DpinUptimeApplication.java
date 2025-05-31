package com.devproject.dpinUptime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DpinUptimeApplication {

public static void main(String[] args) {
    String port = "8080";

    System.setProperty("server.port", port);
    System.out.println("⚙️ Using port: " + port);
    SpringApplication.run(DpinUptimeApplication.class, args);
}

@Bean
CommandLineRunner testDB(javax.sql.DataSource dataSource) {
    return args -> {
        try (java.sql.Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Database connected to: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
            System.exit(1);
        }
    };
}

}

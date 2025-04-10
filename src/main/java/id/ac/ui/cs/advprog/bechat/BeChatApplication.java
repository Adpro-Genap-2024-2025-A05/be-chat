package id.ac.ui.cs.advprog.bechat;

import id.ac.ui.cs.advprog.bechat.repository.ChatMessageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BeChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeChatApplication.class, args);
    }

    @Bean
    public CommandLineRunner testRepo(ChatMessageRepository repo) {
        return args -> System.out.println(" ChatMessageRepository injected successfully: " + repo);
    }
}

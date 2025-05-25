package id.ac.ui.cs.advprog.bechat.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMetricsConfig {
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Counter sendMessageCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.message.send.success")
                .description("Number of successfully sent messages")
                .register(meterRegistry);
    }

    @Bean
    public Counter sendMessageFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.message.send.failure")
                .description("Number of failed sent messages")
                .register(meterRegistry);
    }

    @Bean
    public Counter editMessageCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.message.edit.success")
                .description("Number of successfully edited messages")
                .register(meterRegistry);
    }

    @Bean
    public Counter deleteMessageCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.message.delete.success")
                .description("Number of successfully deleted messages")
                .register(meterRegistry);
    }

    @Bean
    public Timer getMessagesTimer(MeterRegistry meterRegistry) {
        return Timer.builder("chat.message.fetch.timer")
                .description("Time taken to fetch messages")
                .publishPercentileHistogram() 
                .publishPercentiles(0.95)     
                .register(meterRegistry);
    }

    @Bean
    public Counter chatSessionCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.session.create.success")
                .description("Number of chat sessions created")
                .register(meterRegistry);
    }

    @Bean
    public Counter chatSessionCreateFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.session.create.failure")
                .description("Number of failed chat session creations")
                .register(meterRegistry);
    }
}

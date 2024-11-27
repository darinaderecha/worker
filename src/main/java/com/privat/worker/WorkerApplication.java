package com.privat.worker;

import com.privat.worker.service.RegularPaymentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication implements CommandLineRunner {

    private final RegularPaymentService paymentService;

    public WorkerApplication(RegularPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting regular payment processing...");
        paymentService.processPayments();
        System.out.println("Processing completed.");
    }
}


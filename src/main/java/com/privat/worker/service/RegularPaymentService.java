package com.privat.worker.service;

import com.privat.worker.dto.ChargeDto;
import com.privat.worker.dto.ChargeValidation;
import com.privat.worker.dto.PaymentDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class RegularPaymentService {

    private final RestTemplate restTemplate;
    private String url = "http://localhost:8088";

    public RegularPaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void processPayments() {
        List<PaymentDto> payments = fetchAllPayments();
        List<UUID> paymentIds = payments.stream()
                .map(PaymentDto::id)
                .toList();
        for (UUID paymentId : paymentIds) {
            try {
                checkCharges(paymentId);
            } catch (Exception e) {
                System.err.println("Failed to process paymentId: " + paymentId + " \n" +  e.getMessage());
            }
        }
    }

    public void checkCharges(UUID paymentId) {
        String endpoint = url + "/v1/charges-rest/check/" + paymentId;

        try {
            ResponseEntity<ChargeValidation> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ChargeValidation>() {}
            );
             if(response.getBody() != null &&  response.getStatusCode().is2xxSuccessful()){
                 ChargeValidation validation = response.getBody();
                 if(validation.isNeedToCharge()) {
                     makeCharges(paymentId);
                 }
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to create charge for paymentId: " + paymentId, e);
        }catch (Exception e) {
            throw new RuntimeException("Unexpected error : " + e.getMessage(), e);
        }
    }

    private void makeCharges(UUID paymentId) {
        String endpoint = url + "/v1/charges-rest/" + paymentId;
        try {
           restTemplate.postForEntity(endpoint, null, ChargeDto.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to create charge for paymentId: " + paymentId, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error : " + e.getMessage(), e);
        }
    }

    public List<PaymentDto> fetchAllPayments() {
        String endpoint = url + "/v1/payments-rest/all";
        try {
            ResponseEntity<List<PaymentDto>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PaymentDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch payments. HTTP Status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Client error while fetching payments: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new RuntimeException("Server error while fetching payments: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Failed to access payment API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while fetching payments: " + e.getMessage(), e);
        }
    }
}

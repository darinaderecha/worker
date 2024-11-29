package com.privat.worker.service;

import com.privat.worker.dto.ChargeDto;
import com.privat.worker.dto.ChargeValidation;
import com.privat.worker.dto.PaymentDto;
import com.privat.worker.exception.ChargeCheckException;
import com.privat.worker.exception.ChargeCreationException;
import com.privat.worker.exception.PaymentFetchException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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
        int page = 0;
        int size = 4;
        boolean hasMoreData = true;

        while (hasMoreData) {
            List<PaymentDto> payments = fetchAllPayments(page, size);
            if (payments == null || payments.isEmpty()) {
                hasMoreData = false;
            } else {
                List<UUID> paymentIds = payments.stream()
                        .map(PaymentDto::id)
                        .toList();

                for (UUID paymentId : paymentIds) {
                    try {
                        checkCharges(paymentId);
                    } catch (Exception e) {
                        System.err.println("Failed to process paymentId: " + paymentId + " \n" + e.getMessage());
                    }
                }
                page++;
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
                    new ParameterizedTypeReference<ChargeValidation>() {
                    }
            );
            if (response.getBody() != null && response.getStatusCode().is2xxSuccessful()) {
                ChargeValidation validation = response.getBody();
                if (validation.isNeedToCharge()) {
                    makeCharges(paymentId);
                }
            }
        } catch (HttpClientErrorException e) {
            throw new ChargeCheckException("Failed to create charge for paymentId: " + paymentId, e);
        } catch (Exception e) {
            throw new ChargeCheckException("Unexpected error : " + e.getMessage(), e);
        }
    }

    public void makeCharges(UUID paymentId) {
        String endpoint = url + "/v1/charges-rest/" + paymentId;
        try {
            restTemplate.postForEntity(endpoint, null, ChargeDto.class);
        } catch (HttpClientErrorException e) {
            throw new ChargeCreationException("Failed to create charge for paymentId: " + paymentId, e);
        } catch (Exception e) {
            throw new ChargeCreationException("Unexpected error : " + e.getMessage(), e);
        }
    }

    public List<PaymentDto> fetchAllPayments(int page, int size) {
        String endpoint = url + "/v1/payments-rest/all?page=" + page + "&size=" + size;
        try {
            ResponseEntity<List<PaymentDto>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PaymentDto>>() {
                    }
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                System.err.println("Failed to fetch payments. HTTP Status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            throw new PaymentFetchException("Client error while fetching payments: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new PaymentFetchException("Server error while fetching payments: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new PaymentFetchException("Failed to access payment API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new PaymentFetchException("Unexpected error while fetching payments: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}

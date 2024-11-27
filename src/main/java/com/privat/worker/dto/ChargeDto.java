package com.privat.worker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeDto(UUID chargeId,
                        @JsonProperty("payment_id")
                        UUID payment,
                        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                        LocalDateTime chargeTime,
                        BigDecimal amount,
                        Status status) {
}

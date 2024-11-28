package com.privat.worker.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentDto(UUID id,
                         UUID card,
                         String iban,
                         String mfo,
                         String zkpo,
                         String receiverName,
                         BigDecimal amount,
                         Long withdrawalPeriod)  {
}

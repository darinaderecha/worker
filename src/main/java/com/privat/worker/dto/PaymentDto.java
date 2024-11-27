package com.privat.worker.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentDto(UUID id,
                         UUID card,
                         String IBAN,
                         String MFO,
                         String ZKPO,
                         String receiverName,
                         BigDecimal amount,
                         Long withdrawalPeriod)  {
}

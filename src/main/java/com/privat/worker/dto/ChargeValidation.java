package com.privat.worker.dto;

public class ChargeValidation {
    private boolean needToCharge;

    public ChargeValidation(boolean needToCharge) {
        this.needToCharge = needToCharge;
    }

    public boolean isNeedToCharge() {
        return needToCharge;
    }

    public void setNeedToCharge(boolean needToCharge) {
        this.needToCharge = needToCharge;
    }
}


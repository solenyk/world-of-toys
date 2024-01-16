package com.kopchak.worldoftoys.domain.order.recipient.number;

public enum CountryCode {
    UA("+380");
    public final String countryCode;

    CountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}

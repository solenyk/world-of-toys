package com.kopchak.worldoftoys.model.order;

public enum CountryCode {
    UA("+380");
    public final String countryCode;

    CountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}

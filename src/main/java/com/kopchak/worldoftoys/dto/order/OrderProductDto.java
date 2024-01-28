package com.kopchak.worldoftoys.dto.order;

import java.math.BigInteger;

public record OrderProductDto(String name, String slug, BigInteger quantity) {
}

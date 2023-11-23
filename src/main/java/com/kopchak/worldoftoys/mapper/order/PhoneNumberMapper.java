package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.dto.order.PhoneNumberDto;
import com.kopchak.worldoftoys.model.order.recipient.number.PhoneNumber;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PhoneNumberMapper {
    @Mapping(target = "countryCode",
            expression = "java(com.kopchak.worldoftoys.model.order.recipient.number.CountryCode.UA)")
    PhoneNumber toPhoneNumber(PhoneNumberDto phoneNumberDto);
}

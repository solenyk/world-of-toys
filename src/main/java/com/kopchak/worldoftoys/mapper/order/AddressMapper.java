package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.dto.order.AddressDto;
import com.kopchak.worldoftoys.model.order.recipient.address.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(AddressDto addressDto);
}

package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PhoneNumberMapper.class)
public interface OrderRecipientMapper {
    OrderRecipient toOrderRecipient(OrderRecipientDto orderRecipientDto);
}

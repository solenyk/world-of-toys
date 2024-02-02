package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient.OrderRecipientBuilder;
import com.kopchak.worldoftoys.domain.order.recipient.address.Address;
import com.kopchak.worldoftoys.domain.order.recipient.address.Address.AddressBuilder;
import com.kopchak.worldoftoys.dto.order.AddressDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-31T23:11:17+0200",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class OrderRecipientMapperImpl implements OrderRecipientMapper {

    @Autowired
    private PhoneNumberMapper phoneNumberMapper;

    @Override
    public OrderRecipient toOrderRecipient(OrderRecipientDto orderRecipientDto) {
        if ( orderRecipientDto == null ) {
            return null;
        }

        OrderRecipientBuilder orderRecipient = OrderRecipient.builder();

        orderRecipient.lastname( orderRecipientDto.lastname() );
        orderRecipient.firstname( orderRecipientDto.firstname() );
        orderRecipient.patronymic( orderRecipientDto.patronymic() );
        orderRecipient.phoneNumber( phoneNumberMapper.toPhoneNumber( orderRecipientDto.phoneNumber() ) );
        orderRecipient.address( addressDtoToAddress( orderRecipientDto.address() ) );

        return orderRecipient.build();
    }

    protected Address addressDtoToAddress(AddressDto addressDto) {
        if ( addressDto == null ) {
            return null;
        }

        AddressBuilder address = Address.builder();

        address.region( addressDto.region() );
        address.settlement( addressDto.settlement() );
        address.street( addressDto.street() );
        address.house( addressDto.house() );
        address.apartment( addressDto.apartment() );

        return address.build();
    }
}

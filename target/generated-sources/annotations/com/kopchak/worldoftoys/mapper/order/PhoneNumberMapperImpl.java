package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.domain.order.recipient.number.PhoneNumber;
import com.kopchak.worldoftoys.domain.order.recipient.number.PhoneNumber.PhoneNumberBuilder;
import com.kopchak.worldoftoys.dto.order.PhoneNumberDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-31T22:53:11+0200",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class PhoneNumberMapperImpl implements PhoneNumberMapper {

    @Override
    public PhoneNumber toPhoneNumber(PhoneNumberDto phoneNumberDto) {
        if ( phoneNumberDto == null ) {
            return null;
        }

        PhoneNumberBuilder phoneNumber = PhoneNumber.builder();

        phoneNumber.operatorCode( phoneNumberDto.operatorCode() );
        phoneNumber.number( phoneNumberDto.number() );

        phoneNumber.countryCode( com.kopchak.worldoftoys.domain.order.recipient.number.CountryCode.UA );

        return phoneNumber.build();
    }
}

package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

public interface OrderPaymentService {
    void createOrder(OrderRecipientDto orderRecipientDto, AppUser user);
}

package com.kopchak.worldoftoys.dto.error;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;
}

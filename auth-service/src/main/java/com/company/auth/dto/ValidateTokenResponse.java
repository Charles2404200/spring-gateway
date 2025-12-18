package com.company.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

    private Boolean valid;

    private Long userId;

    private String username;

    private String message;

}


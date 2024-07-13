package com.network.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticateRequest {

    @Email(message = "Email is not formatted")
    @NotEmpty(message = "Email can not be empty!")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotEmpty(message = "Password can not be empty!")
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String password;
}

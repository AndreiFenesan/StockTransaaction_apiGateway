package com.example.gateway.authorization;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class PermissionCheckRequestBody {
    @NotBlank
    private String roleToHave;
}

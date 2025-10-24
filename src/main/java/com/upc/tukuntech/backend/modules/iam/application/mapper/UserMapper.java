package com.upc.tukuntech.backend.modules.iam.application.mapper;

import com.upc.tukuntech.backend.modules.iam.application.dto.RegisterRequest;
import com.upc.tukuntech.backend.modules.iam.domain.entity.UserIdentity;

public class UserMapper {

    public static UserIdentity toEntity(RegisterRequest request) {
        UserIdentity user = new UserIdentity();

        // Solo atributos gestionados por IAM
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setEnabled(true);

        return user;
    }
}


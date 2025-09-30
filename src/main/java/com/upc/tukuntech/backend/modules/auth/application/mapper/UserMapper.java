package com.upc.tukuntech.backend.modules.auth.application.mapper;

import com.upc.tukuntech.backend.modules.auth.application.dto.RegisterRequest;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;

public class UserMapper {

    public static UserEntity toEntity(RegisterRequest request) {
        UserEntity user = new UserEntity();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDni(request.dni());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setGender(request.gender());
        user.setAge(request.age());
        user.setBloodGroup(request.bloodGroup());
        user.setNationality(request.nationality());
        user.setAllergy(request.allergy());




        user.setEnabled(true);
        return user;
    }
}


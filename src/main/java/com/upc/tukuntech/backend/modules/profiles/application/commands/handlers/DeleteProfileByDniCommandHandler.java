package com.upc.tukuntech.backend.modules.profiles.application.commands.handlers;

import com.upc.tukuntech.backend.modules.profiles.application.commands.DeleteProfileByDniCommand;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DeleteProfileByDniCommandHandler {

    private final UserProfileRepository repository;

    public DeleteProfileByDniCommandHandler(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void handle(DeleteProfileByDniCommand cmd) {
        var profile = repository.findByDni(cmd.dni())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found with DNI: " + cmd.dni()));

        repository.delete(profile);
    }
}

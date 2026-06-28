package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userAdminService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userAdminService.getUsers();
    }

    @PatchMapping("/{userId}/status")
    public UserResponse updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return userAdminService.updateStatus(userId, request);
    }
}

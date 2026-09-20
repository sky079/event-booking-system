package com.sumit.eventbooking.api;

import com.sumit.eventbooking.model.auth.AuthResponse;
import com.sumit.eventbooking.model.auth.LoginRequest;
import com.sumit.eventbooking.model.auth.RegisterRequest;
import com.sumit.eventbooking.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthController {

    private final AuthService auth;

    @Operation(
            summary = "Register a new user",
            description = "Register a CUSTOMER or ORGANIZER account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Customer Registration",
                                    value = """
                                            {
                                              "name": "John Doe",
                                              "email": "john@example.com",
                                              "password": "password123",
                                              "role": "CUSTOMER"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RegisterRequest req) {

        return ResponseEntity.status(201).body(auth.register(req));
    }

    @Operation(
            summary = "Login user",
            description = "Login using email and password to receive a JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public AuthResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login",
                                    value = """
                                            {
                                              "email": "john@example.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest req) {

        return auth.login(req);
    }
}
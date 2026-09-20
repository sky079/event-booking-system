package com.sumit.eventbooking.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Application health APIs")
@SecurityRequirement(name = "bearerAuth")
public class HealthController {

    @Operation(
            summary = "Check application health",
            description = "Returns the current application status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Application is running",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Event Booking System is running"
                            )
                    )
            )
    })
    @GetMapping("/api/health")
    public String health() {
        return "Event Booking System is running";
    }
}
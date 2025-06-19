package pl.lodz.p.it.ssbd2025.ssbd02.interceptors;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Brak uwierzytelnienia – token nieprawidłowy lub brak tokenu"),
        @ApiResponse(responseCode = "403", description = "Brak wymaganej roli – dostęp zabroniony")
})
public @interface AuthorizedEndpoint {
}

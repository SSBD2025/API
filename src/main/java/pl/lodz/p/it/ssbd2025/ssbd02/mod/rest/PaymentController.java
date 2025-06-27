package pl.lodz.p.it.ssbd2025.ssbd02.mod.rest;

import com.nimbusds.jose.shaded.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.PaymentException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.AuthorizedEndpoint;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mod/payment")
@PropertySource("classpath:secrets.properties")
public class PaymentController {

    @Value("${app.domain}")
    private String DOMAIN;

    @Value("${app.stripe}")
    private String stripeApiKey;

    @Value("${app.stripe.price}")
    private String stripePrice;
    
    @PostMapping("/create-checkout-session")
    @PreAuthorize("permitAll()")
    public Map<String, String> createCheckoutSession() {
        Map<String, String> responseData = new HashMap<>();

        try {
            Stripe.apiKey = stripeApiKey;

            SessionCreateParams params = SessionCreateParams.builder()
                    .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setReturnUrl(DOMAIN + "/return?session_id={CHECKOUT_SESSION_ID}")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPrice(stripePrice)
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            responseData.put("clientSecret", session.getClientSecret());

        } catch (StripeException e) {
            throw new PaymentException();
        }

        return responseData;
    }
}
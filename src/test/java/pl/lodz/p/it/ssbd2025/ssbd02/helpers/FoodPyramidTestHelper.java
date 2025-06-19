package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FeedbackNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FoodPyramidNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientFoodPyramidRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientModRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FeedbackRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.FoodPyramidRepository;

import java.util.List;
import java.util.UUID;

@TestComponent
public class FoodPyramidTestHelper {

    private static final UUID EXISTING_PYRAMID_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Autowired
    private FoodPyramidRepository foodPyramidRepository;

    @Autowired
    private ClientModRepository clientModRepository;

    @Autowired
    private ClientFoodPyramidRepository clientFoodPyramidRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public FoodPyramid getFoodPyramid(UUID id) {
        return foodPyramidRepository.findById(id).orElseThrow(FoodPyramidNotFoundException::new);
    }

    @Transactional(readOnly = true, transactionManager = "modTransactionManager")
    public FoodPyramid getExistingFoodPyramid() {
        return foodPyramidRepository.findById(EXISTING_PYRAMID_ID)
                .orElseThrow(FoodPyramidNotFoundException::new);
    }

    @Transactional(readOnly = true, transactionManager = "modTransactionManager")
    public List<ClientFoodPyramid> getClientFoodPyramids(UUID clientId) {
        return clientFoodPyramidRepository.findByClientId(clientId);
    }
}
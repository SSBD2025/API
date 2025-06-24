package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
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
import java.util.Optional;
import java.util.UUID;

@TestComponent
public class FoodPyramidTestHelper {

    private static final UUID EXISTING_PYRAMID_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Autowired
    private FoodPyramidRepository foodPyramidRepository;


    @Autowired
    private ClientFoodPyramidRepository clientFoodPyramidRepository;

    @Autowired
    private ModTestHelper modTestHelper;


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
    @WithMockUser(roles = "DIETICIAN")
    public List<ClientFoodPyramid> getClientFoodPyramids(UUID clientId) {
        return clientFoodPyramidRepository.findByClientId(clientId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    public FoodPyramid createFoodPyramid(FoodPyramid foodPyramid) {
        return foodPyramidRepository.saveAndFlush(foodPyramid);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "modTransactionManager")
    @WithMockUser(roles = "DIETICIAN")
    public FoodPyramid findByName(String name) {
        modTestHelper.setDieticianContext();
        FoodPyramid foodPyramid = Optional.ofNullable(foodPyramidRepository.findByName(name))
                .orElseThrow(() -> new IllegalArgumentException("Food pyramid with name " + name + " not found"));
        modTestHelper.resetContext();
        return foodPyramid;
    }

    @Transactional(propagation = Propagation.MANDATORY, transactionManager = "modTransactionManager")
    @WithMockUser(roles = "DIETICIAN")
    public void deleteFoodPyramid(FoodPyramid foodPyramid) {
        foodPyramidRepository.delete(foodPyramid);
//        foodPyramidRepository.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
    @WithMockUser(roles = "DIETICIAN")
    public void deleteByName(String name) {
        FoodPyramid pyramid = findByName(name);
        deleteFoodPyramid(pyramid);
    }
}
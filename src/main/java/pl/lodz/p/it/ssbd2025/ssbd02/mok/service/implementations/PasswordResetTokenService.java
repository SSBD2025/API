package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;


import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenExpiredException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IPasswordResetTokenService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.util.Date;

@RequiredArgsConstructor
@Service
@EnableMethodSecurity(prePostEnabled=true)
@MethodCallLogged
public class PasswordResetTokenService implements IPasswordResetTokenService {

    private final TokenRepository tokenRepository;
    private final TokenUtil tokenUtil;

    @Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager", readOnly = false)
    @PreAuthorize("permitAll()")
    public void createPasswordResetToken(Account account, SensitiveDTO token) {
        if(tokenRepository.existsByAccountWithType(account, TokenType.PASSWORD_RESET)) {
            tokenRepository.deleteAllByAccountWithType(account, TokenType.PASSWORD_RESET);
        }
        tokenRepository.saveAndFlush(new TokenEntity(token.getValue(), tokenUtil.generateMinuteExpiration(5), account, TokenType.PASSWORD_RESET));
    }

    @Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager", readOnly = false)
    @PreAuthorize("permitAll()")
    public void validatePasswordResetToken(SensitiveDTO passwordResetToken) {
        TokenEntity passwordToken = tokenRepository.findByToken(passwordResetToken.getValue()).orElseThrow(TokenNotFoundException::new);
        tokenRepository.delete(passwordToken);
        if(new Date().after(passwordToken.getExpiration())){
            throw new TokenExpiredException();
        }
    }
}


package pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations;


import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.TokenExpiredException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.TokenNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.interfaces.IPasswordResetTokenService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.util.Date;

@RequiredArgsConstructor
@Service
@EnableMethodSecurity(prePostEnabled=true)
@MethodCallLogged
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public class PasswordResetTokenService implements IPasswordResetTokenService {

//    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenRepository tokenRepository;
    private final TokenUtil tokenUtil;

    public void createPasswordResetToken(Account account, String token) {
//        if(passwordResetTokenRepository.findByAccount(account) != null) {
//           passwordResetTokenRepository.deleteByAccount(account);
//        }
        if(tokenRepository.existsByAccountWithType(account, TokenType.PASSWORD_RESET)) {
            tokenRepository.deleteAllByAccountWithType(account, TokenType.PASSWORD_RESET);
        }
        tokenRepository.saveAndFlush(new TokenEntity(token, tokenUtil.generateMinuteExpiration(5), account, TokenType.PASSWORD_RESET));
//        PasswordResetToken passwordResetToken = new PasswordResetToken(token, account);

//        passwordResetTokenRepository.saveAndFlush(passwordResetToken);
    }

    public void validatePasswordResetToken(String passwordResetToken) {
        TokenEntity passwordToken = tokenRepository.findByToken(passwordResetToken).orElseThrow(TokenNotFoundException::new);
        tokenRepository.delete(passwordToken);
        if(new Date().after(passwordToken.getExpiration())){
            throw new TokenExpiredException();
        }
    }
}


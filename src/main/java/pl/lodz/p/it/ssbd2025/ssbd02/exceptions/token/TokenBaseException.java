package pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token;

import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;

public abstract class TokenBaseException extends AppBaseException {
  protected TokenBaseException(HttpStatusCode status, String reason) {
    super(status, reason);
  }

  protected TokenBaseException(HttpStatusCode status, String reason, Throwable cause) {
    super(status, reason, cause);
  }
}

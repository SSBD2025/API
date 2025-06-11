package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class ClientDTO {
        @Valid UserRoleDTO.ClientDTO client;
        @Valid AccountDTO account;

        private ClientDTO() {}

        @Override
        public String toString() {
            return "ClientDTO{" +
                    "client=" + client +
                    ", account=" + account +
                    '}';
        }
}

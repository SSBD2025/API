package pl.lodz.p.it.ssbd2025.ssbd02.dto;

public record AccountWithTokenDTO(
        AccountReadDTO account,
        String lockToken,
        Iterable<AccountRoleDTO> roles
) {
}

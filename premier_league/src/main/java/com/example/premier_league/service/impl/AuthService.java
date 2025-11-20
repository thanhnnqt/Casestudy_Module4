package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Account;
import com.example.premier_league.repository.IAccountRepository;
import com.example.premier_league.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final IAccountRepository accountRepository;

    @Override
    public Long getLoggedInTeamId(Principal principal) {
        if (principal == null) return null;

        String username = principal.getName();
        Optional<Account> accountOpt = accountRepository.findByUsername(username);

        if (accountOpt.isPresent() && accountOpt.get().getTeam() != null) {
            // Lấy teamId từ entity Account
            return accountOpt.get().getTeam().getId();
        }
        return null;
    }
}
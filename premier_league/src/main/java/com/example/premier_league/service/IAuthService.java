package com.example.premier_league.service;

import java.security.Principal;

public interface IAuthService {
    Long getLoggedInTeamId(Principal principal);
}

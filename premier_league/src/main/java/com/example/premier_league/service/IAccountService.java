package com.example.premier_league.service;

import com.example.premier_league.entity.Account;
import java.util.List;

public interface IAccountService {
    List<Account> findAll();
    Account findById(Long id);
}
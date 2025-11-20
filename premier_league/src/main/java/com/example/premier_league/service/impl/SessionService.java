package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Session;
import com.example.premier_league.repository.ISessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService implements ISessionService {
    final ISessionRepository sessionRepository;

    public SessionService(ISessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<Session> findAll() {
        return sessionRepository.findAll();
    }

    @Override
    public List<Session> findAllByStadium_Id(Integer stadiumId) {
        return sessionRepository.findAllByStadium_Id(stadiumId);
    }

    @Override
    public boolean save(Session session) {
        return sessionRepository.save(session) != null;
    }


    @Override
    public Session findByNameAndStadiumName(String name, String stadiumName) {
        return sessionRepository.findByNameAndStadium_Name(name, stadiumName);
    }

    @Override
    public Session findSessionById(Integer id) {
        return sessionRepository.findById(id).orElse(null);
    }
}

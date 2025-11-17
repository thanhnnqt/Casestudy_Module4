package com.example.premier_league.controller;



import com.example.premier_league.entity.Team;
import com.example.premier_league.serivce.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RankingRestController {
    private final RankingService rankingService;


    @GetMapping("/rankings")
    public List<?> getRankings() {
        return rankingService.getRanking();
    }
    @GetMapping("/rankings/{id}")
    public Team getRankTeam(@PathVariable Long id) {
        return rankingService.findById(id);
    }
    @PatchMapping("/rankings/{id}")
    public void updateRankTeam(@PathVariable Long id, @RequestBody Team team) {
        Team teamUpdate =  rankingService.findById(id);
        BeanUtils.copyProperties(team, teamUpdate);
        rankingService.save(teamUpdate);
    }

    @DeleteMapping("/rankings/{id}")
    public void deleteRankings(@PathVariable Long id){
         rankingService.delete(id);
    }
}
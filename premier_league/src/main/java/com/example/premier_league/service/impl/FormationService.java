package com.example.premier_league.service.impl;

import com.example.premier_league.dto.FormationDto;
import com.example.premier_league.service.IFormationService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FormationService implements IFormationService {

    // Sử dụng LinkedHashMap để giữ đúng thứ tự
    private final Map<String, FormationDto> formations = new LinkedHashMap<>();

    /**
     * Khởi tạo danh sách các sơ đồ cố định khi server chạy
     */
    @PostConstruct
    private void initFormations() {
        // Sơ đồ 4-4-2
        Map<String, String> f442 = new LinkedHashMap<>();
        f442.put("GK", "GK");
        f442.put("RB", "RB");
        f442.put("RCB", "CB");
        f442.put("LCB", "CB");
        f442.put("LB", "LB");
        f442.put("RM", "RM");
        f442.put("RCM", "CM");
        f442.put("LCM", "CM");
        f442.put("LM", "LM");
        f442.put("RST", "ST");
        f442.put("LST", "ST");
        formations.put("4-4-2", new FormationDto("4-4-2", f442));

        // Sơ đồ 4-3-3
        Map<String, String> f433 = new LinkedHashMap<>();
        f433.put("GK", "GK");
        f433.put("RB", "RB");
        f433.put("RCB", "CB");
        f433.put("LCB", "CB");
        f433.put("LB", "LB");
        f433.put("RCM", "CM");
        f433.put("CM", "CDM");
        f433.put("LCM", "CM");
        f433.put("RW", "RW");
        f433.put("ST", "ST");
        f433.put("LW", "LW");
        formations.put("4-3-3", new FormationDto("4-3-3", f433));

        // Sơ đồ 4-2-3-1
        Map<String, String> f4231 = new LinkedHashMap<>();
        f4231.put("GK", "GK");
        f4231.put("RB", "RB");
        f4231.put("RCB", "CB");
        f4231.put("LCB", "CB");
        f4231.put("LB", "LB");
        f4231.put("RDM", "CDM");
        f4231.put("LDM", "CDM");
        f4231.put("RAM", "CAM");
        f4231.put("CAM", "CAM");
        f4231.put("LAM", "CAM");
        f4231.put("ST", "ST");
        formations.put("4-2-3-1", new FormationDto("4-2-3-1", f4231));
    }

    @Override
    public Map<String, FormationDto> getAllFormations() {
        return formations;
    }

    @Override
    public FormationDto getFormationByName(String name) {
        return formations.getOrDefault(name, formations.get("4-4-2")); // Mặc định là 4-4-2
    }
}
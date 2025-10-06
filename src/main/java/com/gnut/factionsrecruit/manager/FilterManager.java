package com.gnut.factionsrecruit.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.gnut.factionsrecruit.model.FactionApplication;

public class FilterManager {

    private final Map<UUID, FactionApplication> playerFilters = new HashMap<>();

    public FactionApplication getPlayerFilters(UUID playerUuid) {
        return playerFilters.getOrDefault(playerUuid, new FactionApplication("", "", null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false, 0, 0));
    }

    public void setPlayerFilters(UUID playerUuid, FactionApplication filters) {
        playerFilters.put(playerUuid, filters);
    }

    public void clearPlayerFilters(UUID playerUuid) {
        playerFilters.remove(playerUuid);
    }

    public void toggleFilter(UUID playerUuid, String category, String value) {
        FactionApplication filters = getPlayerFilters(playerUuid);
        Set<String> categoryFilters;

        switch (category) {
            case "timezones":
                categoryFilters = new HashSet<>(filters.getDesiredTimezones());
                break;
            case "experience":
                categoryFilters = new HashSet<>(filters.getExperienceLevels());
                break;
            case "days":
                categoryFilters = new HashSet<>(filters.getRequiredDays());
                break;
            case "skills":
                categoryFilters = new HashSet<>(filters.getDesiredSkills());
                break;
            default:
                return;
        }

        if (categoryFilters.contains(value)) {
            categoryFilters.remove(value);
        } else {
            categoryFilters.add(value);
        }

        switch (category) {
            case "timezones":
                filters.setDesiredTimezones(new ArrayList<>(categoryFilters));
                break;
            case "experience":
                filters.setExperienceLevels(new ArrayList<>(categoryFilters));
                break;
            case "days":
                filters.setRequiredDays(new ArrayList<>(categoryFilters));
                break;
            case "skills":
                filters.setDesiredSkills(new ArrayList<>(categoryFilters));
                break;
        }

        setPlayerFilters(playerUuid, filters);
    }
}

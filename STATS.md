# Player Statistics System

## Overview
The player statistics system provides faction leaders with comprehensive data for recruitment decisions. Statistics are displayed in the player profile GUI (accessed via `/recruit <username>`) across 7 slots (10-16) with each slot focused on a specific aspect of player performance.

## Statistics Layout

### Slot 10: 🗡️ Combat Stats (DIAMOND_SWORD)
**Purpose**: Evaluate PVP performance and combat effectiveness

**Primary Metrics**:
- **K/D Ratio**: Kill-to-death ratio with calculated percentage
- **KOTH Wins**: King of the Hill victories using `%kore_koth_wins%`
- **Total Kills**: Player vs player eliminations
- **Mob Kills**: Total hostile mobs eliminated

**Placeholder Sources**:
```
Primary: %simplestats_kills%, %simplestats_deaths%, %kore_koth_wins%
Fallback: %statistic_player_kills%, %statistic_deaths%, %statistic_kill_entity_hostile%
Format: "K/D: 15/3 (5.0) | KOTH: 12"
```

### Slot 11: ⏰ Activity Stats (CLOCK)
**Purpose**: Measure player engagement and time investment

**Primary Metrics**:
- **Playtime**: Total hours played (current working implementation)
- **Login Streak**: Consecutive active days
- **Total Sessions**: Number of logins
- **Activity Rate**: Recent activity level

**Placeholder Sources**:
```
Primary: %statistic_time_played%, %server_total_playtime%
Fallback: %player_playtime%, server-specific tracking
Format: "142h 30m | Streak: 7 days"
```

### Slot 12: ⛏️ Building Stats (DIAMOND_PICKAXE)
**Purpose**: Track block interaction and building activity

**Primary Metrics**:
- **Blocks Mined**: Total blocks broken (separate line)
- **Blocks Placed**: Total blocks placed (separate line)
**Placeholder Sources**:
```
Primary: %statistic_mine_block%, %statistic_use_item%
Specific: %statistic_mine_block_stone%, %statistic_use_item_dirt%
Format: "Mined: 15,420\nPlaced: 8,765"
```

### Slot 13: 🌾 Grinding Stats (IRON_SWORD)
**Purpose**: Economic activity and resource farming performance

**Primary Metrics**:
- **Shop Sales**: Items sold to server shop (crops, materials)
- **Mob Farming**: Specific mob kills for resource grinding
- **Resource Collection**: Gathered materials (ores, wood, food)
- **Economic Activity**: Trading and market participation

**Placeholder Sources**:
```
Primary: EconomyShopGUI placeholders, shop plugin statistics
Secondary: %statistic_kill_entity_cow%, %statistic_harvest_crop%
Custom: Server-specific grinding metrics
Format: "Sales: $1,250 | Mobs: 2,341"
```

### Slot 14: 💰 Economy Stats (GOLD_INGOT)
**Purpose**: Financial standing and economic influence

**Primary Metrics**:
- **Current Balance**: Available money (working implementation)
- **Total Earned**: Cumulative income if tracked
- **Transaction Volume**: Buy/sell activity
- **Economic Rank**: Wealth-based server ranking

**Placeholder Sources**:
```
Primary: %vault_eco_balance%, %economy_balance%
Extended: Shop plugin transaction logs, custom tracking
Format: "$45,678 (Rank #12)"
```

### Slot 15: ⭐ Server Status (NETHER_STAR)
**Purpose**: Player standing and server-specific achievements

**Primary Metrics**:
- **Current Rank**: Permission group (working implementation)
- **Achievements**: Server-specific accomplishments
- **Reputation**: Community standing metrics
- **Special Status**: Earned titles, honors, badges

**Placeholder Sources**:
```
Primary: %vault_rank%, %luckperms_primary_group_name%
Extended: Achievement plugins, custom status systems
Format: "VIP | Achievements: 24"
```

### Slot 16: 📈 Experience Stats (ENCHANTED_BOOK)
**Purpose**: Character progression and skill development

**Primary Metrics**:
- **Current Level**: XP level (working implementation)
- **Total Experience**: Lifetime XP earned
- **Skill Progression**: McMMO or custom skills
- **Growth Rate**: Recent progression velocity

**Placeholder Sources**:
```
Primary: %player_level%, %player_total_experience%
McMMO: %mcmmo_power_level%, individual skill levels
Format: "Level 45 | Total XP: 2.1M"
```

## Implementation Phases

### Phase 1: Core Fixes (Immediate)
- [ ] Fix invalid block statistics placeholders
- [ ] Implement proper PVP statistics with K/D ratio
- [ ] Replace farming with grinding statistics
- [ ] Add multi-line support for complex stats

### Phase 2: Enhanced Sources (Short-term)
- [ ] Integrate KOTH wins placeholder
- [ ] Add shop-based grinding statistics
- [ ] Implement mob-specific kill tracking
- [ ] Enhance experience with total XP display

### Phase 3: Advanced Features (Medium-term)
- [ ] Add progress bars for comparative stats
- [ ] Implement color-coded performance indicators
- [ ] Create tooltip explanations for statistics
- [ ] Add historical trend analysis

### Phase 4: Custom Metrics (Long-term)
- [ ] Soul harvesting statistics
- [ ] Special event participation tracking
- [ ] Server-specific custom metrics
- [ ] Achievement and milestone tracking

## Placeholder Integration Priority

### High Priority (Essential)
1. **Combat**: SimpleStats or Player Stats plugin integration
2. **Blocks**: Core Minecraft statistics (fixed placeholders)
3. **Economy**: Vault integration (already working)
4. **KOTH**: Kore KOTH plugin integration

### Medium Priority (Enhanced)
1. **Shop Sales**: EconomyShopGUI or similar shop plugin
2. **Mob Farming**: Specific entity kill statistics
3. **Experience**: Total XP and progression tracking
4. **Achievements**: Server-specific accomplishment systems

### Low Priority (Optional)
1. **Login Streaks**: Custom tracking or plugin integration
2. **Reputation**: Community standing metrics
3. **Special Events**: Seasonal or event-specific statistics
4. **Custom Metrics**: Server-unique performance indicators

## Display Format Guidelines

### Single-Line Stats
```
Format: "Primary: Value | Secondary: Value"
Example: "Balance: $45,678 | Rank: VIP"
```

### Multi-Line Stats
```
Format: "Primary Metric: Value"
        "Secondary: Value"
Example: "Mined: 15,420"
         "Placed: 8,765"
```

### Ratio/Percentage Stats
```
Format: "Metric: X/Y (Ratio%)"
Example: "K/D: 15/3 (5.0)"
```

## Error Handling

### Placeholder Unavailable
- Display "No [stat type] data" with muted color
- Fall back to alternative placeholder sources
- Log missing placeholders for server admin review

### Invalid Values
- Sanitize numeric values (handle non-numbers)
- Format large numbers with appropriate units (K, M, B)
- Handle division by zero for ratio calculations

### Plugin Dependencies
- Gracefully handle missing plugins
- Provide meaningful fallback messages
- Document required plugins for full functionality

## Future Enhancements

### Planned Additions
1. **Soul Harvesting**: Custom server mechanic tracking
2. **Event Participation**: Special event statistics
3. **Faction Contributions**: Faction-specific performance metrics
4. **Seasonal Stats**: Time-based performance tracking

### Technical Improvements
1. **Caching**: Reduce placeholder API calls with smart caching
2. **Async Loading**: Non-blocking statistic retrieval
3. **Custom Calculations**: Server-specific metric formulas
4. **Data Persistence**: Historical statistics storage

This comprehensive statistics system transforms the player profile from basic information display into a powerful recruitment analysis tool for faction leaders.





### For block, item and entity:
- mine_block: block_name
- craft_item: item_name
- use_item: item_name
- break_item: item_name
- pickup: item_name
- drop: item_name
- kill_entity: entity_name
- entity_killed_by: entity_name

### General:
- animals_bred
- armor_cleaned
- aviate_one_cm
- banner_cleaned
- beacon_interaction
- bell_ring
- boat_one_cm
- brewingstand_interaction
- cake_slices_eaten
- cauldron_filled
- cauldron_used
- chest_opened
- clean_shulker_box
- climb_one_cm
- crafting_table_interaction
- crouch_one_cm
- damage_dealt
- damage_dealt_absorbed
- damage_dealt_resisted
- damage_taken
- damage_blocked_by_shield
- damage_absorbed
- damage_resisted
- deaths
- dispenser_inspected
- drop_count
- dropper_inspected
- enderchest_opened
- fall_one_cm
- flower_potted
- fly_one_cm
- fish_caught
- furnace_interaction
- hopper_inspected
- horse_one_cm
- interact_with_anvil
- interact_with_blast_furnace
- interact_with_campfire
- interact_with_cartography_table
- interact_with_grindstone
- interact_with_lectern
- interact_with_loom
- interact_with_smithing_table
- interact_with_smoker
- interact_with_stonecutter
- item_enchanted
- jump
- leave_game
- minecart_one_cm
- mob_kills
- noteblock_played
- noteblock_tuned
- open_barrel
- pig_one_cm
- play_one_minute
- player_kills
- raid_trigger
- raid_win
- record_played
- shulker_box_opened
- sleep_in_bed
- sneak_time
- sprint_one_cm
- strider_one_cm
- swim_one_cm
- talked_to_villager
- target_hit
- time_since_death
- time_since_rest
- total_world_time
- traded_with_villager
- trapped_chest_triggered
- walk_on_water_one_cm
- walk_one_cm
- walk_under_water_one_cm


/papi ecloud download playerstats


%playerstats_prefix% 
%playerstats_prefixtitle% 
%playerstats_rainbowprefix% 
%playerstats_rainbowprefixtitle% 
%playerstats_top:<n>,<statistic>% 
%playerstats_top:<n>,<statistic:item/block>% 
%playerstats_top:<n>,<statistic:entity>% 
%playerstats_top:<n>,<statistic>,only:number% 
%playerstats_top:<n>,<statistic>,only:number_raw%
%playerstats_top:<n>,<statistic>,only:player_name% 
%playerstats_player:<player-name>,<statistic>% 
%playerstats_player:<player-name>,<statistic:item/block>% 
%playerstats_player:<player-name>,<statistic:entity>% 
%playerstats_player:<player-name>,<statistic>,only:number% 
%playerstats_player:<player-name>,<statistic>,only:number_raw% 
%playerstats_me,<statistic>% 
%playerstats_me,<statistic:item/block>% 
%playerstats_me,<statistic:entity>% 
%playerstats_me,<statistic>,only:number% 
%playerstats_me,<statistic>,only:number_raw% 
%playerstats_server,<statistic>% 
%playerstats_server,<statistic:item/block>% 
%playerstats_server,<statistic:entity>% 
%playerstats_server,<statistic>,only:number% 
%playerstats_server,<statistic>,only:number_raw% 
%playerstats_title,<statistic>% 
%playerstats_title:[n],<statistic>% 
%playerstats_title:[n],<statistic:item/block>% 
%playerstats_title:[n],<statistic:entity>% 


## Statz plugin

%statz_deaths%
%statz_blocks_broken%
%statz_blocks_placed%
%statz_blocks_broken_<block_ID:data>%
%statz_blocks_placed_<block_ID:data>%
%statz_blocks_broken_<block_name>%
%statz_blocks_placed_<block_name>%
%statz_caught_items%
%statz_crafted_items%
%statz_damage_taken%
%statz_distance_traveled%
%statz_distance_traveled_allworlds_<moveType>%
%statz_distance_traveled_<world>%
%statz_distance_traveled_<world>:<moveType>%
%statz_food_eaten%
%statz_food_eaten_<food_name>%
%statz_joins%
%statz_mobs_killed%
%statz_mobs_killed_<mob_name>%
%statz_players_killed%
%statz_time_formated_dhm%
%statz_time_formated_dh%
%statz_time_formated_d%
%statz_time_day%
%statz_time_hour%
%statz_time_minute%
%statz_time_played%
%statz_times_shorn%
%statz_villager_trades%
%statz_villager_trades_<item_name>%
%statz_xp_gained%
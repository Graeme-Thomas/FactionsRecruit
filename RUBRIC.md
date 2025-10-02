# FactionsRecruit Feature Rubric

This rubric outlines the key features of the FactionsRecruit plugin for tracking development progress.

## Core Features

- [ ] **Player Resume Management**
    - [ ] Create/Edit Resume
    - [ ] View Own Resume
    - [ ] Delete Resume
    - [ ] Resume fields: IGN, Faction, KDR, Power, Role, Playstyle, Experience, Why Join, Discord, Online Hours
- [ ] **Faction Application Management**
    - [ ] Faction leaders set application requirements
    - [ ] View incoming applications
    - [ ] Accept/Deny applications
- [ ] **In-Game GUI**
    - [ ] Main Menu GUI
    - [ ] Player Resume GUI
    - [ ] Faction Application GUI
    - [ ] Browse Players GUI
    - [ ] Browse Factions GUI
    - [ ] Application Review GUI
    - [ ] Invitation Management GUI
- [ ] **Application and Invitation System**
    - [ ] Players apply to factions
    - [ ] Factions invite players
    - [ ] Configurable timeouts for requests/invitations
    - [ ] Cooldowns for sending invitations
- [ ] **Login and Notification System**
    - [ ] Notifications for new applications/invitations
    - [ ] Notifications for accepted/denied applications
    - [ ] Notifications for expired requests/invitations
- [ ] **PlaceholderAPI Integration**
    - [ ] Support for FactionsUUID placeholders
    - [ ] Custom FactionsRecruit placeholders
- [ ] **Database Schema**
    - [ ] `player_resumes` table
    - [ ] `faction_applications` table
    - [ ] `faction_invitations` table
    - [ ] `recruitment_requests` table
    - [ ] `login_notifications` table
    - [ ] `edit_cooldowns` table
- [ ] **Automated Cleanup**
    - [ ] Cleanup of expired applications
    - [ ] Cleanup of expired invitations
    - [ ] Cleanup of old notifications

## Commands

- [ ] `/recruit` (main player command)
    - [ ] `/recruit resume`
    - [ ] `/recruit apply <faction>`
    - [ ] `/recruit browse players`
    - [ ] `/recruit browse factions`
    - [ ] `/recruit applications`
    - [ ] `/recruit invitations`
- [ ] `/recruitadmin` (main admin command)
    - [ ] `/recruitadmin reload`
    - [ ] `/recruitadmin cleanup`
    - [ ] `/recruitadmin stats`
    - [ ] `/recruitadmin reset <player>`
    - [ ] `/recruitadmin faction <faction> status`

## UI/GUI Details

- [ ] **Main Menu GUI**
    - [ ] Open resume button
    - [ ] Browse players button
    - [ ] Browse factions button
    - [ ] View applications button
    - [ ] View invitations button
- [ ] **Player Resume GUI**
    - [ ] Display resume details
    - [ ] Edit resume button
    - [ ] Delete resume button
- [ ] **Faction Application GUI**
    - [ ] Display application form
    - [ ] Submit application button
- [ ] **Browse Players GUI**
    - [ ] List available players with resumes
    - [ ] Click to view player resume
    - [ ] Option to send invitation
- [ ] **Browse Factions GUI**
    - [ ] List available factions
    - [ ] Click to view faction details/apply
- [ ] **Application Review GUI**
    - [ ] List pending applications for faction leaders
    - [ ] Accept/Deny buttons
- [ ] **Invitation Management GUI**
    - [ ] List pending invitations for players
    - [ ] Accept/Deny buttons

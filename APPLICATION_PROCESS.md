# Faction Recruitment Application Process

This document explains how the faction recruitment system works from both the player and faction leader perspectives.

## For Players Looking for a Faction

### Creating Your Resume

1. **Initial Setup**: Use `/recruit` to open the recruitment browser
2. **Resume Editor**: Click the "Manage Resume" button to create or edit your profile
3. **Required Information**:
   - **Timezone**: Select your primary timezone (single choice)
   - **Experience**: Choose your factions experience level (single choice)
   - **Available Days**: Select which days you can be online (multiple choice)
   - **Skills**: Choose up to 3 skills you're good at (multiple choice, max 3)

### Resume Visibility and Status

- **Looking Status**: Toggle whether your resume is visible to faction leaders
- **Display Duration**: When you set yourself as "looking", your resume is visible for 3 days by default
- **Edit Cooldown**: You can only edit your resume once every 6 hours
- **Automatic Hiding**: Your resume automatically becomes hidden after the display period expires

### Application System

#### Application Slots
- **Slot Limit**: You can have a maximum of 3 pending applications at any time
- **Slot Duration**: Each application uses a slot for 3 days, regardless of outcome
- **Slot Recovery**: Application slots become available again 3 days after submission
- **Cooldown**: 24-hour cooldown between applications to the same faction

#### Making Applications
1. Browse factions using the recruitment browser
2. Click on a faction to view their requirements
3. Submit an application if you meet their criteria
4. Wait for the faction leader's response (accept/reject)

#### Application Lifecycle
- **Pending**: Application submitted, waiting for faction leader response
- **Accepted**: Faction leader has accepted your application
- **Rejected**: Faction leader has rejected your application
- **Expired**: Application expired after 3 days without response
- **Cancelled**: You cancelled your own application (only after 24 hours)

### Notifications

#### Login Notifications
You'll receive notifications when logging in for:
- Expired applications that need attention
- New faction invitations waiting for response
- Available application slots when they free up
- Resume expiration reminders

#### Real-time Notifications
Immediate notifications for:
- Application status changes (accepted/rejected)
- New faction invitations received
- Application confirmations

## For Faction Leaders

### Setting Up Recruitment

1. **Access Requirements Editor**: Use `/recruit` → "Manage Applications" → "Edit Requirements"
2. **Define Criteria**: Set your faction's recruitment preferences:
   - **Desired Timezones**: Which timezones you want (multiple choice)
   - **Experience Levels**: Acceptable experience ranges (multiple choice)
   - **Required Days**: Days when members should be active (multiple choice)
   - **Desired Skills**: Skills you're looking for (multiple choice)

### Managing Applications

#### Application Review Process
1. **View Applications**: Access pending applications through the management interface
2. **Review Candidates**: Click on applicant heads to view their full resumes
3. **Make Decisions**: Accept or reject applications
4. **Send Invitations**: Directly invite players you find suitable

#### Application Processing
- **Response Time**: Applications expire after 3 days if not processed
- **Immediate Effects**: Accepted players can join immediately
- **Notification System**: Both you and the applicant receive status updates

### Faction Visibility

#### Automatic Management
- **Member Limit**: Factions automatically hide from browser when member limit (30) is reached
- **Accepting Status**: Toggle whether your faction is currently accepting applications
- **Requirements Update**: Changes to requirements are immediately active

#### Edit Limitations
- **Edit Cooldown**: Faction requirements can only be edited once every 6 hours
- **Leader Restriction**: Only faction leaders can manage recruitment settings
- **Persistent Settings**: Requirements persist until manually changed

### Invitation System

#### Direct Invitations
- **Browse Players**: View players looking for factions
- **Send Invitations**: Directly invite suitable candidates
- **Invitation Duration**: Invitations expire after 3 days
- **Response Tracking**: Receive notifications when invitations are accepted/rejected

## System Mechanics

### Cooldown System
- **Resume Edits**: 6 hours between resume modifications
- **Application Edits**: 6 hours between faction requirement changes
- **Application Submissions**: 24 hours between applications to the same faction

### Data Retention
- **Active Period**: Applications and invitations are active for 3 days
- **Cleanup**: Expired data is automatically cleaned after 30 days
- **Status Tracking**: All application states are preserved for the active period

### Matching Algorithm
- **Compatibility**: System shows factions/players based on matching criteria
- **Filter Options**: Advanced filtering available for both sides
- **Search Function**: Name-based search for specific players or factions

## Best Practices

### For Players
- Keep your resume updated and accurate
- Be selective with applications to maximize your 3 slots
- Respond promptly to invitations (3-day expiry)
- Use the filter system to find compatible factions

### For Faction Leaders
- Set realistic and clear requirements
- Review applications promptly (3-day expiry)
- Use direct invitations for highly desired candidates
- Keep recruitment status updated based on current needs

## Error Handling

### Common Issues
- **Slot Exhaustion**: Cannot apply when all 3 slots are used
- **Cooldown Active**: Must wait before editing resume/requirements
- **Expired Applications**: Automatic expiry after 3 days
- **Permission Denied**: Only faction leaders can manage recruitment

### Recovery Options
- **Slot Recovery**: Wait for automatic slot recovery after 3 days
- **Resume Reset**: Admins can reset player data if needed
- **Faction Transfer**: Leadership changes automatically transfer recruitment management
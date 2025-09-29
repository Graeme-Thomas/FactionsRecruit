all UIs must use the server theme of a pink and white glass pane alternating border around UIs
all UIs must either have a close or go back button in the middle of the bottom row of the UI
UIs that contain multiple of an icon that need to be paged must have forward and backward arrows(if there is a next or previous page)


### Landing UI


Slot Layout (0-53):

Header(must be centered and use server theme): Recruitment
Row 1: [0:PINK]     [1:WHITE]   [2:PINK]     [3:WHITE]              [4:PINK]            [5:WHITE]               [6:PINK]                [7:WHITE]   [8:PINK]
Row 2: [9:WHITE]    [10:]       [11:]        [12:]                  [13:]               [14:]                   [15:]                   [16:]       [17:WHITE]
Row 3: [18:PINK]    [19:]       [20:]        [21:Faction Listings]  [22:]               [23:Player Listings]    [24:]                   [25:]       [26:PINK]
Row 4: [27:WHITE]   [28:]       [29:Status]  [30:]                  [31:Toggle listing] [32:]                   [33:Create listing]     [34:]       [35:WHITE]
Row 5: [36:PINK]    [37:]       [38:]        [39:]                  [40:]               [41:]                   [42:]     [43:]       [44:PINK]
Row 6: [45:WHITE]   [46:PINK]   [47:WHITE]   [48:PINK]              [49:CLOSE]          [50:PINK]               [51:WHITE]              [52:PINK]   [53:WHITE]

Button 38 depends on wether a player is in a faction or not.

if in faction will check incoming applications and outgoing invites

if not in faction it will check outgoing applications and incoming invitations

Empty slots are filled with black stained glass

Items:
21: Player Head
23: Faction Banner
29: Jukebox
31: Spyglass(toggled on)(enchantment glare), Grey Dye(toggled off)(unenchanted)
33: Lectern

### Factions Recruiting UI (from clicking on Banner)
Header(must be centered and use server theme): Factions Recruiting
Row 1: [0:PINK]     [1:WHITE]   [2:PINK]     [3:WHITE]              [4:PINK]            [5:WHITE]               [6:PINK]                [7:WHITE]   [8:PINK]
Row 2: [9:WHITE]    [10:]       [11:]        [12:]                  [13:]               [14:]                   [15:]                   [16:]       [17:WHITE]
Row 3: [18:PINK]    [19:]       [20:]        [21:]                  [22:]               [23:]                   [24:]                   [25:]       [26:PINK]
Row 4: [27:WHITE]   [28:]       [29:]        [30:]                  [31:]               [32:]                   [33:]                   [34:]       [35:WHITE]
Row 5: [36:PINK]    [37:]       [38:]        [39:]                  [40:]               [41:]                   [42:]                   [43:]       [44:PINK]
Row 6: [45:WHITE]   [46:PINK]   [47:WHITE]   [48:PINK]              [49:CLOSE]          [50:PINK]               [51:WHITE]              [52:PINK]   [53:WHITE]
45: Back arrow or White depending on page
53: Back arrow or White depending on page
Empty spaces contain banners with faction details
Banner is a factions banner set by faction. Default is a yellow banner.


### Looking For Faction UI (From clicking player head)
Header(must be centered and use server theme): Looking for a Faction
Row 1: [0:PINK]     [1:WHITE]   [2:PINK]     [3:WHITE]              [4:PINK]            [5:WHITE]               [6:PINK]                [7:WHITE]   [8:PINK]
Row 2: [9:WHITE]    [10:]       [11:]        [12:]                  [13:]               [14:]                   [15:]                   [16:]       [17:WHITE]
Row 3: [18:PINK]    [19:]       [20:]        [21:]                  [22:]               [23:]                   [24:]                   [25:]       [26:PINK]
Row 4: [27:WHITE]   [28:]       [29:]        [30:]                  [31:]               [32:]                   [33:]                   [34:]       [35:WHITE]
Row 5: [36:PINK]    [37:]       [38:]        [39:]                  [40:]               [41:]                   [42:]                   [43:]       [44:PINK]
Row 6: [45:WHITE]   [46:PINK]   [47:WHITE]   [48:PINK]              [49:CLOSE]          [50:PINK]               [51:WHITE]              [52:PINK]   [53:WHITE]
45: Back arrow or White depending on page
53: Back arrow or White depending on page
Empty spaces contain player heads with application details
Click to send invitation if player is a faction leader (player doesn't have option to send to self)


### Players Application UI (From clicking Jukebox)
Header(must be centered and use server theme): Looking for a Faction
Row 1: [0:PINK]     [1:WHITE]   [2:PINK]     [3:WHITE]              [4:PINK]            [5:WHITE]               [6:PINK]                [7:WHITE]   [8:PINK]
Row 2: [9:WHITE]    [10:]       [11:]        [12:]                  [13:]               [14:]                   [15:]                   [16:]       [17:WHITE]
Row 3: [18:PINK]    [19:]       [20:]        [21:]                  [22:]               [23:]                   [24:]                   [25:]       [26:PINK]
Row 4: [27:WHITE]   [28:]       [29:]        [30:]                  [31:]               [32:]                   [33:]                   [34:]       [35:WHITE]
Row 5: [36:PINK]    [37:]       [38:]        [39:]                  [40:]               [41:]                   [42:]                   [43:]       [44:PINK]
Row 6: [45:WHITE]   [46:PINK]   [47:WHITE]   [48:PINK]              [49:CLOSE]          [50:PINK]               [51:WHITE]              [52:PINK]   [53:WHITE]
45: Back arrow or White depending on page
53: Back arrow or White depending on page
Empty spaces contain outgoing or incoming applications/invitations as player heads or banners.
### Application UI(From clicking Lectern)

Header(must be centered and use server theme): Looking for a Faction
Row 1: [0:PINK]     [1:WHITE]     [2:PINK]       [3:WHITE]      [4:Ready?]            [5:WHITE]               [6:PINK]             [7:WHITE]  [8:PINK]
Row 2: [9:WHITE]    [10:]         [11:Timezone]  [12:]          [13:Discord]        [14:]                   [15:Factions]        [16:]      [17:WHITE]
Row 3: [18:PINK]    [19:]         [20:]          [21:Raiding]   [22:]               [23:Building]           [24:]                [25:]      [26:PINK]
Row 4: [27:PINK]    [28:]         [29:PvP]       [30:]          [31:Availability]   [32:]                   [33:Prev Facs]       [34:]      [35:PINK]
Row 5: [36:WHITE]   [37:PINK]     [38:WHITE]     [39:PINK]      [40:CLOSE]          [41:PINK]               [42:WHITE]           [43:PINK]  [44:Grey Dye]

4  Lever/Redstone Torch, If all fields are filled in becomes redstone torch. Ready to submit settings or not.
11 Clock: Opens Timezone select UI
13 Anvil string input UI
15 Opens 1-10 selector UI
21 Opens 1-10 selector UI
23 Opens 1-10 selector UI
29 Opens 1-10 selector UI
31 Opens time ranges selection UI
33 Opens anvil string input UI (appends to the list of factions) (rightclicking clears list)
44 Clears All selections.

53: Clear All selections

### One to ten selection UI

Header(must be centered and use server theme): [skillname] Experience
Row 1: [0:PINK]     [1:WHITE]     [2:PINK]      [3:WHITE]    [4:]        [5:WHITE]    [6:PINK]        [7:WHITE]  [8:PINK]
Row 2: [9:WHITE]    [10:]         [11:One]      [12:Two]     [13:Three]        [14:Four]    [15:Five]       [16:]      [17:WHITE]
Row 3: [18:PINK]    [19:]         [20:Six]      [21:Seven]   [22:Eight]        [23:Nine]    [24:Ten]        [25:]      [26:PINK]
Row 4: [27:PINK]    [28:WHITE]    [29:PINK]     [30:WHITE]   [31:Close]        [32:WHITE]   [33:PINK]       [34:WHITE] [35:PINK]

Use gradient from red to green with terracotta blocks

### Timezone Selection UI
Header(must be centered and use server theme): [skillname] Experience
Row 1: [0:PINK]     [1:WHITE]     [2:PINK]          [3:WHITE]    [4:]        [5:WHITE]    [6:PINK]        [7:WHITE]  [8:PINK]
Row 2: [9:WHITE]    [10:]         [11:NAW]          [12:]        [13:NAE]    [14:]        [15:EUW]        [16:]      [17:WHITE]
Row 3: [18:PINK]    [19:]         [20:EUCENTRAL]    [21:]        [22:ASIA]   [23:]        [24:OCEANIA]    [25:]      [26:PINK]
Row 4: [27:PINK]    [28:WHITE]    [29:PINK]         [30:WHITE]   [31:Close]  [32:WHITE]   [33:PINK]       [34:WHITE] [35:PINK]


### Avail selection UI

Header(must be centered and use server theme): Hours available per week.
Row 1: [0:PINK]     [1:WHITE]     [2:PINK]      [3:WHITE]    [4:]        [5:WHITE]    [6:PINK]        [7:WHITE]  [8:PINK]
Row 2: [9:WHITE]    [10:]         [11:One]      [12:Two]     [13:Three]        [14:Four]    [15:Five]       [16:]      [17:WHITE]
Row 3: [18:PINK]    [19:]         [20:Six]      [21:Seven]   [22:Eight]        [23:Nine]    [24:Ten]        [25:]      [26:PINK]
Row 4: [27:PINK]    [28:WHITE]    [29:PINK]     [30:WHITE]   [31:Close]        [32:WHITE]   [33:PINK]       [34:WHITE] [35:PINK]

Same but one => less than 10 hours
two => 10-20 hours
.
.
.
10 => more than 100 hours



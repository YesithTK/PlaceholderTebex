# PlaceholderTebex

**Real-time Tebex store data as PlaceholderAPI variables.**
Top donors · Monthly goals · Live stats · Fully configurable.

[![Version](https://img.shields.io/badge/version-1.0.1-gold?style=flat-square)](https://github.com/YesithTK/PlaceholderTebex)
[![Paper](https://img.shields.io/badge/Paper-1.20--1.21-white?style=flat-square)](https://papermc.io)
[![PlaceholderAPI](https://img.shields.io/badge/PlaceholderAPI-2.11%2B-white?style=flat-square)](https://www.spigotmc.org/resources/placeholderapi.6245/)
[![Java](https://img.shields.io/badge/Java-17%2B-white?style=flat-square)](https://adoptium.net)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red?style=flat-square)](#license)

---

## Overview

PlaceholderTebex bridges your **Tebex plugin** and **PlaceholderAPI**, exposing live donation data as placeholders usable anywhere — holograms, scoreboards, menus, signs, chat, and more.

Data is fetched asynchronously and cached in memory. Every placeholder resolves instantly with zero per-request API calls, keeping your server fast and your Tebex rate limits safe.

---

## Requirements

| Dependency | Version |
| --- | --- |
| Paper / Spigot | 1.20.x — 1.21.x |
| [Tebex Plugin](https://www.spigotmc.org/resources/tebex-formerly-buycraft.336/) | Latest |
| PlaceholderAPI | 2.11.x or higher |
| Java | 17 or higher |

---

## Installation

1. Make sure the **Tebex plugin** is already installed and working on your server.
2. Place `PlaceholderTebex.jar` in your `/plugins` folder.
3. Start the server once to generate the config.
4. Open `plugins/Tebex/config.yml` and copy the value of `secret`.
5. Paste it into `plugins/PlaceholderTebex/config.yml` under `tebex.secret-key`.
6. Run `/ptebex reload`.

> **Warning:** Never share your secret key. It grants full access to your Tebex store.

---

## Configuration

```yaml
tebex:
  secret-key: "YOUR_TEBEX_SECRET_KEY"
  base-url: "https://plugin.tebex.io"

cache:
  refresh-interval: 10         # Minutes between Tebex API fetches
  payment-history-limit: 100   # Number of past payments to load (0 = whole history)

goal:
  enabled: true
  target-amount: 500.0         # Monthly donation target

placeholders:
  no-data: "N/A"
  loading: "Loading..."
  currency-format: "%.2f"
  date-format: "dd/MM/yyyy"
  datetime-format: "dd/MM/yyyy HH:mm"
```

---

## Placeholders

### Last Donor

| Placeholder | Returns |
| --- | --- |
| `%tebex_last_donor_name%` | Username of the last donor |
| `%tebex_last_donor_amount%` | Amount paid |
| `%tebex_last_donor_currency%` | Currency used |
| `%tebex_last_donor_date%` | Date of the donation |
| `%tebex_last_donor_datetime%` | Date and time of the donation |
| `%tebex_last_donor_package%` | Package purchased |
| `%tebex_last_donor_transaction%` | Transaction ID |

### All-Time Top Donors — replace `N` with `1` through `10`

| Placeholder | Returns |
| --- | --- |
| `%tebex_top_name_N%` | Username of top donor #N |
| `%tebex_top_amount_N%` | Total amount donated by #N |
| `%tebex_top_currency_N%` | Currency |
| `%tebex_top_date_N%` | Date of last payment |
| `%tebex_top_package_N%` | Package purchased |

### Monthly Top Donors — replace `N` with `1` through `10`

| Placeholder | Returns |
| --- | --- |
| `%tebex_monthly_top_name_N%` | Username of monthly top #N |
| `%tebex_monthly_top_amount_N%` | Amount donated this month |
| `%tebex_monthly_top_currency_N%` | Currency |
| `%tebex_monthly_top_date_N%` | Date of donation |
| `%tebex_monthly_top_package_N%` | Package purchased |
| `%tebex_monthly_total%` | Total donated this month |
| `%tebex_monthly_transactions%` | Transaction count this month |

### General Statistics

| Placeholder | Returns |
| --- | --- |
| `%tebex_total_donated%` | All-time total donated |
| `%tebex_total_donated_month%` | Total donated this month |
| `%tebex_total_donated_today%` | Total donated today |
| `%tebex_total_transactions%` | All-time transaction count |
| `%tebex_total_transactions_month%` | Transactions this month |
| `%tebex_total_transactions_today%` | Transactions today |
| `%tebex_unique_donors%` | Number of unique donors |

### Donation Goal

| Placeholder | Returns |
| --- | --- |
| `%tebex_goal_target%` | Target amount set in config |
| `%tebex_goal_current%` | Amount raised this month |
| `%tebex_goal_remaining%` | Amount remaining to reach the goal |
| `%tebex_goal_progress%` | Percentage with symbol — e.g. `73.50%` |
| `%tebex_goal_progress_raw%` | Raw number — e.g. `73.50` |
| `%tebex_goal_bar%` | Visual progress bar, 20 characters |
| `%tebex_goal_bar_10%` | Visual progress bar, 10 characters |
| `%tebex_goal_reached%` | `true` or `false` |

### Store Info

| Placeholder | Returns |
| --- | --- |
| `%tebex_store_name%` | Store name |
| `%tebex_store_currency%` | Default currency |
| `%tebex_store_domain%` | Store domain |

---

## Usage Examples

**Top donors hologram**
```
&6&lTOP DONORS
&e#1 &f%tebex_top_name_1% &7— &a$%tebex_top_amount_1%
&e#2 &f%tebex_top_name_2% &7— &a$%tebex_top_amount_2%
&e#3 &f%tebex_top_name_3% &7— &a$%tebex_top_amount_3%
```

**Monthly goal scoreboard**
```
Goal: %tebex_goal_bar_10% %tebex_goal_progress%
```

**Last donor sign**
```
%tebex_last_donor_name% — $%tebex_last_donor_amount%
%tebex_last_donor_datetime%
```

---

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/ptebex reload` | Reload config and restart cache | `placeholdertebex.admin` |
| `/ptebex refresh` | Force fetch fresh data from Tebex | `placeholdertebex.admin` |
| `/ptebex clearcache` | Clear all cached data | `placeholdertebex.admin` |
| `/ptebex info` | Show plugin status and stats | `placeholdertebex.admin` |

Default permission: **op only.**

---

## Changelog

### v1.0.1
- Fixed player name field — now correctly reads `player.name` from Tebex API
- Fixed JSON parsing — handles both array and wrapped `data` response formats
- Fixed date parsing — supports multiple Tebex date formats
- Improved error logging
- Removed verbose debug logs

### v1.0.0
- Initial release

---

## Support

- **Discord:** `_onlynea`
- **Spigot:** [PlaceholderTebex](https://www.spigotmc.org/resources/placeholdertebex.136478/)
- **Issues:** [github.com/YesithTK/PlaceholderTebex/issues](https://github.com/YesithTK/PlaceholderTebex/issues)

---

## License

© 2026 YesithX — All Rights Reserved.

This plugin is provided free of charge for personal and server use. You may not copy, modify, decompile, resell, or redistribute this plugin or any part of its source code without explicit written permission from the author.

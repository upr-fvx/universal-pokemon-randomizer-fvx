# Changes

---
## General

- Better Support for Poorly Decrypted ROMs

Decrypted ROMs that previously caused an error when being loaded (with the error log stating a `NullPointerException` as being the reason) should now work. Trying to load an encrypted ROM now gives a more clear error message to the user.

---
## Starters, Statics & Trades

### Starter Pokemon

- Setting: Ban Bad Items

When randomizing Starter held items in Gen 2/3 games, this option can now be selected.

### Static Pokemon

- Setting: Swap Mega Evolvables

For X/Y, this setting has been removed for Static Pokemon and is instead forced to be active, in order to avoid an issue where you can get softlocked on the Successor Korrina battle.

---
## Wild Pokemon

### Wild Pokemon

- Setting: Area 1-to-1 Mapping

Fixed a crash that would happen when this setting was used with "Similar Strength" and "Allow Alternate Formes" in Gen 7.

---
## Items

### Special Shops

- Setting: Balance Shop Prices

Should now set all prices correctly in Pt/HG/SS.
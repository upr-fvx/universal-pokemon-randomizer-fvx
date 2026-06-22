This branch contains rudimentary support for pokecrystal-speedchoice v8. That is, it passes all unit tests.

When this effort was started (2025-10-18), the most up-to-date pokecrystal-speedchoice branch was https://github.com/choatix/zxplus. However, it diverged too early, and FVX changes too much of the general code layout. It was deemed easier to lift the ROM Entry, and implement needed features from scratch, than try to merge anything using GitHub... sadly, since it would be ideal to credit everyone involved in speedchoice, and Git does that well. Dunno what the best solution for that is :/.

Below is a list of what was/is needed to give speedchoice support on par with the Vanilla games, and then what features ZX-plus has that FVX currently lacks. The prior are prioritized, especially since some of the latter are tricky to implement. 

Though there might be more. Use the following Git commands to investigate: `git remote add zxplus https://github.com/choatix/zxplus.git
`, `git log zxplus/main ^master --oneline --graph`, `git show <commit>`.

## The list:

To be supported on par with the Vanilla games
- [x] allow Unown to appear without unlocking
- [x] complete ROM Entry 
  - [x] shop offsets
  - [x] graphics offsets
  - [x] misc tweak offsets
- [x] write the checksum to ROM

To be feature-complete with ZX-plus:
- [x] better movesets for Gen 2
- [x] option to provide custom seed via CLI
- [ ] option to even out enc rates
- [ ] add logs for field items
- [ ] add logs for encounter percentages
- [ ] BST randomization (tricky)
- [ ] ensure compatibility with the map/key item randomizer

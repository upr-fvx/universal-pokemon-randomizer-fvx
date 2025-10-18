Below is a checklist, to make UPR FVX work with pokecrystal-speedchoice.

At the time of writing (2025-10-18), the most up-to-date pokecrystal-speedchoice branch is https://github.com/choatix/zxplus. However, it has diverged too early, and FVX changes too much of the general code layout. It should be easier to implement the below, partially from scratch, than try to merge anything using GitHub... sadly, since it would be ideal to credit everyone involved in speedchoice, and Git does that well. Dunno what the best solution for that is :/.

In any case, the below is a minimum list for things needed, to assert that FVX has pokecrystal-speedchoice support on par with the current speedchoice Randomizers / ZX-plus. 

Though there might be more. Use the following Git commands to investigate: `git remote add zxplus https://github.com/choatix/zxplus.git
`, `git log zxplus/main ^master --oneline --graph`, `git show <commit>`.

## The list:

- add ROM Entry
- write the checksum to ROM somehow

- better movesets for Gen 1+2
- add option to provide custom seed via CLI
- add logs for field items
- add logs for enc percentages

- allow unown in speedchoice

- BST randomization (tricky)
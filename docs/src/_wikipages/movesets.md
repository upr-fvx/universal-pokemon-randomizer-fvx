---
name: Movesets
---
## Random (preferring same type)

This setting will randomize moves with the following probabilities:

### Monotype Pokemon

* 40% Pokemon's type
* 60% Random

### Dual-type Pokemon (one type is Normal)

* 10% Normal
* 30% Pokemon's other type
* 60% Random

### Dual-type Pokemon

* 20% Pokemon's first type
* 20% Pokemon's second type
* 60% Random

Additionally, due to some significant power imbalances between different types, this setting tries to balance the power a bit so that types with a lot of low-power/status moves aren't always going to be significantly weaker (and vice versa for types with a lot of high-power moves).

## Good Damaging Moves

When using the "Force % of Good Damaging Moves" option, the following cases count as "Good Damaging Moves":

* Moves with at least 100 effective base power
* Moves with at least 50 effective base power and at least 90 accuracy

where "effective base power" is the move's base power multiplied by its average hit count.
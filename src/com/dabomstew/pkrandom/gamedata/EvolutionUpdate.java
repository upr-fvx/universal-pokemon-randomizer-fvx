package com.dabomstew.pkrandom.gamedata;

@Deprecated
public class EvolutionUpdate implements Comparable<EvolutionUpdate> {

    private final Species from;
    private final Species to;
    private final String fromName;
    private final String toName;
    private final EvolutionType type;
    private final String extraInfo;
    private final boolean condensed;
    private final boolean additional;

    public EvolutionUpdate(Species from, Species to, EvolutionType type, String extraInfo, boolean condensed, boolean additional) {
        this.from = from;
        this.to = to;
        fromName = from.getFullName();
        toName = to.getFullName();
        this.type = type;
        this.extraInfo = extraInfo;
        this.condensed = condensed;
        this.additional = additional;
    }

    public boolean isCondensed() {
        return condensed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EvolutionUpdate other = (EvolutionUpdate) obj;
        return from == other.from && to == other.to && type == other.type;
    }

    @Override
    public int compareTo(EvolutionUpdate o) {
        if (this.from.getNumber() < o.from.getNumber()) {
            return -1;
        } else if (this.from.getNumber() > o.from.getNumber()) {
            return 1;
        } else return Integer.compare(this.to.getNumber(), o.to.getNumber());
    }

    @Override
    public String toString() {
        switch (type) {
            case LEVEL:
                if (condensed) {
                    String formatLength = this.additional ? "%-15s" : "%-20s";
                    return String.format("%-15s now%s evolves into " + formatLength + " at minimum level %s",
                            fromName, additional ? " also" : "", toName, extraInfo);
                } else {
                    return String.format("%-15s -> %-15s at level %s", fromName, toName, extraInfo);
                }
            case STONE:
                return String.format("%-15s -> %-15s using a %s", fromName, toName, extraInfo);
            case HAPPINESS:
                return String.format("%-15s -> %-15s by reaching high happiness", fromName, toName);
            case LEVEL_ITEM_DAY:
                return String.format("%-15s -> %-15s by leveling up holding %s", fromName, toName, extraInfo);
            case LEVEL_WITH_OTHER:
                return String.format("%-15s -> %-15s by leveling up with %s in the party", fromName, toName, extraInfo);
             default:
                return "";
        }
    }
}

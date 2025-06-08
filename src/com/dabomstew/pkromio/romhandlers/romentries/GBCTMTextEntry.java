package com.dabomstew.pkromio.romhandlers.romentries;

public class GBCTMTextEntry {

    private final int number, offset;
    private final String template;

    public GBCTMTextEntry(int number, int offset, String template) {
        this.number = number;
        this.offset = offset;
        this.template = template;
    }

    public int getNumber() {
        return number;
    }

    public int getOffset() {
        return offset;
    }

    public String getTemplate() {
        return template;
    }

}

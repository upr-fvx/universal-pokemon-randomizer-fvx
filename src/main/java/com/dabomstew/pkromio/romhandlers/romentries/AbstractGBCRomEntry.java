package com.dabomstew.pkromio.romhandlers.romentries;

import com.dabomstew.pkromio.romhandlers.AbstractGBCRomHandler;

import java.util.*;

/**
 * An abstract {@link RomEntry} to be used by GBC (Gen 1 and 2) games. Corresponds to {@link AbstractGBCRomHandler}.
 */
public abstract class AbstractGBCRomEntry extends AbstractGBRomEntry {

    protected abstract static class GBCRomEntryReader<T extends AbstractGBCRomEntry> extends GBRomEntryReader<T> {

        protected GBCRomEntryReader() {
            super();
            putSpecialKeyMethod("NonJapanese", AbstractGBCRomEntry::setNonJapanese);
            putSpecialKeyMethod("ExtraTableFile", AbstractGBCRomEntry::setExtraTableFile);
            putSpecialKeyMethod("CRCInHeader", AbstractGBCRomEntry::setCRCInHeader);
            putSpecialKeyMethod("TMText[]", AbstractGBCRomEntry::addTMText);
            putKeyPrefixMethod("BankEndFreeSpaceMargin<", AbstractGBCRomEntry::addBankEndFreeSpaceMargin);
        }
    }

    private int nonJapanese;
    private String extraTableFile;
    private int crcInHeader = -1;
    private final List<GBCTMTextEntry> tmTexts = new ArrayList<>();
    private final Map<Integer, Integer> bankEndFreeSpaceMargins = new HashMap<>();

    public AbstractGBCRomEntry(String name) {
        super(name);
    }

    public AbstractGBCRomEntry(AbstractGBCRomEntry original) {
        super(original);
        this.nonJapanese = original.nonJapanese;
        this.extraTableFile = original.extraTableFile;
        this.crcInHeader = original.getCRCInHeader();
        this.tmTexts.addAll(original.tmTexts);
        this.bankEndFreeSpaceMargins.putAll(original.bankEndFreeSpaceMargins);
    }

    public int getNonJapanese() {
        return nonJapanese;
    }

    public boolean isNonJapanese() {
        return nonJapanese > 0;
    }

    private void setNonJapanese(String unparsed) {
        this.nonJapanese = IniEntryReader.parseInt(unparsed);
    }

    public String getExtraTableFile() {
        return extraTableFile;
    }

    private void setExtraTableFile(String extraTableFile) {
        this.extraTableFile = extraTableFile;
    }

    public int getCRCInHeader() {
        return crcInHeader;
    }

    private void setCRCInHeader(String s) {
        this.crcInHeader = IniEntryReader.parseInt(s);
    }

    private void addTMText(String s)  {
        if (s.startsWith("[") && s.endsWith("]")) {
            String[] parts = s.substring(1, s.length() - 1).split(",", 3);
            int number = IniEntryReader.parseInt(parts[0]);
            int offset = IniEntryReader.parseInt(parts[1]);
            String template = parts[2];
            GBCTMTextEntry tte = new GBCTMTextEntry(number, offset, template);
            tmTexts.add(tte);
        }
    }

    public List<GBCTMTextEntry> getTMTexts() {
        return Collections.unmodifiableList(tmTexts);
    }

    public Map<Integer, Integer> getBankEndFreeSpaceMargins() {
        return Collections.unmodifiableMap(bankEndFreeSpaceMargins);
    }

    private void addBankEndFreeSpaceMargin(String[] valuePair) {
        String keyString = valuePair[0].split("<")[1].split(">")[0];
        int key = IniEntryReader.parseInt(keyString);
        int value = IniEntryReader.parseInt(valuePair[1]);
        bankEndFreeSpaceMargins.put(key, value);
    }

    @Override
    public void copyFrom(IniEntry other) {
        super.copyFrom(other);
        if (other instanceof AbstractGBCRomEntry) {
            AbstractGBCRomEntry gbcOther = (AbstractGBCRomEntry) other;
            extraTableFile = gbcOther.extraTableFile;
            if (getIntValue("CopyTMText") == 1) {
                tmTexts.addAll(gbcOther.tmTexts);
            }
            bankEndFreeSpaceMargins.putAll(gbcOther.bankEndFreeSpaceMargins);
        }
    }

}

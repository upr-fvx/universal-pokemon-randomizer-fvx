package com.dabomstew.pkromio.constants;

import com.dabomstew.pkromio.gamedata.EncounterArea;
import com.dabomstew.pkromio.gamedata.EncounterType;

import java.util.*;

public abstract class EncounterAreaTagger {

    private static class TagPack {
        public final List<String> locationTags;
        public List<EncounterType> encounterTypes;
        public int[] postGameAreas = new int[0];
        public int[] partialPostGameAreas = new int[0];
        public int[] partialPostGameCutoffs = new int[0];

        public TagPack(List<String> locationTags) {
            this.locationTags = locationTags;
        }
    }

    protected static class TagPacks {
        private final Map<Boolean, Map<Integer, TagPack>> map;

        private TagPacks(Map<Boolean, Map<Integer, TagPack>> map) {
            this.map = map;
        }

        private TagPack get(int romType, boolean useTimeOfDay) {
            return map.get(useTimeOfDay).get(romType);
        }
    }

    protected static class Factory {
        private final Map<Boolean, Map<Integer, TagPack>> batch;
        private final Set<Integer> romTypes = new HashSet<>();

        private boolean started;
        private int currROMType;
        private TagPack currNoTOD;
        private TagPack currTOD;

        public Factory() {
            batch = new HashMap<>();
            batch.put(false, new HashMap<>());
            batch.put(true, new HashMap<>());
        }

        public Factory newPack(int romType, List<String> locationTags) {
            return newPack(romType, locationTags, locationTags);
        }

        public Factory newPack(int romType, List<String> locationTagsNoTOD, List<String> locationTagsTOD) {
            if (started) {
                finishPack();
            }
            if (romTypes.contains(romType)) {
                throw new IllegalStateException("Can't add duplicate pack for romType " + romType + "!");
            }
            romTypes.add(romType);
            currROMType = romType;
            currNoTOD = new TagPack(locationTagsNoTOD);
            currTOD = new TagPack(locationTagsTOD);
            return this;
        }

        private void finishPack() {
            batch.get(false).put(currROMType, currNoTOD);
            batch.get(false).put(currROMType, currTOD);
        }

        public TagPacks build() {
            if (!started) {
                throw new IllegalStateException("Can't build; no packs have been added.");
            }
            finishPack();
            return new TagPacks(batch);
        }
    }

    public void tagEncounterAreas(List<EncounterArea> encounterAreas, int romType, boolean useTimeOfDay) {
        TagPack tagPack = getTagPacks().get(romType, useTimeOfDay);
        if (tagPack == null) {
            throw new IllegalStateException("Unexpected value for romType: " + romType);
        }
        tagEncounterAreas(encounterAreas, tagPack);
    }

    protected abstract TagPacks getTagPacks();

    private static void tagEncounterAreas(List<EncounterArea> encounterAreas, TagPack tagPack) {
        // TODO: some of these should not be used always
        if (encounterAreas.size() != tagPack.locationTags.size()) {
            throw new IllegalArgumentException("Unexpected amount of encounter areas");
        }
        for (int i = 0; i < encounterAreas.size(); i++) {
            encounterAreas.get(i).setLocationTag(tagPack.locationTags.get(i));
        }
        for (int areaIndex : tagPack.postGameAreas) {
            encounterAreas.get(areaIndex).setPostGame(true);
        }
        for (int i = 0; i < tagPack.partialPostGameAreas.length; i++) {
            int areaIndex = tagPack.partialPostGameAreas[i];
            int cutoff = tagPack.partialPostGameCutoffs[i];
            encounterAreas.get(areaIndex).setPartiallyPostGameCutoff(cutoff);
        }
    }

}

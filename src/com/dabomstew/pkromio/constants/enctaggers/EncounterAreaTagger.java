package com.dabomstew.pkromio.constants.enctaggers;

import com.dabomstew.pkromio.gamedata.EncounterArea;
import com.dabomstew.pkromio.gamedata.EncounterType;

import java.util.*;

public abstract class EncounterAreaTagger {

    private static class TagPack {
        private final List<String> locationTags;
        private List<EncounterType> encounterTypes;
        private int[] postGameAreas = new int[0];
        private int[] partialPostGameAreas = new int[0];
        private int[] partialPostGameCutoffs = new int[0];

        public TagPack(List<String> locationTags) {
            this.locationTags = locationTags;
        }

        public void setEncounterTypes(List<EncounterType> encounterTypes) {
            if (encounterTypes.size() != locationTags.size()) {
                throw new IllegalArgumentException("Location and encounter type lists do not match! (" +
                        locationTags.size() + " vs " + encounterTypes.size() + ")");
            }
            this.encounterTypes = encounterTypes;
        }

        public void setPostGameAreas(int[] postGameAreas) {
            for (int pgaIndex : postGameAreas) {
                if (pgaIndex >= locationTags.size()) {
                    throw new IllegalArgumentException("PostGameArea index out of bounds: " + pgaIndex
                            + ". Must be less than the number of Encounter Areas=" + locationTags.size() + ".");
                }
            }
            this.postGameAreas = postGameAreas;
        }

        public void setPartialPostGameAreas(int[] partialPostGameAreas) {
            for (int ppgaIndex : partialPostGameAreas) {
                if (ppgaIndex >= locationTags.size()) {
                    throw new IllegalArgumentException("PartialPostGameArea index out of bounds: " + ppgaIndex
                            + ". Must be less than the number of Encounter Areas=" + locationTags.size() + ".");
                }
            }
            this.partialPostGameAreas = partialPostGameAreas;
        }

        public void setPartialPostGameCutoffs(int[] partialPostGameCutoffs) {
            if (partialPostGameCutoffs.length != partialPostGameAreas.length) {
                throw new IllegalArgumentException("Partial post-game areas and cutoff lengths do not match! (" +
                        partialPostGameAreas.length + " vs " + partialPostGameCutoffs.length + ")");
            }
            this.partialPostGameCutoffs = partialPostGameAreas;
        }
    }

    protected static class TagPackMap {
        // Mostly just an alias for the looong type below
        private final Map<Boolean, Map<Integer, TagPack>> map;

        private TagPackMap(Map<Boolean, Map<Integer, TagPack>> map) {
            this.map = map;
        }

        private TagPack get(int romType, boolean useTimeOfDay) {
            return map.get(useTimeOfDay).get(romType);
        }
    }

    /**
     * Builds a {@link TagPackMap}.
     */
    protected static class Builder {
        private final Map<Boolean, Map<Integer, TagPack>> batch;
        private final Set<Integer> romTypes = new HashSet<>();

        private boolean started;
        private int currROMType;
        private TagPack currNoTOD;
        private TagPack currTOD;

        public Builder() {
            batch = new HashMap<>();
            batch.put(false, new HashMap<>());
            batch.put(true, new HashMap<>());
        }

        public Builder newPack(int romType, List<String> locationTags) {
            return newPack(romType, locationTags, locationTags);
        }

        public Builder newPack(int romType, List<String> locationTagsNoTOD, List<String> locationTagsTOD) {
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
            started = true;
            return this;
        }

        private void finishPack() {
            batch.get(false).put(currROMType, currNoTOD);
            batch.get(true).put(currROMType, currTOD);
        }

        public Builder encounterTypes(List<EncounterType> encounterTypes) {
            return encounterTypes(encounterTypes, encounterTypes);
        }

        public Builder encounterTypes(List<EncounterType> encounterTypesNoTOD, List<EncounterType> encounterTypesTOD) {
            currNoTOD.setEncounterTypes(encounterTypesNoTOD);
            currTOD.setEncounterTypes(encounterTypesTOD);
            return this;
        }

        public Builder postGameAreas(int[] postGameAreas) {
            return postGameAreas(postGameAreas, postGameAreas);
        }

        public Builder postGameAreas(int[] postGameAreasNoTOD, int[] postGameAreasTOD) {
            currNoTOD.setPostGameAreas(postGameAreasNoTOD);
            currTOD.setPostGameAreas(postGameAreasTOD);
            return this;
        }

        public Builder partialPostGameAreas(int[] partialPostGameAreas) {
            return partialPostGameAreas(partialPostGameAreas, partialPostGameAreas);
        }

        public Builder partialPostGameAreas(int[] partialPostGameAreasNoTOD, int[] partialPostGameAreasTOD) {
            currNoTOD.setPartialPostGameAreas(partialPostGameAreasNoTOD);
            currTOD.setPartialPostGameAreas(partialPostGameAreasTOD);
            return this;
        }

        public Builder partialPostGameCutoffs(int[] partialPostGameCutoffs) {
            return partialPostGameCutoffs(partialPostGameCutoffs, partialPostGameCutoffs);
        }

        public Builder partialPostGameCutoffs(int[] partialPostGameCutoffsNoTOD, int[] partialPostGameCutoffsTOD) {
            currNoTOD.setPartialPostGameCutoffs(partialPostGameCutoffsNoTOD);
            currTOD.setPartialPostGameCutoffs(partialPostGameCutoffsTOD);
            return this;
        }

        public TagPackMap build() {
            if (!started) {
                throw new IllegalStateException("Can't build; no packs have been added.");
            }
            finishPack();
            return new TagPackMap(batch);
        }
    }

    public void tag(List<EncounterArea> encounterAreas, int romType, boolean useTimeOfDay) {
        TagPack tagPack = getTagPacks().get(romType, useTimeOfDay);
        if (tagPack == null) {
            throw new IllegalStateException("Unexpected value for romType: " + romType);
        }
        tag(encounterAreas, tagPack);
    }

    protected abstract TagPackMap getTagPacks();

    private void tag(List<EncounterArea> encounterAreas, TagPack tagPack) {
        if (encounterAreas.size() != tagPack.locationTags.size()) {
            throw new IllegalArgumentException("Unexpected amount of encounter areas. Expected: "
                    + tagPack.locationTags.size() + ", was: " + encounterAreas.size());
        }
        for (int i = 0; i < encounterAreas.size(); i++) {
            encounterAreas.get(i).setLocationTag(tagPack.locationTags.get(i));
        }
        if (tagPack.encounterTypes != null) {
            for (int i = 0; i < encounterAreas.size(); i++) {
                encounterAreas.get(i).setEncounterType(tagPack.encounterTypes.get(i));
            }
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

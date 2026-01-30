package com.dabomstew.pkrandom;

/*----------------------------------------------------------------------------*/
/*--  BatchRandomizationSettings.java - handles functionality related to    --*/
/*--                                    batch randomization.                --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkromio.RootPath;

import java.util.StringJoiner;

public class BatchRandomizationSettings implements Cloneable {
    private Boolean batchRandomizationEnabled;
    private Boolean generateLogFile;
    private Boolean autoAdvanceStartingIndex;
    private Integer numberOfRandomizedROMs;
    private Integer startingIndex;
    private String fileNamePrefix;
    private String outputDirectory;

    public BatchRandomizationSettings() {
        batchRandomizationEnabled = false;
        generateLogFile = false;
        autoAdvanceStartingIndex = true;
        numberOfRandomizedROMs = 10;
        startingIndex = 0;
        fileNamePrefix = "random";
        outputDirectory = RootPath.path;
    }

    public boolean isBatchRandomizationEnabled() {
        return batchRandomizationEnabled;
    }

    public void setBatchRandomizationEnabled(boolean batchRandomizationEnabled) {
        this.batchRandomizationEnabled = batchRandomizationEnabled;
    }

    public boolean shouldGenerateLogFile() {
        return generateLogFile;
    }

    public void setGenerateLogFile(boolean generateLogFile) {
        this.generateLogFile = generateLogFile;
    }

    public boolean shouldAutoAdvanceStartingIndex() {
        return autoAdvanceStartingIndex;
    }

    public void setAutoAdvanceStartingIndex(boolean autoAdvanceStartingIndex) {
        this.autoAdvanceStartingIndex = autoAdvanceStartingIndex;
    }

    public int getNumberOfRandomizedROMs() {
        return numberOfRandomizedROMs;
    }

    public void setNumberOfRandomizedROMs(int numberOfRandomizedROMs) {
        this.numberOfRandomizedROMs = numberOfRandomizedROMs;
    }

    public int getStartingIndex() {
        return startingIndex;
    }

    public void setStartingIndex(int startingIndex) {
        this.startingIndex = startingIndex;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(System.lineSeparator());
        sj.add("batchrandomization.enabled=" + batchRandomizationEnabled.toString());
        sj.add("batchrandomization.generatelogfiles=" + generateLogFile.toString());
        sj.add("batchrandomization.autoadvanceindex=" + autoAdvanceStartingIndex.toString());
        sj.add("batchrandomization.numberofrandomizedroms=" + numberOfRandomizedROMs.toString());
        sj.add("batchrandomization.startingindex=" + startingIndex.toString());
        sj.add("batchrandomization.filenameprefix=" + fileNamePrefix);
        sj.add("batchrandomization.outputdirectory=" + outputDirectory);
        return sj.toString();
    }

    @Override
    public BatchRandomizationSettings clone() {
        try {
            return (BatchRandomizationSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

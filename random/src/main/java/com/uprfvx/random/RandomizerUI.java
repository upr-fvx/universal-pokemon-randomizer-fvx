package com.uprfvx.random;

public interface RandomizerUI {

    void displayRandomizationStarted();

    void displayRandomizationProgress(int current, int batchSize);

    void displayRandomizationEnded();

    RandomizationCoordinator.LogPresetPromptResults promptSaveLogOrPresetFiles(boolean createRaceMode);

    void displayRandomizationCoordinationDone(RandomizeMode mode,
                                              int batchNumber, boolean batchSaveLogs,
                                              boolean racePreset, boolean racePresetLogExists);

    void displayLoggingFailure(Exception logException, long seed);

    void displayLogFileSaveFailure();

    void displayPresetFileSaveFailure();

    void unloadRomHandler();

    void initialState();

    void reinitializeRomHandler(boolean batchMode);
}

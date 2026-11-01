package com.uprfvx.random;

public interface RandomizerUI {

    /**
     * Displays somehow that the randomization has started.<br>
     * Can also be used for setting up what later display and prompt methods need,
     * since it is guaranteed to always run before them.
     */
    void displayRandomizationStarted();

    void displayRandomizationProgress(int currentBatch, int batchSize);

    void displayRandomizationEnded();

    RandomizationCoordinator.LogPresetPromptResults promptSaveLogOrPresetFiles(boolean createRaceMode);

    void displayRandomizationCoordinationDone(RandomizeMode mode,
                                              int batchNumber, boolean batchSaveLogs,
                                              boolean racePreset, boolean racePresetLogExists);

    void displayRandomizationFailure(Exception exception, long seed);

    void displayBatchLoggingFailure(Exception logException, long seed, int currentBatch, int batchSize);

    void displayBatchLogFileSaveFailure(int currentBatch, int batchSize);

    void displayLoggingFailure(Exception logException, long seed);

    void displayLogFileSaveFailure();

    void displayPresetFileSaveFailure();

    void unloadRomHandler();

    void initialState();

    void reinitializeRomHandler(boolean batchMode);
}

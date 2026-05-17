package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Trainer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Gen3TrainerTextDisplayNameSyncTest {

    @Test
    void setTrainerNamesRefreshesLoadedFullDisplayNamesForLogger() {
        TestableGen3RomHandler romHandler = new TestableGen3RomHandler();
        Trainer trainer = trainer(1, "Quinn", "BURGLAR Quinn");
        romHandler.addTrainer(trainer);

        romHandler.setTrainerNames(List.of("Alex"));

        assertEquals("Alex", trainer.getName());
        assertEquals("BURGLAR Alex", trainer.getFullDisplayName());
    }

    @Test
    void classNameRefreshUpdatesLoadedFullDisplayNamesForLogger() {
        TestableGen3RomHandler romHandler = new TestableGen3RomHandler();
        Trainer trainer = trainer(1, "Quinn", "BURGLAR Quinn");
        romHandler.addTrainer(trainer);

        romHandler.refreshTrainerFullDisplayNames(List.of("UNUSED", "ENGINEER"));

        assertEquals("Quinn", trainer.getName());
        assertEquals("ENGINEER Quinn", trainer.getFullDisplayName());
    }

    private static Trainer trainer(int trainerClass, String name, String fullDisplayName) {
        Trainer trainer = new Trainer();
        trainer.setTrainerclass(trainerClass);
        trainer.setName(name);
        trainer.setFullDisplayName(fullDisplayName);
        return trainer;
    }

    private static class TestableGen3RomHandler extends Gen3RomHandler {
        @Override
        public List<String> getTrainerClassNames() {
            return List.of("UNUSED", "BURGLAR");
        }

        void addTrainer(Trainer trainer) {
            trainers.add(trainer);
        }
    }
}

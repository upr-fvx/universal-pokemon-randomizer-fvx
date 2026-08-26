package com.uprfvx.random.gui;

/*----------------------------------------------------------------------------*/
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

import com.uprfvx.random.*;
import com.uprfvx.random.cli.CliRandomizer;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.random.customnames.OldCustomNamesImporter;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.random.gui.SettingElementCoordinators.*;
import com.uprfvx.random.random.SeedPicker;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.Settings.*;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.updaters.TypeEffectivenessUpdater;
import com.uprfvx.romio.RootPath;
import com.uprfvx.romio.constants.GlobalConstants;
import com.uprfvx.romio.exceptions.CannotWriteToLocationException;
import com.uprfvx.romio.exceptions.EncryptedROMException;
import com.uprfvx.romio.gamedata.*;
import com.uprfvx.romio.graphics.packs.CustomPlayerGraphics;
import com.uprfvx.romio.romhandlers.*;
import com.uprfvx.romio.romio.ROMFilter;
import com.uprfvx.romio.romio.RomOpener;
import filefunctions.FileNameFunctions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * The main GUI for the Universal Pokemon Randomizer FVX, containing the various options available and such.
 */
public class RandomizerGUI {

    //region JComponent Declarations

    //region *** Header/Main ***

    //Header left
    private JCheckBox raceModeCheckBox;
    private JButton loadSettingsButton;
    private JButton saveSettingsButton;

    //Header right
    private JButton openROMButton;
    private JButton randomizeSaveButton;
    private JButton premadeSeedButton;
    private JButton settingsButton;

    //Main
    private JPanel mainPanel;
    private JTabbedPane randomizationSettingsTabbedPane;

    //endregion

    //region *** General tab ***

    //Cosmetic options
    private JPanel cosmeticPanel;
    private JCheckBox coRandomIntroMonCheckBox;
    private JCheckBox coRandomizeCatchingTutorialCheckBox;
    private JCheckBox coLowerCaseSpeciesNamesCheckBox;
    private JCheckBox coRandomizeTrainerNamesCheckBox;
    private JCheckBox coRandomizeTrainerClassNamesCheckBox;

    //Limit Species
    //--By Generation
    private JCheckBox lsBanGeneration1CheckBox;
    private JCheckBox lsBanGeneration2CheckBox;
    private JCheckBox lsBanGeneration3CheckBox;
    private JCheckBox lsBanGeneration4CheckBox;
    private JCheckBox lsBanGeneration5CheckBox;
    private JCheckBox lsBanGeneration6CheckBox;
    private JCheckBox lsBanGeneration7CheckBox;
    private JCheckBox lsAllowRelativesCheckBox;
    //--By Other Qualities
    private JCheckBox lsNoIrregularAltFormesCheckBox;
    private JCheckBox lsRetainAltFormesCheckBox; //This is a little odd placement, but it seems relevant?
    private JCheckBox lsNoPrematureEvosCheckbox;

    //Quality of Life Tweaks
    private JPanel qolTweaksPanel;
    private JLabel qoltNoneAvailableLabel;
    private JCheckBox qoltFastestTextCheckBox;
    private JCheckBox qoltRunIndoorsCheckBox;
    private JCheckBox qoltNationalDexCheckBox;
    private JCheckBox qoltRunWithoutRunningShoesCheckBox;
    private JCheckBox qoltFasterHPAndEXPBarsCheckBox;
    private JCheckBox qoltFastDistortionWorldCheckBox;
    private JCheckBox qoltDisableLowHPMusicCheckBox;
    private JCheckBox qoltFastEggsCheckBox;
    private JCheckBox qoltReusableTMsCheckBox;
    private JCheckBox qoltForgettableHMsCheckBox;

    //Balance Tweaks
    private JPanel balanceTweaksPanel;
    private JLabel btNoneAvailableLabel;
    private JCheckBox btNerfXAccuracyCheckBox;
    private JCheckBox btUpdateCritRateCheckBox;
    private JCheckBox btScalingEXPCheckBox;
    private JCheckBox btForceChallengeModeCheckBox;
    private JCheckBox btNoEVYieldsCheckBox;

    //endregion

    //region *** Species Traits tab ***

    //Base Stats
    private JCheckBox sbsUpdateBaseStatsCheckBox;
    //--Totals

    //--Distribution
    private JRadioButton sbsdUnchangedRadioButton;
    private JRadioButton sbsdShuffleRadioButton;
    private JRadioButton sbsdRandomRadioButton;
    private JCheckBox sbsdFollowEvolutionsCheckBox;

    //Types
    private JRadioButton stUnchangedRadioButton;
    private JRadioButton stRandomFollowEvolutionsRadioButton;
    private JRadioButton stRandomCompletelyRadioButton;

    private JCheckBox stForceDualTypeCheckBox;
    private JCheckBox stUpdateRotomCheckBox;

    //Abilities
    private JRadioButton saUnchangedRadioButton;
    private JRadioButton saRandomRadioButton;

    private JCheckBox saFollowEvolutionsCheckBox;

    //--Ban
    private JCheckBox saBanWonderGuardCheckBox;
    private JCheckBox saBanTrappingAbilitiesCheckBox;
    private JCheckBox saBanNegativeAbilitiesCheckBox;
    private JCheckBox saBanMinorAbilitiesCheckBox;

    //Evolutions
    private JCheckBox peChangeImpossibleEvosCheckBox;

    private JCheckBox peAllowPikachuEvolutionCheckBox;
    private JCheckBox peUseEstimatedInsteadOfHardcodedLevelsCheckBox;

    private JCheckBox peMakeEvolutionsEasierCheckBox;
    private JSlider peMakeEvolutionsEasierLvlSlider;
    //--Randomize
    private JRadioButton peUnchangedRadioButton;
    private JRadioButton peRandomRadioButton;
    private JCheckBox peSimilarStrengthCheckBox;
    private JCheckBox peSameTypingCheckBox;
    private JCheckBox peLimitEvolutionsToThreeCheckBox;

    private JCheckBox peForceChangeCheckBox;
    private JCheckBox peForceGrowthCheckBox;
    private JCheckBox peNoConvergenceCheckBox;
    private JCheckBox peAdjustLevelsCheckBox;

    //EXP Curves
    private JCheckBox secStandardizeEXPCurvesCheckBox;
    private JRadioButton secLegendariesSlowRadioButton;
    private JRadioButton secStrongLegendariesSlowRadioButton;
    private JRadioButton secAllSpeciesRadioButton;

    //endregion

    private JCheckBox btBanLuckyEggCheckBox;
    private JCheckBox btNoFreeLuckyEggCheckBox;
    private JCheckBox miscBanBigMoneyManiacCheckBox;
    private JCheckBox miscUpdateTypeEffectivenessCheckBox;

    private JPanel generalItemsPanel;
    private JCheckBox miscSOSBattlesCheckBox;
    private JCheckBox miscRandomizePCPotionCheckBox;

    private JRadioButton spUnchangedRadioButton;
    private JRadioButton spCustomRadioButton;
    private JRadioButton spRandomRadioButton;
    private JComboBox<String> spCustom1ComboBox;
    private JComboBox<String> spCustom2ComboBox;
    private JComboBox<String> spCustom3ComboBox;
    private JCheckBox spRandomizeStarterHeldItemsCheckBox;
    private JCheckBox spBanMinorItemsCheckBox;
    private JRadioButton stpUnchangedRadioButton;
    private JRadioButton stpSwapLegendariesSwapStandardsRadioButton;
    private JRadioButton stpRandomCompletelyRadioButton;
    private JRadioButton stpRandomSimilarStrengthRadioButton;
    private JCheckBox stpLimitMainGameLegendariesCheckBox;
    private JCheckBox stpRandomize600BSTCheckBox;
    private JCheckBox igtRandomizeNicknamesCheckBox;
    private JCheckBox igtRandomizeOTsCheckBox;
    private JCheckBox igtRandomizeIVsCheckBox;
    private JCheckBox igtRandomizeItemsCheckBox;
    private JCheckBox mdRandomizeMovePowerCheckBox;
    private JCheckBox mdRandomizeMoveAccuracyCheckBox;
    private JCheckBox mdRandomizeMovePPCheckBox;
    private JCheckBox mdRandomizeMoveTypesCheckBox;
    private JCheckBox mdRandomizeMoveNamesCheckBox;
    private JCheckBox mdRandomizeMoveCategoryCheckBox;
    private JCheckBox mdUpdateMovesCheckBox;
    private JCheckBox mdLegacyCheckBox;
    private JRadioButton pmsUnchangedRadioButton;
    private JRadioButton pmsRandomPreferringSameTypeRadioButton;
    private JRadioButton pmsRandomCompletelyRadioButton;
    private JRadioButton pmsMetronomeOnlyModeRadioButton;
    private JCheckBox pmsGuaranteedLevel1MovesCheckBox;
    private JCheckBox pmsReorderDamagingMovesCheckBox;
    private JCheckBox pmsNoGameBreakingMovesCheckBox;
    private JCheckBox pmsForceGoodDamagingCheckBox;
    private JSlider pmsGuaranteedLevel1MovesSlider;
    private SpinSlider pmsForceGoodDamagingSpinSlider;
    private JCheckBox tpRivalCarriesStarterCheckBox;
    private JCheckBox tpSimilarStrengthCheckBox;
    private JCheckBox tpAvoidDuplicatesCheckBox;
    private JCheckBox tpWeightTypesCheckBox;
    private JCheckBox tpDontUseLegendariesCheckBox;
    private JCheckBox tpNoEarlyWonderGuardCheckBox;
    private JCheckBox tpTrainersEvolveTheirPokemonCheckbox;
    private SpinSlider tpPercentageEvolutionLevelModifierSpinSlider;
    private SpinSlider tpPercentageLevelModifierSpinSlider;
    private JLabel tpCalculatedFullyEvolvedLvlLabel;
    private JCheckBox tpEliteFourUniquePokemonCheckBox;
    private JSpinner tpEliteFourUniquePokemonSpinner;
    private JCheckBox tpPercentageLevelModifierCheckBox;
    private JRadioButton wpZoneNoneRadioButton;
    private JRadioButton wpZoneEncounterSetRadioButton;
    private JRadioButton wpZoneNamedLocationRadioButton;
    private JRadioButton wpZoneGameRadioButton;
    private JCheckBox wpSimilarStrengthCheckBox;
    private JCheckBox wpCatchEmAllModeCheckBox;
    private JRadioButton wpTRNoneRadioButton;
    private JRadioButton wpTRThemedAreasRadioButton;
    private JRadioButton wpTRKeepPrimaryRadioButton;
    private JCheckBox wpRemoveTimeBasedEncountersCheckBox;
    private JCheckBox wpDontUseLegendariesCheckBox;
    private JCheckBox wpSetMinimumCatchRateCheckBox;
    private JCheckBox wpRandomizeHeldItemsCheckBox;
    private JCheckBox wpBanBadItemsCheckBox;
    private JCheckBox wpBalanceShakingGrassPokemonCheckBox;
    private JCheckBox wpPercentageLevelModifierCheckBox;
    private SpinSlider wpPercentageLevelModifierSpinSlider;
    private JSlider wpSetMinimumCatchRateSlider;
    private JRadioButton tmmUnchangedRadioButton;
    private JRadioButton tmmRandomRadioButton;
    private JCheckBox thcFullHMCompatibilityCheckBox;
    private JCheckBox thcLevelupMoveSanityCheckBox;
    private JCheckBox tmmKeepFieldMoveTMsCheckBox;
    private JCheckBox tmmForceGoodDamagingCheckBox;
    private SpinSlider tmmForceGoodDamagingSpinSlider;
    private JRadioButton thcUnchangedRadioButton;
    private JRadioButton thcRandomPreferSameTypeRadioButton;
    private JRadioButton thcRandomCompletelyRadioButton;
    private JRadioButton thcFullCompatibilityRadioButton;
    private JRadioButton mtmUnchangedRadioButton;
    private JRadioButton mtmRandomRadioButton;
    private JCheckBox mtcLevelupMoveSanityCheckBox;
    private JCheckBox mtmKeepFieldMoveTutorsCheckBox;
    private JCheckBox mtmForceGoodDamagingCheckBox;
    private SpinSlider mtmForceGoodDamagingSpinSlider;
    private JRadioButton mtcUnchangedRadioButton;
    private JRadioButton mtcRandomPreferSameTypeRadioButton;
    private JRadioButton mtcRandomCompletelyRadioButton;
    private JRadioButton mtcFullCompatibilityRadioButton;
    private JRadioButton fiUnchangedRadioButton;
    private JRadioButton fiShuffleRadioButton;
    private JRadioButton fiRandomRadioButton;
    private JRadioButton fiRandomEvenDistributionRadioButton;
    private JCheckBox fiBanBadItemsCheckBox;
    private JRadioButton shUnchangedRadioButton;
    private JRadioButton shShuffleRadioButton;
    private JRadioButton shRandomRadioButton;
    private JCheckBox shBanOverpoweredShopItemsCheckBox;
    private JCheckBox shBanBadItemsCheckBox;
    private JCheckBox shBanRegularShopItemsCheckBox;
    private JCheckBox shBalanceShopItemPricesCheckBox;
    private JCheckBox shGuaranteeEvolutionItemsCheckBox;
    private JCheckBox shGuaranteeXItemsCheckBox;
    private JPanel speciesAbilitiesPanel;
    private JPanel moveTutorPanel;
    private JPanel mtMovesPanel;
    private JPanel mtCompatPanel;
    private JLabel mtNoExistLabel;
    private JPanel shopItemsPanel;

    private JLabel gameMascotLabel;
    private JPanel baseTweaksPanel;
    private JLabel romNameLabel;
    private JLabel romCodeLabel;
    private JLabel romSupportLabel;
    private JLabel websiteLinkLabel;
    private JCheckBox tmmNoGameBreakingMovesCheckBox;
    private JCheckBox mtmNoGameBreakingMovesCheckBox;
    private JCheckBox tpAllowAlternateFormesCheckBox;
    private JLabel versionLabel;
    private JCheckBox sbsdFollowMegaEvosCheckBox;
    private JCheckBox saFollowMegaEvosCheckBox;
    private JCheckBox stFollowMegaEvosCheckBox;
    private JCheckBox spAllowAltFormesCheckBox;
    private JCheckBox stpAllowAltFormesCheckBox;
    private JCheckBox stpSwapMegaEvosCheckBox;
    private JCheckBox tpSwapMegaEvosCheckBox;
    private JCheckBox wpAllowAltFormesCheckBox;
    private JPanel tpBattleStylePanel;
    private JRadioButton tbsUnchangedStyleRadioButton;
    private JRadioButton tbsRandomStyleRadioButton;
    private JCheckBox tpBossTrainersCheckBox;
    private JCheckBox tpImportantTrainersCheckBox;
    private JCheckBox tpRegularTrainersCheckBox;
    private JSpinner tpBossTrainersSpinner;
    private JSpinner tpImportantTrainersSpinner;
    private JSpinner tpRegularTrainersSpinner;
    private JLabel tpAdditionalPokemonForLabel;
    private JCheckBox peAllowAltFormesCheckBox;
    private JCheckBox tpRandomShinyTrainerPokemonCheckBox;
    private JRadioButton totpUnchangedRadioButton;
    private JRadioButton totpRandomRadioButton;
    private JRadioButton totpRandomSimilarStrengthRadioButton;
    private JRadioButton totpAllyUnchangedRadioButton;
    private JRadioButton totpAllyRandomRadioButton;
    private JRadioButton totpAllyRandomSimilarStrengthRadioButton;
    private JPanel totpAllyPanel;
    private JPanel totpAuraPanel;
    private JRadioButton totpAuraUnchangedRadioButton;
    private JRadioButton totpAuraRandomRadioButton;
    private JRadioButton totpAuraRandomSameStrengthRadioButton;
    private JCheckBox totpPercentageLevelModifierCheckBox;
    private SpinSlider totpPercentageLevelModifierSpinSlider;
    private JCheckBox totpRandomizeHeldItemsCheckBox;
    private JCheckBox totpAllowAltFormesCheckBox;
    private JPanel totpPanel;
    private JCheckBox pmsEvolutionMovesCheckBox;
    private JComboBox<String> mdUpdateComboBox;
    private JLabel wikiLinkLabel;
    private JCheckBox saWeighDuplicatesTogetherCheckBox;
    private JComboBox<String> secEXPCurveComboBox;
    private JCheckBox peRemoveTimeBasedEvolutionsCheckBox;
    private JCheckBox thcFollowEvolutionsCheckBox;
    private JCheckBox mtcFollowEvolutionsCheckBox;
    private JCheckBox stpPercentageLevelModifierCheckBox;
    private SpinSlider stpPercentageLevelModifierSpinSlider;
    private JCheckBox stpFixMusicCheckBox;
    private JCheckBox tpBossTrainersItemsCheckBox;
    private JCheckBox tpImportantTrainersItemsCheckBox;
    private JCheckBox tpRegularTrainersItemsCheckBox;
    private JLabel tpHeldItemsLabel;
    private JCheckBox tpConsumableItemsOnlyCheckBox;
    private JCheckBox tpSensibleItemsCheckBox;
    private JCheckBox tpHighestLevelGetsItemCheckBox;
    private JPanel pickupItemsPanel;
    private JRadioButton puUnchangedRadioButton;
    private JRadioButton puRandomRadioButton;
    private JCheckBox puBanBadItemsCheckBox;
    private JCheckBox sbsdAssignEvoStatsRandomlyCheckBox;
    private JRadioButton peRandomEveryLevelRadioButton;
    private JCheckBox saForceTwoAbilitiesCheckbox;
    private JRadioButton ppalUnchangedRadioButton;
    private JRadioButton ppalRandomRadioButton;
    private JCheckBox ppalFollowTypesCheckBox;
    private JCheckBox ppalFollowEvolutionsCheckBox;
    private JCheckBox ppalShinyFromNormalCheckBox;
    private JPanel graphicsPanel;
    private JLabel ppalNotExistLabel;
    private JLabel ppalPartiallyImplementedLabel;
    private JLabel cpgNotExistLabel;
    private JRadioButton cpgUnchangedRadioButton;
    private JRadioButton cpgCustomRadioButton;
    private CPGSelection cpgSelection;
    private JCheckBox tpUseLocalPokemonCheckBox;
    private JRadioButton spTypeTriangleRadioButton;
    private JRadioButton spTypeNoneRadioButton;
    private JRadioButton spTypeFwgRadioButton;
    private JRadioButton spTypeSingleRadioButton;
    private JComboBox<String> spTypeSingleComboBox;
    private JCheckBox spTypeNoDualCheckbox;
    private JRadioButton spTypeUniqueRadioButton;
    private JCheckBox spNoLegendariesCheckBox;
    private JCheckBox wpTRKeepThemesCheckBox;
    private JPanel typesPanel;
    private JRadioButton teUnchangedRadioButton;
    private JRadioButton teRandomRadioButton;
    private JRadioButton teRandomBalancedRadioButton;
    private JRadioButton teKeepTypeIdentitiesRadioButton;
    private JRadioButton teInverseRadioButton;
    private JCheckBox teAddRandomImmunitiesCheckBox;
    private JCheckBox teUpdateCheckbox;
    private JCheckBox spBSTMinimumCheckbox;
    private JCheckBox spBSTMaximumCheckbox;
    private JSpinner spBSTMinimumSpinner;
    private JSpinner spBSTMaximumSpinner;
    private JRadioButton wpZoneMapRadioButton;
    private JCheckBox wpSplitByEncounterTypesCheckBox;
    private JCheckBox wpERKeepEvolutionsCheckBox;
    private JCheckBox wpRandomizeWildPokemonCheckBox;
    private JRadioButton wpERNoneRadioButton;
    private JRadioButton wpERBasicOnlyRadioButton;
    private JRadioButton wpERSameEvolutionStageRadioButton;
    private JCheckBox tpBossTrainersTypeDiversityCheckBox;
    private JCheckBox tpImportantTrainersTypeDiversityCheckBox;
    private JCheckBox tpRegularTrainersTypeDiversityCheckBox;
    private JPanel specialShopsPanel;
    private JCheckBox shAddRareCandyCheckBox;
    private JLabel tpBetterMovesetsLabel;
    private JCheckBox tpBetterMovesetsBossTrainersCheckBox;
    private JCheckBox tpBetterMovesetsImportantTrainersCheckBox;
    private JCheckBox tpBetterMovesetsRegularTrainersCheckBox;
    private JRadioButton sbstUnchangedRadioButton;
    private JRadioButton sbstRandomBuffNerfRadioButton;
    private JRadioButton sbstShuffleRadioButton;
    private JRadioButton sbstRandomRadioButton;
    private JCheckBox sbstFollowEvolutionsCheckBox;
    private JCheckBox sbstSwapLegendariesCheckBox;
    private SpinSlider sbstRandomBuffNerfSpinSlider;
    private JSpinner sbsUpdateGenerationChoiceSpinner;
    private JPanel speciesBanAbilitiesPanel;
    private JCheckBox spBasicOnlyCheckBox;
    private JCheckBox spHasEvolutionsCheckBox;
    private JLabel spBSTLimitsLabel;
    private JCheckBox igtRandomizeGivenSpeciesCheckBox;
    private JCheckBox igtRandomizeRequestedSpeciesCheckBox;
    private JSlider spHasEvolutionCountSlider;
    private JCheckBox tpRandomizeTrainerPokemonCheckBox;
    private JRadioButton tpTypesUnrestrictedRadioButton;
    private JRadioButton tpRandomTypeThemesRadioButton;
    private JRadioButton tpKeepTypeThemesRadioButton;
    private JCheckBox tpTypeGymsAndElitesOnlyCheckBox;
    private JRadioButton tpKeepThemesOrPrimaryRadioButton;
    private JLabel tpTypeDiversityLabel;
    private JCheckBox tpDistributeSpeciesCheckBox;
    private JCheckBox tpDistributeInMainGameOnly;
    private JCheckBox tbsExcludeSingleBattlesCheckBox;
    private JCheckBox tbsExcludeDoubleBattlesCheckBox;
    private JCheckBox tbsExcludeTripleBattlesCheckBox;
    private JCheckBox tbsExtendTeamsCheckBox;
    private JCheckBox tbsExcludeRotationBattlesCheckBox;
    private JCheckBox miscBalanceStaticLevelsCheckBox;

    //endregion

    private static final Random RND = new Random();

    private static JFrame frame;

    private static String launcherInput = "";
    public static boolean usedLauncher = false;

    private GenRestrictions currentRestrictions;
    private OperationDialog opDialog;

    private final ResourceBundle bundle;
    protected RomHandler.Factory[] checkHandlers;
    private RomHandler romHandler;

    private Theme theme = Theme.DEFAULT;
    private boolean presetMode = false;
    private boolean initialPopup = true;
    private boolean showInvalidRomPopup = true;
    private String openDirectory = RootPath.path;
    private String saveDirectory = RootPath.path;
    private final Map<String, String> lastUsedCPGConfigs = new TreeMap<>();

    private final RomOpener romOpener = new RomOpener();

    private final JFileChooser romOpenChooser = new JFileChooser();
    private final JFileChooser romSaveChooser = new JFileChooser();
    private final JFileChooser qsOpenChooser = new JFileChooser();
    private final JFileChooser qsSaveChooser = new JFileChooser();
    private final JFileChooser qsUpdateChooser = new JFileChooser();
    private final JFileChooser gameUpdateChooser = new JFileChooser();

    private JPopupMenu settingsMenu;
    private JMenuItem themeSelectionMenuItem;
    private JMenuItem customNamesEditorMenuItem;
    private JMenuItem applyGameUpdateMenuItem;
    private JMenuItem removeGameUpdateMenuItem;
    private JMenuItem loadGetSettingsMenuItem;
    private JMenuItem keepOrUnloadGameAfterRandomizingMenuItem;
    private JMenuItem batchRandomizationMenuItem;

    private final ImageIcon emptyIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/uprfvx/random/gui/emptyIcon.png")));
    private boolean haveCheckedCustomNames, hasVisitedCustomNamesEditor;
    private boolean unloadGameOnSuccess;
    private final Map<String, String> gameUpdates = new TreeMap<>();

    private final List<String> trainerSettings = new ArrayList<>();
    private final List<String> trainerSettingToolTips = new ArrayList<>();
    private final int TRAINER_UNCHANGED = 0, TRAINER_RANDOM = 1, TRAINER_RANDOM_EVEN = 2, TRAINER_RANDOM_EVEN_MAIN = 3,
                        TRAINER_TYPE_THEMED = 4, TRAINER_TYPE_THEMED_ELITE4_GYMS = 5, TRAINER_KEEP_THEMED = 6,
                        TRAINER_KEEP_THEME_OR_PRIMARY = 7;

    private final List<String> selectableBattleStyles = new ArrayList<>();
    private final List<String> selectableBattleStylesTooltips = new ArrayList<>();
    private final int SINGLE_BATTLE = 0, DOUBLE_BATTLE = 1, TRIPLE_BATTLE = 2, ROTATION_BATTLE = 3;

    private BatchRandomizationSettings batchRandomizationSettings;

    private SettingsManager settingsManager;

    public RandomizerGUI() {
        ToolTipManager.sharedInstance().setInitialDelay(400);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        bundle = ResourceBundle.getBundle("com/uprfvx/random/gui/Bundle");
        checkHandlers = new RomHandler.Factory[] { new Gen1RomHandler.Factory(), new Gen2RomHandler.Factory(),
                new Gen3RomHandler.Factory(), new Gen4RomHandler.Factory(), new Gen5RomHandler.Factory(),
                new Gen6RomHandler.Factory(), new Gen7RomHandler.Factory() };
        romOpener.setGameUpdates(gameUpdates);
        romOpener.setExtraMemoryAvailable(usedLauncher);

        haveCheckedCustomNames = false;
        attemptReadConfig();
        initExplicit();
        //initTweaksPanel();
        initFileChooserDirectories();

        boolean canWrite = attemptWriteConfig();
        if (!canWrite) {
            JOptionPane.showMessageDialog(null, bundle.getString("GUI.startup.cantWriteConfigFileDialog.message"));
        }

        if (!haveCheckedCustomNames) {
            checkCustomNames();
        }

        new Thread(() -> {
            String latestVersionString = "???";

            try {

                URL url = new URI(SysConstants.RELEASES_API_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String output;
                while ((output = br.readLine()) != null) {
                    String[] a = output.split("tag_name\":\"");
                    if (a.length > 1) {
                        latestVersionString = a[1].split("\",")[0];
                    }
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            // If the release version is newer than this version, bold it to make it more obvious.
            if (Version.isReleaseVersionNewer(latestVersionString)) {
                latestVersionString = String.format("<b>%s</b>", latestVersionString);
            }
            String finalLatestVersionString = latestVersionString;
            SwingUtilities.invokeLater(() -> websiteLinkLabel.setText(
                    String.format(bundle.getString("GUI.header.websiteLinkLabel.text"), finalLatestVersionString)));
        }).run();

        frame.setTitle(String.format(bundle.getString("GUI.windowTitle"),Version.LATEST.name));

        settingsManager = new SettingsManager();
        associateSettingControls();


        List<AbstractButton> subControlButtons = List.of(new AbstractButton[] {
                lsNoIrregularAltFormesCheckBox, lsNoPrematureEvosCheckbox,

                sbstUnchangedRadioButton, sbstRandomBuffNerfRadioButton, sbstShuffleRadioButton, sbstRandomRadioButton,

                sbsdUnchangedRadioButton, sbsdShuffleRadioButton, sbsdRandomRadioButton, sbsdFollowMegaEvosCheckBox,
                sbsdFollowEvolutionsCheckBox, secStandardizeEXPCurvesCheckBox, sbsUpdateBaseStatsCheckBox,

                stUnchangedRadioButton, stRandomFollowEvolutionsRadioButton, stRandomCompletelyRadioButton,
                stForceDualTypeCheckBox,

                saUnchangedRadioButton, saRandomRadioButton,

                peUnchangedRadioButton, peRandomRadioButton, peRandomEveryLevelRadioButton,
                peChangeImpossibleEvosCheckBox, peMakeEvolutionsEasierCheckBox, peAllowAltFormesCheckBox,

                spUnchangedRadioButton, spCustomRadioButton, spRandomRadioButton,
                spTypeNoneRadioButton, spTypeFwgRadioButton, spTypeTriangleRadioButton, spTypeSingleRadioButton,
                spTypeNoDualCheckbox,
                spBSTMinimumCheckbox, spBSTMaximumCheckbox, spRandomizeStarterHeldItemsCheckBox,

                stpUnchangedRadioButton, stpSwapLegendariesSwapStandardsRadioButton, stpRandomCompletelyRadioButton,
                stpRandomSimilarStrengthRadioButton, stpPercentageLevelModifierCheckBox,

                mdUpdateMovesCheckBox,

                pmsUnchangedRadioButton, pmsRandomPreferringSameTypeRadioButton, pmsRandomCompletelyRadioButton,
                pmsMetronomeOnlyModeRadioButton, pmsGuaranteedLevel1MovesCheckBox, pmsForceGoodDamagingCheckBox,

                tpTrainersEvolveTheirPokemonCheckbox, tpPercentageLevelModifierCheckBox,
                tpEliteFourUniquePokemonCheckBox, tbsUnchangedStyleRadioButton, tbsRandomStyleRadioButton,
                tpAllowAlternateFormesCheckBox, tpBossTrainersCheckBox, tpImportantTrainersCheckBox,
                tpRegularTrainersCheckBox, tpBossTrainersItemsCheckBox, tpImportantTrainersItemsCheckBox,
                tpRegularTrainersItemsCheckBox,

                totpUnchangedRadioButton, totpRandomRadioButton, totpRandomSimilarStrengthRadioButton,
                totpAllyUnchangedRadioButton, totpAllyRandomRadioButton, totpAllyRandomSimilarStrengthRadioButton,
                totpPercentageLevelModifierCheckBox,

                wpRandomizeWildPokemonCheckBox,
                wpZoneNoneRadioButton, wpZoneEncounterSetRadioButton,
                wpZoneMapRadioButton, wpZoneNamedLocationRadioButton, wpZoneGameRadioButton,
                wpTRNoneRadioButton, wpTRThemedAreasRadioButton, wpTRKeepPrimaryRadioButton,
                wpSimilarStrengthCheckBox, wpSetMinimumCatchRateCheckBox, wpRandomizeHeldItemsCheckBox,
                wpPercentageLevelModifierCheckBox,

                tmmUnchangedRadioButton, tmmRandomRadioButton, tmmForceGoodDamagingCheckBox, thcLevelupMoveSanityCheckBox,

                thcUnchangedRadioButton, thcRandomPreferSameTypeRadioButton, thcRandomCompletelyRadioButton,
                thcFullCompatibilityRadioButton,

                mtmUnchangedRadioButton, mtmRandomRadioButton, mtmForceGoodDamagingCheckBox, mtcUnchangedRadioButton,
                mtcLevelupMoveSanityCheckBox,

                mtcRandomPreferSameTypeRadioButton, mtcRandomCompletelyRadioButton, mtcFullCompatibilityRadioButton,

                fiUnchangedRadioButton, fiShuffleRadioButton, fiRandomRadioButton, fiRandomEvenDistributionRadioButton,

                shUnchangedRadioButton, shShuffleRadioButton, shRandomRadioButton, puUnchangedRadioButton,

                puRandomRadioButton,

                teUnchangedRadioButton, teRandomRadioButton, teRandomBalancedRadioButton,
                teKeepTypeIdentitiesRadioButton, teInverseRadioButton,

                ppalUnchangedRadioButton, ppalRandomRadioButton,

                cpgUnchangedRadioButton, cpgCustomRadioButton,
        });
        subControlButtons.forEach(comp -> comp.addActionListener(_ -> enableOrDisableSubControls()));

        openROMButton.addActionListener(_ -> selectAndOpenRom());
        peMakeEvolutionsEasierLvlSlider.addChangeListener(_ -> updateFullyEvolvedAtLvlLabel());

        spCustom1ComboBox.addActionListener(_ -> enableOrDisableSubControls());
        spCustom2ComboBox.addActionListener(_ -> enableOrDisableSubControls());
        spCustom3ComboBox.addActionListener(_ -> enableOrDisableSubControls());

        spBSTMinimumSpinner.addChangeListener(_ -> checkSpMaximumNeedsRaise());
        spBSTMaximumSpinner.addChangeListener(_ -> checkSpMinimumNeedsLower());

        tpPercentageEvolutionLevelModifierSpinSlider.addChangeListener(_ -> updateFullyEvolvedAtLvlLabel());

        websiteLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Desktop desktop = java.awt.Desktop.getDesktop();
                try {
                    desktop.browse(new URI(SysConstants.RELEASES_URL));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        wikiLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Desktop desktop = java.awt.Desktop.getDesktop();
                try {
                    desktop.browse(new URI(SysConstants.WIKI_HOME_URL));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        randomizeSaveButton.addActionListener(_ -> saveROM());
        premadeSeedButton.addActionListener(_ -> presetLoader());
        loadSettingsButton.addActionListener(_ -> loadQS());
        saveSettingsButton.addActionListener(_ -> saveQS());
        settingsButton.addActionListener(_ -> settingsMenu.show(settingsButton,0,settingsButton.getHeight()));
        themeSelectionMenuItem.addActionListener(_ -> new ThemeSelectionDialog(this, frame));
        customNamesEditorMenuItem.addActionListener(_ -> customNamesEditorMenuItemActionPerformed());
        applyGameUpdateMenuItem.addActionListener(_ -> applyGameUpdateMenuItemActionPerformed());
        removeGameUpdateMenuItem.addActionListener(_ -> removeGameUpdateMenuItemActionPerformed());
        loadGetSettingsMenuItem.addActionListener(_ -> loadGetSettingsMenuItemActionPerformed());
        keepOrUnloadGameAfterRandomizingMenuItem.addActionListener(_ -> keepOrUnloadGameAfterRandomizingMenuItemActionPerformed());
        /*
        limitPokemonButton.addActionListener(_ -> {
            GenerationLimitDialog gld = new GenerationLimitDialog(frame, currentRestrictions,
                    romHandler.generationOfPokemon(), romHandler.forceSwapStaticMegaEvos());
            if (gld.pressedOK()) {
                currentRestrictions = gld.getChoice();
                boolean isTypeTheme = isTrainerSetting(TRAINER_TYPE_THEMED) || isTrainerSetting(TRAINER_TYPE_THEMED_ELITE4_GYMS)
                        || isTrainerSetting(TRAINER_KEEP_THEMED) || isTrainerSetting(TRAINER_KEEP_THEME_OR_PRIMARY);
                if (currentRestrictions != null && !currentRestrictions.allowTrainerSwapMegaEvolvables(
                        romHandler.forceSwapStaticMegaEvos(), isTypeTheme)) {
                    disableAndDeselectButtons(tpSwapMegaEvosCheckBox);
                }
            }
        });
         */

        frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {

            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {
                showInitialPopup();
            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
        batchRandomizationMenuItem.addActionListener(_ -> batchRandomizationSettingsDialog());


    }

    private void associateSettingControls() {
        //Huh. Interesting. We don't really need to hold on to the SettingUI controls. We just need to *make* them.

        //...Maybe I'll put them all in a List anyway, just in case. Much easier to ignore a created list
        //than to convert a bunch of freestanding constructors to a list creation, if I'm wrong.

        List<SettingCoordinator<?, ?>> settingUICoordinators = List.of(
                // *** GENERAL ***
                //General Options
                associateCheckBox(Name.NO_RANDOM_INTRO_MON, coRandomIntroMonCheckBox),
                associateCheckBox(Name.NO_PREMATURE_EVOLUTIONS, lsNoPrematureEvosCheckbox),
                associateCheckBox(Name.RACE_MODE, raceModeCheckBox),
                associateCheckBox(Name.NO_IRREGULAR_ALT_FORMES, lsNoIrregularAltFormesCheckBox),
                //Limit Pokemon
                //TODO: add to list OR add handling to dialog

                // *** SPECIES TRAITS ***
                //Species BSTs
                associateButtonSet(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        Map.of(
                                BSTMod.UNCHANGED, sbstUnchangedRadioButton,
                                BSTMod.RANDOM_BUFF_NERF, sbstRandomBuffNerfRadioButton,
                                BSTMod.SHUFFLE, sbstShuffleRadioButton,
                                BSTMod.RANDOM, sbstRandomRadioButton
                        )),
                associateSpinSlider(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, sbstRandomBuffNerfSpinSlider),
                associateCheckBox(Name.SPECIES_BSTS_FOLLOW_EVOLUTION, sbstFollowEvolutionsCheckBox),
                associateCheckBox(Name.SPECIES_BST_SHUFFLE_LEGENDARIES_SEPARATELY, sbstSwapLegendariesCheckBox),
                //Species Base Stat Distributions
                associateButtonSet(Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS,
                        Map.of(
                                BaseStatDistributionsMod.UNCHANGED, sbsdUnchangedRadioButton,
                                BaseStatDistributionsMod.SHUFFLE, sbsdShuffleRadioButton,
                                BaseStatDistributionsMod.RANDOM, sbsdRandomRadioButton
                        )),
                associateCheckBox(Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS, sbsdFollowEvolutionsCheckBox),
                associateCheckBox(Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_MEGA_EVOLUTIONS, sbsdFollowMegaEvosCheckBox),
                associateCheckBox(Name.SPECIES_STAT_DISTRIBUTIONS_ASSIGN_EVO_STATS_RANDOMLY, sbsdAssignEvoStatsRandomlyCheckBox),
                //Update Base Stats
                associateCheckBox(Name.UPDATE_SPECIES_BASE_STATS, sbsUpdateBaseStatsCheckBox),
                associateSpinner(Name.SPECIES_UPDATE_BASE_STATS_TO_GENERATION, sbsUpdateGenerationChoiceSpinner),
                //Species Types
                associateButtonSet(Name.RANDOMIZE_SPECIES_TYPES,
                        Map.of(
                                SpeciesTypesMod.UNCHANGED, stUnchangedRadioButton,
                                SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS, stRandomFollowEvolutionsRadioButton,
                                SpeciesTypesMod.COMPLETELY_RANDOM, stRandomCompletelyRadioButton
                        )),
                associateCheckBox(Name.SPECIES_TYPES_FOLLOW_MEGA_EVOLUTIONS, stFollowMegaEvosCheckBox),
                associateCheckBox(Name.SPECIES_TYPES_FORCE_DUAL_TYPES, stForceDualTypeCheckBox),
                //Species Abilities
                associateButtonSet(Name.RANDOMIZE_SPECIES_ABILITIES,
                        Map.of(
                                AbilitiesMod.UNCHANGED, saUnchangedRadioButton,
                                AbilitiesMod.RANDOMIZE, saRandomRadioButton
                        )),
                associateCheckBox(Name.SPECIES_ABILITIES_FOLLOW_EVOLUTIONS, saFollowEvolutionsCheckBox),
                associateCheckBox(Name.SPECIES_ABILITIES_FOLLOW_MEGA_EVOLUTIONS, saFollowMegaEvosCheckBox),
                associateCheckBox(Name.SPECIES_ALWAYS_HAVE_TWO_ABILITIES, saForceTwoAbilitiesCheckbox),
                associateCheckBox(Name.SPECIES_ABILITIES_COMBINE_DUPLICATES, saWeighDuplicatesTogetherCheckBox),
                associateCheckBox(Name.SPECIES_ABILITIES_BAN_WONDER_GUARD, saBanWonderGuardCheckBox),
                associateCheckBox(Name.SPECIES_ABILITIES_BAN_TRAPPING, saBanTrappingAbilitiesCheckBox),
                associateCheckBox(Name.SPECIES_ABILITIES_BAN_MINOR, saBanMinorAbilitiesCheckBox),
                associateCheckBox(Name.SPECIES_ABILITIES_BAN_NEGATIVE, saBanNegativeAbilitiesCheckBox),
                //Species evolutions
                associateButtonSet(Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                        Map.of(
                                EvolutionsMod.UNCHANGED, peUnchangedRadioButton,
                                EvolutionsMod.RANDOM, peRandomRadioButton,
                                EvolutionsMod.RANDOM_EVERY_LEVEL, peRandomEveryLevelRadioButton
                        ))


                //TODO: complete list of settings
        );

    }

    private BooleanSettingCoordinator<CheckBoxManager> associateCheckBox(Name settingName, JCheckBox checkBox) {
        return new BooleanSettingCoordinator<>(settingName, settingsManager, new CheckBoxManager(checkBox));
    }

    private <E extends Enum<E>, J extends AbstractButton> EnumSettingCoordinator<E> associateButtonSet(
            Name settingName, Map<E, J> map) {
        return new EnumSettingCoordinator<>(settingName, settingsManager, new ButtonGroupManager<>(map));
    }

    private NumericSettingCoordinator<Integer, SpinSliderManager> associateSpinSlider(
            Name settingName, SpinSlider spinSlider) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SpinSliderManager(spinSlider));
    }

    private NumericSettingCoordinator<Integer, SpinnerManager> associateSpinner(Name settingName, JSpinner spinner) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SpinnerManager(spinner));
    }

    private void checkSpMinimumNeedsLower() {
        if((int)spBSTMaximumSpinner.getValue() < (int)spBSTMinimumSpinner.getValue()) {
            spBSTMinimumSpinner.setValue(spBSTMaximumSpinner.getValue());
        }
    }

    private void checkSpMaximumNeedsRaise() {
        if((int)spBSTMaximumSpinner.getValue() < (int)spBSTMinimumSpinner.getValue()) {
            spBSTMaximumSpinner.setValue(spBSTMinimumSpinner.getValue());
        }
    }

    private void updateFullyEvolvedAtLvlLabel() {
        if (tpTrainersEvolveTheirPokemonCheckbox.isSelected()) {
            int highestEvoLvl = peMakeEvolutionsEasierCheckBox.isSelected()
                    ? peMakeEvolutionsEasierLvlSlider.getValue() : romHandler.getHighestEvoLvl();
            int modifiedLevel = (int) Math.ceil(highestEvoLvl * (1 + tpPercentageEvolutionLevelModifierSpinSlider.getValue() / 100.0));
            tpCalculatedFullyEvolvedLvlLabel.setText(String.format(
                    bundle.getString("GUI.foeTab.trainersPanel.calculatedFullyEvolvedLvlLabel.text"),
                    Math.max(1, Math.min(100, modifiedLevel))));
        }
    }

    private void showInitialPopup() {
        if (!usedLauncher) {
            String message = bundle.getString("GUI.startup.pleaseUseLauncherDialog.message");
            Object[] messages = {message};
            JOptionPane.showMessageDialog(frame, messages);
        }
        if (initialPopup) {
            String message = String.format(bundle.getString("GUI.startup.firstStartDialog.message"),Version.LATEST.name);
            JLabel label = new JLabel(String.format(
                    bundle.getString("GUI.startup.firstStartDialog.wikiLink"),
                    SysConstants.WIKI_IMPORTANT_INFO_URL));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Desktop desktop = java.awt.Desktop.getDesktop();
                    try {
                        desktop.browse(new URI(SysConstants.WIKI_IMPORTANT_INFO_URL));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            label.setCursor(new java.awt.Cursor(Cursor.HAND_CURSOR));
            Object[] messages = {message,label};
            JOptionPane.showMessageDialog(frame, messages);
            initialPopup = false;
            attemptWriteConfig();
        }
    }

    private void showInvalidRomPopup() {
        if (showInvalidRomPopup) {
            String message = String.format(bundle.getString("GUI.loadROM.invalidROMDialog.message"));
            JCheckBox checkbox = new JCheckBox(bundle.getString("GUI.loadROM.invalidROMDialog.dontShowAgainCheckBox.text"));
            Object[] messages = {message, checkbox};
            Object[] options = {"OK"};
            JOptionPane.showOptionDialog(frame,
                    messages,
                    bundle.getString("GUI.loadROM.invalidROMDialog.title"),
                    JOptionPane.OK_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    null);
            showInvalidRomPopup = !checkbox.isSelected();
            attemptWriteConfig();
        }
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        if (!theme.isInstalled()) {
            this.theme = Theme.DEFAULT;
        }

        try {
            javax.swing.UIManager.setLookAndFeel(theme.getLaf());
            SwingUtilities.updateComponentTreeUI(mainPanel);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RandomizerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        }

        attemptWriteConfig();
    }

    private void initFileChooserDirectories() {
        romOpenChooser.setCurrentDirectory(new File(openDirectory));
        romSaveChooser.setCurrentDirectory(new File(saveDirectory));
        if (new File(RootPath.path + "settings/").exists()) {
            qsOpenChooser.setCurrentDirectory(new File(RootPath.path + "settings/"));
            qsSaveChooser.setCurrentDirectory(new File(RootPath.path + "settings/"));
            qsUpdateChooser.setCurrentDirectory(new File(RootPath.path + "settings/"));
        } else {
            qsOpenChooser.setCurrentDirectory(new File(RootPath.path));
            qsSaveChooser.setCurrentDirectory(new File(RootPath.path));
            qsUpdateChooser.setCurrentDirectory(new File(RootPath.path));
        }
    }

    private void initExplicit() {

        versionLabel.setText(String.format(bundle.getString("GUI.header.versionLabel.text"), Version.LATEST.name));
        mtNoExistLabel.setVisible(false);
        qoltNoneAvailableLabel.setVisible(false);
        ppalNotExistLabel.setVisible(false);
        ppalPartiallyImplementedLabel.setVisible(false);
        cpgNotExistLabel.setVisible(false);
        websiteLinkLabel.setCursor(new java.awt.Cursor(Cursor.HAND_CURSOR));
        wikiLinkLabel.setCursor(new java.awt.Cursor(Cursor.HAND_CURSOR));

        romOpenChooser.setFileFilter(new ROMFilter());

        romSaveChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        romSaveChooser.setFileFilter(new ROMFilter());

        qsOpenChooser.setFileFilter(new QSFileFilter());

        qsSaveChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        qsSaveChooser.setFileFilter(new QSFileFilter());

        qsUpdateChooser.setFileFilter(new QSFileFilter());

        settingsMenu = new JPopupMenu();

        SpinnerModel bossTrainerModel = new SpinnerNumberModel(
                1,
                1,
                5,
                1
        );
        SpinnerModel importantTrainerModel = new SpinnerNumberModel(
                1,
                1,
                5,
                1
        );
        SpinnerModel regularTrainerModel = new SpinnerNumberModel(
                1,
                1,
                5,
                1
        );

        SpinnerModel eliteFourUniquePokemonModel = new SpinnerNumberModel(
                1,
                1,
                2,
                1
        );

        sbstRandomBuffNerfSpinSlider.setModel(new SpinnerNumberModel(
                0,
                0,
                50,
                1
        ));

        stpPercentageLevelModifierSpinSlider.setModel(new SpinnerNumberModel(
                0,
                -100,
                155,
                1
        ));

        pmsForceGoodDamagingSpinSlider.setModel(new SpinnerNumberModel(
                0,
                0,
                100,
                1
        ));

        List<String> keys = new ArrayList<>(bundle.keySet());
        Collections.sort(keys);
        for (String k: keys) {
            if (k.matches("^GUI\\.tpMain.*\\.text$")) {
                trainerSettings.add(bundle.getString(k));
                trainerSettingToolTips.add(k.replace("text","toolTipText"));
            }
            if (k.matches("^GUI\\.tpBattleStyle.*\\.text$")) {
                selectableBattleStyles.add(bundle.getString(k));
                selectableBattleStylesTooltips.add(k.replace("text","toolTipText"));
            }
        }

        tpBossTrainersSpinner.setModel(bossTrainerModel);
        tpImportantTrainersSpinner.setModel(importantTrainerModel);
        tpRegularTrainersSpinner.setModel(regularTrainerModel);
        tpEliteFourUniquePokemonSpinner.setModel(eliteFourUniquePokemonModel);
        tpPercentageEvolutionLevelModifierSpinSlider.setModel(new SpinnerNumberModel(
                0,
                -100,
                155,
                1
        ));
        tpPercentageLevelModifierSpinSlider.setModel(new SpinnerNumberModel(
                0,
                -100,
                155,
                1
        ));

        totpPercentageLevelModifierSpinSlider.setModel(new SpinnerNumberModel(
                0,
                -100,
                155,
                1
        ));

        wpPercentageLevelModifierSpinSlider.setModel(new SpinnerNumberModel(
                0,
                -100,
                155,
                1
        ));

        tmmForceGoodDamagingSpinSlider.setModel(new SpinnerNumberModel(
                0,
                0,
                100,
                1
        ));

        mtmForceGoodDamagingSpinSlider.setModel(new SpinnerNumberModel(
                0,
                0,
                100,
                1
        ));

        themeSelectionMenuItem = new JMenuItem();
        themeSelectionMenuItem.setText(bundle.getString("GUI.header.settingsMenu.themeSelectionMenuItem.text"));
        settingsMenu.add(themeSelectionMenuItem);

        customNamesEditorMenuItem = new JMenuItem();
        customNamesEditorMenuItem.setText(bundle.getString("GUI.header.settingsMenu.customNamesEditorMenuItem.text"));
        settingsMenu.add(customNamesEditorMenuItem);

        loadGetSettingsMenuItem = new JMenuItem();
        loadGetSettingsMenuItem.setText(bundle.getString("GUI.header.settingsMenu.loadGetSettingsMenuItem.text"));
        settingsMenu.add(loadGetSettingsMenuItem);

        applyGameUpdateMenuItem = new JMenuItem();
        applyGameUpdateMenuItem.setText(bundle.getString("GUI.header.settingsMenu.applyGameUpdateMenuItem.text"));
        settingsMenu.add(applyGameUpdateMenuItem);

        removeGameUpdateMenuItem = new JMenuItem();
        removeGameUpdateMenuItem.setText(bundle.getString("GUI.header.settingsMenu.removeGameUpdateMenuItem.text"));
        settingsMenu.add(removeGameUpdateMenuItem);

        keepOrUnloadGameAfterRandomizingMenuItem = new JMenuItem();
        if (this.unloadGameOnSuccess) {
            keepOrUnloadGameAfterRandomizingMenuItem.setText(bundle.getString("GUI.header.settingsMenu.keepGameLoadedAfterRandomizingMenuItem.text"));
        } else {
            keepOrUnloadGameAfterRandomizingMenuItem.setText(bundle.getString("GUI.header.settingsMenu.unloadGameAfterRandomizingMenuItem.text"));
        }
        settingsMenu.add(keepOrUnloadGameAfterRandomizingMenuItem);

        batchRandomizationMenuItem = new JMenuItem();
        batchRandomizationMenuItem.setText(bundle.getString("GUI.header.settingsMenu.batchRandomizationMenuItem.text"));
        settingsMenu.add(batchRandomizationMenuItem);
    }

    private void selectAndOpenRom() {
        romOpenChooser.setSelectedFile(null);
        int returnVal = romOpenChooser.showOpenDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            openDirectory = romOpenChooser.getSelectedFile().getParentFile().getAbsolutePath();
            attemptWriteConfig();
            openRom(romOpenChooser.getSelectedFile(), false);
        }
    }

    /**
     * Creates a blocking load dialog, and sets up a {@link Thread} which will:
     * <ol>
     *     <li>Open up the ROM file, to get a {@link RomHandler}.</li>
     *     <li>Remove the blocking load dialog.</li>
     *     <li>Tell the GUI to react to a ROM having been opened, or create an error dialog if the opening failed.</li>
     * </ol>
     * Returns the Thread.
     * @param f The {@link File} to be opened as a ROM.
     * @param reinitialize If true, the load dialog will not be shown, and the GUI will not react to a ROM being opened.
     */
    private Thread openRom(File f, boolean reinitialize) {
        // A rather simple method - make the romOpener open the file and react to its results -
        // complicated by the need of an animated loading dialog and thus multithreading...
        opDialog = new OperationDialog(bundle.getString("GUI.loadROM.loadingDialog.message"), frame, true);
        Thread t = new Thread(() -> {
            SwingUtilities.invokeLater(() -> opDialog.setVisible(!reinitialize));
            try {
                RomOpener.Results results = romOpener.openRomFile(f);

                SwingUtilities.invokeLater(() -> {
                    opDialog.setVisible(false);
                    if (!reinitialize) {
                        initialState();
                    }
                    if (results.wasOpeningSuccessful()) {
                        unloadRomHandler();
                        romHandler = results.getRomHandler();
                        if (!reinitialize) {
                            romLoaded();
                        }
                    } else {
                        reportOpenRomFailure(f, results);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    opDialog.setVisible(false);
                    initialState();
                    attemptToLogException(e, "GUI.loadROM.loadFailedDialog.message", "GUI.loadROM.loadFailedNoLogDialog.message", null, null);
                });
            }
        });
        t.start();
        return t;
    }

    // This being public is not very pretty, but it works to get this code to PresetLoadDialog without copy-pasting
    public void reportOpenRomFailure(File f, RomOpener.Results results) {
        switch (results.getFailType()) {
            case UNREADABLE:
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.loadROM.unreadableRomDialog.message"), f.getName()));
                break;
            case INVALID_TOO_SHORT:
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.loadROM.tooShortToBeARomDialog.message"), f.getName()));
                break;
            case INVALID_ZIP_FILE:
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.loadROM.openedZIPFileDialog.message"), f.getName()));
                break;
            case INVALID_RAR_FILE:
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.loadROM.openedRARFileDialog.message"), f.getName()));
                break;
            case INVALID_IPS_FILE:
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.loadROM.openedIPSFileDialog.message"), f.getName()));
                break;
            case EXTRA_MEMORY_NOT_AVAILABLE:
                JOptionPane.showMessageDialog(frame,
                        bundle.getString("GUI.startup.pleaseUseLauncherDialog.message"));
                break;
            case ENCRYPTED_ROM:
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.loadROM.encryptedRomDialog.message"), f.getName()));
                break;
            case UNSUPPORTED_ROM:
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.loadROM.unsupportedRomDialog.message"), f.getName()));
                break;
        }
    }

    private void saveROM() {
        if (romHandler == null) {
            return; // none loaded
        }
        if (raceModeCheckBox.isSelected() && batchRandomizationSettings.isBatchRandomizationEnabled()) {
            JOptionPane.showMessageDialog(frame, bundle.getString("GUI.batchRandomization.requirementsDialog.message"));
            return;
        }
        if (raceModeCheckBox.isSelected() && isTrainerSetting(TRAINER_UNCHANGED) &&
                !wpRandomizeWildPokemonCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(frame, bundle.getString("GUI.saveROM.raceModeRequirementsDialog.message"));
            return;
        }
        /*
        if (limitPokemonCheckBox.isSelected()
                && (this.currentRestrictions == null || this.currentRestrictions.nothingSelected())) {
            JOptionPane.showMessageDialog(frame, bundle.getString("GUI.pokeLimitNotChosen"));
            return;
        }
         */

        SaveType outputType = askForSaveType();
        romSaveChooser.setSelectedFile(null);
        boolean allowed = false;
        File fh = null;
        if (batchRandomizationSettings.isBatchRandomizationEnabled() && outputType != SaveType.INVALID) {
            allowed = true;
        }
        else if (outputType == SaveType.FILE) {
            romSaveChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = romSaveChooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fh = romSaveChooser.getSelectedFile();
                // Fix or add extension
                List<String> extensions = new ArrayList<>(Arrays.asList("sgb", "gbc", "gba", "nds", "cxi"));
                extensions.remove(this.romHandler.getDefaultExtension());
                fh = FileNameFunctions.fixFilename(fh, this.romHandler.getDefaultExtension(), extensions);
                allowed = true;
                if (this.romHandler instanceof AbstractDSRomHandler || this.romHandler instanceof Abstract3DSRomHandler) {
                    String currentFN = this.romHandler.loadedFilename();
                    if (currentFN.equals(fh.getAbsolutePath())) {
                        JOptionPane.showMessageDialog(frame, bundle.getString("GUI.saveROM.cantOverwriteDSDialog.message"));
                        allowed = false;
                    }
                }
            }
        } else if (outputType == SaveType.DIRECTORY) {
            romSaveChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = romSaveChooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fh = romSaveChooser.getSelectedFile();
                allowed = true;
            }
        }

        if (allowed && fh != null) {
            saveDirectory = fh.getParentFile().getAbsolutePath();
            attemptWriteConfig();
            saveRandomizedRom(outputType, fh);

        } else if (allowed && batchRandomizationSettings.isBatchRandomizationEnabled()) {
            int numberOfRandomizedROMs = batchRandomizationSettings.getNumberOfRandomizedROMs();
            int startingIndex = batchRandomizationSettings.getStartingIndex();
            int endingIndex = startingIndex + numberOfRandomizedROMs;
            final String progressTemplate = bundle.getString("GUI.batchRandomization.progressDialog.message");
            OperationDialog batchProgressDialog = new OperationDialog(String.format(progressTemplate, 0, numberOfRandomizedROMs), frame, true);
            SwingWorker<Void, Void> swingWorker = new SwingWorker<>() {
                int i;

                @Override
                protected Void doInBackground() {
                    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    SwingUtilities.invokeLater(() -> batchProgressDialog.setVisible(true));

                    for (i = startingIndex; i < endingIndex; i++) {
                        File rom = prepareFile();
                        int currentRomNumber = i - startingIndex + 1;

                        SwingUtilities.invokeLater(
                                () -> batchProgressDialog.setLoadingLabelText(String.format(progressTemplate,
                                        currentRomNumber,
                                        numberOfRandomizedROMs))
                        );
                        saveRandomizedRom(outputType, rom);
                    }
                    return null;
                }

                private File prepareFile() {
                    String fileName = batchRandomizationSettings.getOutputDirectory() +
                            File.separator +
                            batchRandomizationSettings.getFileNamePrefix() +
                            i;
                    if (outputType == SaveType.FILE) {
                        fileName += '.' + romHandler.getDefaultExtension();
                    }
                    File rom = new File(fileName);
                    if (outputType == SaveType.DIRECTORY) {
                        boolean _ = rom.mkdirs();
                    }
                    return rom;
                }

                @Override
                protected void done() {
                    super.done();
                    if (batchRandomizationSettings.shouldAutoAdvanceStartingIndex()) {
                        batchRandomizationSettings.setStartingIndex(i);
                        attemptWriteConfig();
                    }
                    SwingUtilities.invokeLater(() -> batchProgressDialog.setVisible(false));
                    JOptionPane.showMessageDialog(frame, bundle.getString("GUI.saveROM.randomizationDoneDialog.message"));
                    if (unloadGameOnSuccess) {
                        unloadRomHandler();
                        initialState();
                    } else {
                        reinitializeRomHandler(false);
                    }
                    frame.setCursor(null);
                }
            };
            swingWorker.execute();
        }
    }

    /**
     * Closes any resources {@link #romHandler} might still have been using, and sets it to null.
     * The idea here is that the romHandler is allowed to have a resource open for its whole lifetime,
     * but for no longer. Thus, this method <b>must</b> be called anytime before romHandler is set or discarded.
     * <br><br>
     * (Having a resource open for a long time is indeed risky, but allows for worthwhile RAM optimizations)
     */
    private void unloadRomHandler() {
        if (romHandler == null) return;
        if (romHandler.getResourceLifetime() == RomHandler.ResourceLifetime.SAME_AS_ROMHANDLER) {
            try {
                romHandler.closeResources();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        romHandler = null;
    }

    private void saveRandomizedRom(SaveType outputType, File fh) {
        long seed = SeedPicker.pickSeed();
        presetMode = false;

        CustomPlayerGraphics cpg = getCPGFromGUI();
        performRandomization(fh.getAbsolutePath(), seed, cpg, outputType == SaveType.DIRECTORY);
    }

    private CustomPlayerGraphics getCPGFromGUI() {
        return cpgCustomRadioButton.isSelected() ? cpgSelection.getCustomPlayerGraphics() : null;
    }

    private void loadQS() {
        if (this.romHandler == null) {
            return;
        }
        qsOpenChooser.setSelectedFile(null);
        int returnVal = qsOpenChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fh = qsOpenChooser.getSelectedFile();
            try {
                FileInputStream fis = new FileInputStream(fh);
                SettingsManager settings = SettingsManager.readFromFileFormat(fis);
                fis.close();

                SwingUtilities.invokeLater(() -> {
                    // load settings
                    initialState();
                    romLoaded();
                    Settings.StartersMod startersMod = settings.get(Name.RANDOMIZE_STARTERS);
                    // TODO: might be nice to be able to say "hey your custom starter is not an option"
//                    if (feedback.isChangedStarter() && startersMod == Settings.StartersMod.CUSTOM) {
//                        JOptionPane.showMessageDialog(frame, bundle.getString("GUI.saveROM.starterUnavailableDialog.message"));
//                    }
                    this.restoreStateFromSettings(settings);

                    if (settings.isUpdatedFromOldVersion()) {
                        // show a warning dialog, but load it
                        JOptionPane.showMessageDialog(frame, bundle.getString("GUI.loadSettings.settingsFileOlderDialog.message"));
                    }

                    JOptionPane.showMessageDialog(frame,
                            String.format(bundle.getString("GUI.loadSettings.settingsLoadedDialog.message"), fh.getName()));
                });
            } catch (UnsupportedOperationException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, bundle.getString("GUI.loadSettings.invalidSettingsFileDialog.message"));
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, bundle.getString("GUI.loadSettings.settingsLoadFailedDialog.message"));
            }
        }
    }

    private void saveQS() {
        // TODO: entirely rewrite

        /*
        if (this.romHandler == null) {
            return;
        }
        qsSaveChooser.setSelectedFile(null);
        int returnVal = qsSaveChooser.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fh = qsSaveChooser.getSelectedFile();
            // Fix or add extension
            fh = FileNameFunctions.fixFilename(fh, "rnqs");
            // Save now?
            try {
                FileOutputStream fos = new FileOutputStream(fh);
                getCurrentSettings().writeToFileFormat(fos);
                fos.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, bundle.getString("GUI.saveSettings.settingsSaveFailedDialog.message"));
            }
        }
         */
    }

    private void performRandomization(final String filename, final long seed,
                                      CustomPlayerGraphics cpg,
                                      boolean saveAsDirectory) {
        final SettingsManager settings = settingsManager;
        final boolean raceMode = settings.get(Name.RACE_MODE);
        final boolean batchRandomization = batchRandomizationSettings.isBatchRandomizationEnabled() && !presetMode;
        // Setup log
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream log = getPrintStream(baos);

        try {
            opDialog = new OperationDialog(bundle.getString("GUI.saveROM.savingDialog.message"), frame, true);
            Thread t = new Thread(() -> performRandomizationInner(
                    filename, seed, settings, cpg, baos, log, raceMode, batchRandomization, saveAsDirectory
            ));
            t.start();
            if (batchRandomization) {
                t.join();
            }
        } catch (Exception ex) {
            attemptToLogException(ex,
                    "GUI.saveROM.saveFailedDialog.message",
                    "GUI.saveROM.saveFailedNoLogDialog.message",
                    settings.toString(),
                    Long.toString(seed));
            log.close();
        }
    }

    private PrintStream getPrintStream(ByteArrayOutputStream baos) {
        PrintStream log;
        log = new PrintStream(baos, false, StandardCharsets.UTF_8);
        return log;
    }

    private void performRandomizationInner(String filename,
                                           long seed, SettingsManager settings, CustomPlayerGraphics cpg,
                                           ByteArrayOutputStream baos, PrintStream log,
                                           boolean raceMode, boolean batchRandomization, boolean saveAsDirectory) {
        SwingUtilities.invokeLater(() -> opDialog.setVisible(!batchRandomization));
        GameRandomizer randomizer = new GameRandomizer(settings, cpg, romHandler, bundle, saveAsDirectory);
        GameRandomizer.Results results = randomizer.randomize(filename, log, seed);

        if (results.wasSaveSuccessful()) {
            if (!results.wasLogSuccessful()) {
                attemptToLogException(results.getLogException(), "GUI.saveROM.logFailedDialog.message", "GUI.saveROM.logFailedNoLogDialog.message",
                        true, settings.toString(), Long.toString(seed));
            }
            SwingUtilities.invokeLater(() -> finishRandomization(
                    filename, seed, cpg, baos, results.getCheckValue(), raceMode, batchRandomization
            ));
        } else {
            Exception e = results.getException();
            if (e instanceof RandomizationException) {
                attemptToLogException(e, "GUI.saveROM.saveFailedDialog.message", "GUI.saveROM.saveFailedNoLogDialog.message", true,
                        settings.toString(), Long.toString(seed));
            } else if (e instanceof CannotWriteToLocationException) {
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.saveROM.cannotWriteToLocationDialog.message"), filename));
            } else {
                attemptToLogException(e, "GUI.saveROM.saveFailedIODialog.message", "GUI.saveROM.saveFailedIONoLogDialog.message",
                        settings.toString(), Long.toString(seed));
            }

            SwingUtilities.invokeLater(() -> {
                opDialog.setVisible(false);
                unloadRomHandler();
                initialState();
            });
        }
    }

    private void finishRandomization(String filename, long seed,
                                     CustomPlayerGraphics cpg,
                                     ByteArrayOutputStream baos,
                                     int checkValue,
                                     boolean raceMode, boolean batchRandomization) {
        if (cpg != null) {
            recordCPGAsLastUsed(cpg);
        }

        opDialog.setVisible(false);

        showSaveLogDialog(filename, baos, checkValue, raceMode, batchRandomization);

        if (presetMode) {
            JOptionPane.showMessageDialog(frame,
                    bundle.getString("GUI.saveROM.randomizationDoneDialog.message"));

        } else if (!batchRandomization) {
            // TODO: remove / rework config string usage
            // Compile a config string
            // String configString = getCurrentSettings().toString();
            String configString = "DUMMY DUMMY REPORT IF SEEN ON GITHUB";
            // Show the preset maker
            new PresetMakeDialog(frame, seed, configString);
        }

        // Done
        if (this.unloadGameOnSuccess && !batchRandomization) {
            unloadRomHandler();
            initialState();
        } else {
            reinitializeRomHandler(batchRandomization);
        }

    }

    private void showSaveLogDialog(String filename, ByteArrayOutputStream baos,
                                   int checkValue,
                                   boolean raceMode, boolean batchRandomization) {
        byte[] out = baos.toByteArray();
        if (raceMode) {
            JOptionPane.showMessageDialog(frame,
                    String.format(bundle.getString("GUI.saveROM.raceModeCheckValueDialog.message"), checkValue));
        } else if (batchRandomization && batchRandomizationSettings.shouldGenerateLogFile()) {
            try {
                saveLogFile(filename, batchRandomizationSettings.getLogFileEnding(), out);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame,
                        bundle.getString("GUI.saveROM.logSaveFailedDialog.message"));
            }
        } else if (!batchRandomization) {
            int response = JOptionPane.showConfirmDialog(frame,
                    bundle.getString("GUI.saveROM.saveLogDialog.message"),
                    bundle.getString("GUI.saveROM.saveLogDialog.title"),
                    JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                try {
                    saveLogFile(filename, "log", out);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame,
                            bundle.getString("GUI.saveROM.logSaveFailedDialog.message"));
                }
                JOptionPane.showMessageDialog(frame,
                        String.format(bundle.getString("GUI.saveROM.logSavedDialog.message"), filename));
            }
        }
    }

    private void recordCPGAsLastUsed(CustomPlayerGraphics cpg) {
        lastUsedCPGConfigs.put(romHandler.getROMName() + ".pack", cpg.getGraphicsPack().getName());
        lastUsedCPGConfigs.put(romHandler.getROMName() + ".type", cpg.getTypeToReplace().toString());
        attemptWriteConfig();
    }

    private void saveLogFile(String filename, String fileEnding, byte[] out) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename + "." + fileEnding);
        fos.write(0xEF);
        fos.write(0xBB);
        fos.write(0xBF);
        fos.write(out);
        fos.close();
    }

    private void presetLoader() {
        PresetLoadDialog pld = new PresetLoadDialog(this, frame, romOpenChooser, romOpener);
        if (pld.isCompleted()) {
            // Apply it
            long seed = pld.getSeed();
            String config = pld.getSettingsString();
            unloadRomHandler();
            this.romHandler = pld.getROM();
            if (gameUpdates.containsKey(this.romHandler.getROMCode())) {
                this.romHandler.loadGameUpdate(gameUpdates.get(this.romHandler.getROMCode()));
            }
            this.romLoaded();
            SettingsManager settings;
            CustomPlayerGraphics customPlayerGraphics = null;
            try {
                settings = SettingsManager.fromString(config);
                customPlayerGraphics = pld.getCustomPlayerGraphics();
                // settings.tweakForRom(this.romHandler);
                this.restoreStateFromSettings(settings);
            } catch (IllegalArgumentException e) {
                // settings load failed
                e.printStackTrace();
                unloadRomHandler();
                initialState();
            }
            SaveType outputType = askForSaveType();
            romSaveChooser.setSelectedFile(null);
            boolean allowed = false;
            File fh = null;
            if (outputType == SaveType.FILE) {
                romSaveChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int returnVal = romSaveChooser.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fh = romSaveChooser.getSelectedFile();
                    // Fix or add extension
                    List<String> extensions = new ArrayList<>(Arrays.asList("sgb", "gbc", "gba", "nds", "cxi"));
                    extensions.remove(this.romHandler.getDefaultExtension());
                    fh = FileNameFunctions.fixFilename(fh, this.romHandler.getDefaultExtension(), extensions);
                    allowed = true;
                    if (this.romHandler instanceof AbstractDSRomHandler || this.romHandler instanceof Abstract3DSRomHandler) {
                        String currentFN = this.romHandler.loadedFilename();
                        if (currentFN.equals(fh.getAbsolutePath())) {
                            JOptionPane.showMessageDialog(frame, bundle.getString("GUI.saveROM.cantOverwriteDSDialog.message"));
                            allowed = false;
                        }
                    }
                } else {
                    unloadRomHandler();
                    initialState();
                }
            } else if (outputType == SaveType.DIRECTORY) {
                romSaveChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = romSaveChooser.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fh = romSaveChooser.getSelectedFile();
                    allowed = true;
                } else {
                    unloadRomHandler();
                    initialState();
                }
            }

            if (allowed && fh != null) {
                // Apply the seed we were given
                presetMode = true;
                performRandomization(fh.getAbsolutePath(), seed, customPlayerGraphics, outputType == SaveType.DIRECTORY);
            }
        }

    }


    private enum SaveType {
        FILE, DIRECTORY, INVALID
    }

    private SaveType askForSaveType() {
        SaveType saveType = SaveType.FILE;
        if (romHandler.hasGameUpdateLoaded()) {
            String text = bundle.getString("GUI.saveROM.savingWithGameUpdate.message");
            String url = SysConstants.WIKI_3DS_INFO_URL + "#managing-game-updates";
            showMessageDialogWithLink(text, url);
            saveType = SaveType.DIRECTORY;
        } else if (romHandler.generationOfPokemon() == 6 || romHandler.generationOfPokemon() == 7) {
            Object[] options3DS = {"CXI", "LayeredFS"};
            String question = "Would you like to output your 3DS game as a CXI file or as a LayeredFS directory?";
            JLabel label = new JLabel("<html><a href=\"" +  SysConstants.WIKI_3DS_INFO_URL + "#changes-to-saving-a-rom-when-working-with-3ds-games\">For more information, click here.</a>");
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Desktop desktop = java.awt.Desktop.getDesktop();
                    try {
                        desktop.browse(new URI(SysConstants.WIKI_3DS_INFO_URL  + "#changes-to-saving-a-rom-when-working-with-3ds-games"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            label.setCursor(new java.awt.Cursor(Cursor.HAND_CURSOR));
            Object[] messages = {question,label};
            int returnVal3DS = JOptionPane.showOptionDialog(frame,
                    messages,
                    "3DS Output Choice",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options3DS,
                    null);
            if (returnVal3DS < 0) {
                saveType = SaveType.INVALID;
            } else {
                saveType = SaveType.values()[returnVal3DS];
            }
        }
        return saveType;
    }

    private void customNamesEditorMenuItemActionPerformed() {
        new CustomNamesEditorDialog(frame, hasVisitedCustomNamesEditor);
        hasVisitedCustomNamesEditor = true;
        attemptWriteConfig();
    }

    private void applyGameUpdateMenuItemActionPerformed() {

        if (romHandler == null) return;

        gameUpdateChooser.setSelectedFile(null);
        gameUpdateChooser.setFileFilter(new GameUpdateFilter());
        int returnVal = gameUpdateChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fh = gameUpdateChooser.getSelectedFile();

            // On the 3DS, the update has the same title ID as the base game, save for the 8th character,
            // which is 'E' instead of '0'. We can use this to detect if the update matches the game.
            String actualUpdateTitleId = Abstract3DSRomHandler.getTitleIdFromFile(fh.getAbsolutePath());
            if (actualUpdateTitleId == null) {
                // Error: couldn't find a title ID in the update
                JOptionPane.showMessageDialog(frame, String.format(bundle.getString("GUI.loadGameUpdate.invalidGameUpdateDialog.message"), fh.getName()));
                return;
            }
            Abstract3DSRomHandler ctrRomHandler = (Abstract3DSRomHandler) romHandler;
            String baseGameTitleId = ctrRomHandler.getTitleIdFromLoadedROM();
            char[] baseGameTitleIdChars = baseGameTitleId.toCharArray();
            baseGameTitleIdChars[7] = 'E';
            String expectedUpdateTitleId = String.valueOf(baseGameTitleIdChars);
            if (actualUpdateTitleId.equals(expectedUpdateTitleId)) {
                try {
                    romHandler.loadGameUpdate(fh.getAbsolutePath());
                } catch (EncryptedROMException ex) {
                    JOptionPane.showMessageDialog(mainPanel,
                            String.format(bundle.getString("GUI.loadROM.encryptedRomDialog.message"), fh.getAbsolutePath()));
                    return;
                }
                gameUpdates.put(romHandler.getROMCode(), fh.getAbsolutePath());
                attemptWriteConfig();
                removeGameUpdateMenuItem.setVisible(true);
                setRomNameLabel();
                String text = String.format(bundle.getString("GUI.loadGameUpdate.gameUpdateAppliedDialog.message"), romHandler.getROMName());
                String url = SysConstants.WIKI_3DS_INFO_URL + "#3ds-game-updates";
                showMessageDialogWithLink(text, url);
            } else {
                // Error: update is not for the correct game
                JOptionPane.showMessageDialog(frame, String.format(bundle.getString("GUI.loadGameUpdate.nonMatchingGameUpdateDialog.message"), fh.getName(), romHandler.getROMName()));
            }
        }
    }

    private void removeGameUpdateMenuItemActionPerformed() {

        if (romHandler == null) return;

        gameUpdates.remove(romHandler.getROMCode());
        attemptWriteConfig();
        romHandler.removeGameUpdate();
        removeGameUpdateMenuItem.setVisible(false);
        setRomNameLabel();
    }

    private void loadGetSettingsMenuItemActionPerformed() {

        // TODO: this feature makes less sense without the short settings strings. consider
        //  (and remember we want *somewhere* to load old settings strings)
        /*

        if (romHandler == null) return;

        String currentSettingsString = "Current Settings String:";
        JTextField currentSettingsStringField = new JTextField();
        currentSettingsStringField.setEditable(false);
        String theSettingsString = getCurrentSettings().toString();
        currentSettingsStringField.setColumns(SettingsManager.LENGTH_OF_SETTINGS_DATA * 2);
        currentSettingsStringField.setText(theSettingsString);
        String loadSettingsString = "Load Settings String:";
        JTextField loadSettingsStringField = new JTextField();
        Object[] messages = {currentSettingsString,currentSettingsStringField,loadSettingsString,loadSettingsStringField};
        Object[] options = {"Load","Cancel"};
        int choice = JOptionPane.showOptionDialog(
                frame,
                messages,
                "Get/Load Settings String",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null
        );
        if (choice == 0) {
            String configString = loadSettingsStringField.getText().trim();
            if (!configString.isEmpty()) {
                if (configString.length() < 3) {
                    JOptionPane.showMessageDialog(frame,bundle.getString("GUI.loadSettingsString.invalidSettingsStringDialog.message"));
                } else {
                    try {
                        int version = Integer.parseInt(configString.substring(0, 3));
                        if (version > Version.LATEST.id) {
                            JOptionPane.showMessageDialog(frame,bundle.getString("GUI.loadSettingsString.settingsStringTooNewDialog.message"));
                            return;
                        } else if (version < Version.LATEST.id) {
                            JOptionPane.showMessageDialog(frame,bundle.getString("GUI.loadSettingsString.settingsStringOlderDialog.message"));
                        }
                        SettingsManager settings = SettingsManager.fromString(configString);
                        settings.tweakForRom(this.romHandler);
                        restoreStateFromSettings(settings);
                        JOptionPane.showMessageDialog(frame,bundle.getString("GUI.loadSettingsString.settingsStringLoadedDialog.message"));
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(frame,bundle.getString("GUI.loadSettingsString.invalidSettingsStringDialog.message"));
                    }
                }

            }
        }

         */
    }

    private void keepOrUnloadGameAfterRandomizingMenuItemActionPerformed() {
        this.unloadGameOnSuccess = !this.unloadGameOnSuccess;
        if (this.unloadGameOnSuccess) {
            JOptionPane.showMessageDialog(frame, bundle.getString("GUI.afterRandomizing.unloadGameDialog.message"));
            keepOrUnloadGameAfterRandomizingMenuItem.setText(bundle.getString("GUI.header.settingsMenu.keepGameLoadedAfterRandomizingMenuItem.text"));
        } else {
            JOptionPane.showMessageDialog(frame, bundle.getString("GUI.afterRandomizing.keepGameLoadedDialog.message"));
            keepOrUnloadGameAfterRandomizingMenuItem.setText(bundle.getString("GUI.header.settingsMenu.unloadGameAfterRandomizingMenuItem.text"));
        }
        attemptWriteConfig();
    }

    private void showMessageDialogWithLink(String text, String url) {
        JLabel label = new JLabel("<html><a href=\"" + url + "\">For more information, click here.</a>");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Desktop desktop = java.awt.Desktop.getDesktop();
                try {
                    desktop.browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        label.setCursor(new java.awt.Cursor(Cursor.HAND_CURSOR));
        Object[] messages = {text,label};
        JOptionPane.showMessageDialog(frame, messages);
    }

    private void batchRandomizationSettingsDialog() {
        BatchRandomizationSettingsDialog dlg = new BatchRandomizationSettingsDialog(frame, batchRandomizationSettings);
        batchRandomizationSettings = dlg.getCurrentSettings();
        attemptWriteConfig();
    }

    // This is only intended to be used with the "Keep Game Loaded After Randomizing" setting or between randomization
    // iterations when batch randomization is enabled. It assumes that the game has already been loaded once, and we just need
    // to reload the same game to reinitialize the RomHandler. Don't use this for other purposes unless you know what
    // you're doing.
    private void reinitializeRomHandler(boolean batchRandomization) {
        File romFile = new File(romHandler.loadedFilename());
        Thread t = openRom(romFile, true);
        if (batchRandomization) {
            try {
                t.join();
            } catch (InterruptedException e) {
                attemptToLogException(e, "GUI.loadROM.loadFailedDialog.message", "GUI.loadROM.loadFailedNoLogDialog.message", null, null);
            }
        }
    }

    private void restoreStateFromSettings(SettingsManager settings) {
        // keeping this block temporarily, to be able to look at it in case it holds some secrets
        // TODO: clear this whole block.
        /*
        limitPokemonCheckBox.setSelected(settings.isLimitPokemon());
        currentRestrictions = settings.getCurrentRestrictions();
        if (currentRestrictions != null) {
            currentRestrictions.limitToGen(romHandler.generationOfPokemon());
        }
        noIrregularAltFormesCheckBox.setSelected(settings.isBanIrregularAltFormes());
        noPrematureEvosCheckbox.setSelected(settings.isBanPrematureEvos());
        raceModeCheckBox.setSelected(settings.isRaceMode());
        coRandomIntroMonCheckBox.setSelected(!settings.isRandomizeIntroMon());

        peChangeImpossibleEvosCheckBox.setSelected(settings.isChangeImpossibleEvolutions());
        peUseEstimatedInsteadOfHardcodedLevelsCheckBox.setSelected(settings.useEstimatedLevelsForEvolutionImprovements());
        mdUpdateMovesCheckBox.setSelected(settings.isUpdateMoves());
        mdUpdateComboBox.setSelectedIndex(Math.max(0,settings.getUpdateMovesToGeneration() - (romHandler.generationOfPokemon()+1)));
        coRandomizeTrainerNamesCheckBox.setSelected(settings.isRandomizeTrainerNames());
        coRandomizeTrainerClassNamesCheckBox.setSelected(settings.isRandomizeTrainerClassNames());
        stForceDualTypeCheckBox.setSelected(settings.isDualTypeOnly());

        sbstRandomBuffNerfRadioButton.setSelected(settings.getBSTMod() == SettingsManager.BSTMod.RANDOM_BUFF_NERF);
        sbstShuffleRadioButton.setSelected(settings.getBSTMod() == SettingsManager.BSTMod.SHUFFLE);
        sbstRandomRadioButton.setSelected(settings.getBSTMod() == SettingsManager.BSTMod.RANDOM);
        sbstFollowEvolutionsCheckBox.setSelected(settings.isBSTFollowEvolutions());
        sbstSwapLegendariesCheckBox.setSelected(settings.isBSTShuffleSwapLegendaries());
        sbstRandomBuffNerfSpinSlider.setValue(settings.getBSTBuffNerfMaxPercentage());

        sbsdRandomRadioButton.setSelected(settings.getBaseStatisticsMod() == SettingsManager.BaseStatisticsMod.RANDOM);
        sbsdShuffleRadioButton.setSelected(settings.getBaseStatisticsMod() == SettingsManager.BaseStatisticsMod.SHUFFLE);
        sbsdUnchangedRadioButton.setSelected(settings.getBaseStatisticsMod() == SettingsManager.BaseStatisticsMod.UNCHANGED);
        sbsdFollowEvolutionsCheckBox.setSelected(settings.isBaseStatsFollowEvolutions());
        sbsUpdateBaseStatsCheckBox.setSelected(settings.isUpdateBaseStats());
        pbsUpdateComboBox.setSelectedIndex(Math.max(0,settings.getUpdateBaseStatsToGeneration() - (Math.max(6,romHandler.generationOfPokemon()+1))));
        secStandardizeEXPCurvesCheckBox.setSelected(settings.isStandardizeEXPCurves());
        secLegendariesSlowRadioButton.setSelected(settings.getExpCurveMod() == SettingsManager.ExpCurveMod.LEGENDARIES);
        secStrongLegendariesSlowRadioButton.setSelected(settings.getExpCurveMod() == SettingsManager.ExpCurveMod.STRONG_LEGENDARIES);
        secAllSpeciesRadioButton.setSelected(settings.getExpCurveMod() == SettingsManager.ExpCurveMod.ALL);
        ExpCurve[] expCurves = romHandler.getExpCurves();
        int index = 0;
        for (int i = 0; i < expCurves.length; i++) {
            if (expCurves[i] == settings.getSelectedEXPCurve()) {
                index = i;
            }
        }
        secEXPCurveComboBox.setSelectedIndex(index);
        sbsdFollowMegaEvosCheckBox.setSelected(settings.isBaseStatsFollowMegaEvolutions());
        sbsdAssignEvoStatsRandomlyCheckBox.setSelected(settings.isAssignEvoStatsRandomly());

        saUnchangedRadioButton.setSelected(settings.getAbilitiesMod() == SettingsManager.AbilitiesMod.UNCHANGED);
        saRandomRadioButton.setSelected(settings.getAbilitiesMod() == SettingsManager.AbilitiesMod.RANDOMIZE);
        paAllowWonderGuardCheckBox.setSelected(settings.isAllowWonderGuard());
        saFollowEvolutionsCheckBox.setSelected(settings.isAbilitiesFollowEvolutions());
        saBanTrappingAbilitiesCheckBox.setSelected(settings.isBanTrappingAbilities());
        saBanNegativeAbilitiesCheckBox.setSelected(settings.isBanNegativeAbilities());
        paBadAbilitiesCheckBox.setSelected(settings.isBanBadAbilities());
        saFollowMegaEvosCheckBox.setSelected(settings.isAbilitiesFollowMegaEvolutions());
        saWeighDuplicatesTogetherCheckBox.setSelected(settings.isWeighDuplicateAbilitiesTogether());
        saForceTwoAbilitiesCheckbox.setSelected(settings.isEnsureTwoAbilities());

        stRandomFollowEvolutionsRadioButton.setSelected(settings.getSpeciesTypesMod() == SettingsManager.SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS);
        stRandomCompletelyRadioButton.setSelected(settings.getSpeciesTypesMod() == SettingsManager.SpeciesTypesMod.COMPLETELY_RANDOM);
        stUnchangedRadioButton.setSelected(settings.getSpeciesTypesMod() == SettingsManager.SpeciesTypesMod.UNCHANGED);
        stFollowMegaEvosCheckBox.setSelected(settings.isTypesFollowMegaEvolutions());

        peMakeEvolutionsEasierCheckBox.setSelected(settings.isMakeEvolutionsEasier());
        peMakeEvolutionsEasierLvlSlider.setValue(
                Math.min(settings.getMakeEvolutionsEasierLvl(), romHandler.getHighestEvoLvl()));
        peRemoveTimeBasedEvolutionsCheckBox.setSelected(settings.isRemoveTimeBasedEvolutions());

        spCustomRadioButton.setSelected(settings.getStartersMod() == SettingsManager.StartersMod.CUSTOM);
        spRandomRadioButton.setSelected(settings.getStartersMod() == SettingsManager.StartersMod.COMPLETELY_RANDOM);
        spUnchangedRadioButton.setSelected(settings.getStartersMod() == SettingsManager.StartersMod.UNCHANGED);
        spRandomTwoEvosRadioButton.setSelected(settings.getStartersMod() == SettingsManager.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS);
        spRandomBasicRadioButton.setSelected(settings.getStartersMod() == SettingsManager.StartersMod.RANDOM_BASIC);
        spTypeNoneRadioButton.setSelected(settings.getStartersTypeMod() == SettingsManager.StartersTypeMod.NONE);
        spTypeFwgRadioButton.setSelected(settings.getStartersTypeMod() == SettingsManager.StartersTypeMod.FIRE_WATER_GRASS);
        spTypeTriangleRadioButton.setSelected(settings.getStartersTypeMod() == SettingsManager.StartersTypeMod.TRIANGLE);
        spTypeUniqueRadioButton.setSelected(settings.getStartersTypeMod() == SettingsManager.StartersTypeMod.UNIQUE);
        spTypeSingleRadioButton.setSelected(settings.getStartersTypeMod() == SettingsManager.StartersTypeMod.SINGLE_TYPE);
        if(settings.getStartersSingleType() == null) {
            spTypeSingleComboBox.setSelectedIndex(0);
        } else {
            spTypeSingleComboBox.setSelectedIndex(settings.getStartersSingleType().toInt() + 1);
        }
        spTypeNoDualCheckbox.setSelected(settings.isStartersNoDualTypes());
        spRandomizeStarterHeldItemsCheckBox.setSelected(settings.isRandomizeStartersHeldItems());
        spBanMinorItemsCheckBox.setSelected(settings.isBanBadRandomStarterHeldItems());
        spAllowAltFormesCheckBox.setSelected(settings.isAllowStarterAltFormes());
        spNoLegendariesCheckBox.setSelected(settings.isStartersNoLegendaries());
        if(settings.getStartersBSTMinimum() != 0) {
            spBSTMinimumCheckbox.setSelected(true);
            spBSTMinimumSpinner.setValue(settings.getStartersBSTMinimum());
        } else {
            spBSTMinimumCheckbox.setSelected(false);
        }
        if(settings.getStartersBSTMaximum() != 0) {
            spBSTMaximumCheckbox.setSelected(true);
            spBSTMaximumSpinner.setValue(settings.getStartersBSTMaximum());
        } else {
            spBSTMaximumCheckbox.setSelected(false);
        }

        int[] customStarters = settings.getCustomStarters();
        spCustom1ComboBox.setSelectedIndex(customStarters[0]);
        spCustom2ComboBox.setSelectedIndex(customStarters[1]);
        if (!this.romHandler.isYellow()) { // Yellow only has two starters
            spCustom3ComboBox.setSelectedIndex(customStarters[2]);
        }

        peUnchangedRadioButton.setSelected(settings.getEvolutionsMod() == SettingsManager.EvolutionsMod.UNCHANGED);
        peRandomRadioButton.setSelected(settings.getEvolutionsMod() == SettingsManager.EvolutionsMod.RANDOM);
        peRandomEveryLevelRadioButton.setSelected(settings.getEvolutionsMod() == SettingsManager.EvolutionsMod.RANDOM_EVERY_LEVEL);
        peSimilarStrengthCheckBox.setSelected(settings.isEvosSimilarStrength());
        peSameTypingCheckBox.setSelected(settings.isEvosSameTyping());
        peLimitEvolutionsToThreeCheckBox.setSelected(settings.isEvosMaxThreeStages());
        peForceChangeCheckBox.setSelected(settings.isEvosForceChange());
        peAllowAltFormesCheckBox.setSelected(settings.isEvosAllowAltFormes());
        peForceGrowthCheckBox.setSelected(settings.isEvosForceGrowth());
        peNoConvergenceCheckBox.setSelected(settings.isEvosNoConvergence());
        peAdjustLevelsCheckBox.setSelected(settings.isAdjustEvolutionLevels());

        mdRandomizeMoveAccuracyCheckBox.setSelected(settings.isRandomizeMoveAccuracies());
        mdRandomizeMoveCategoryCheckBox.setSelected(settings.isRandomizeMoveCategory());
        mdRandomizeMovePowerCheckBox.setSelected(settings.isRandomizeMovePowers());
        mdRandomizeMovePPCheckBox.setSelected(settings.isRandomizeMovePPs());
        mdRandomizeMoveTypesCheckBox.setSelected(settings.isRandomizeMoveTypes());
        mdRandomizeMoveNamesCheckBox.setSelected(settings.isRandomizeMoveNames());

        pmsRandomCompletelyRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.COMPLETELY_RANDOM);
        pmsRandomPreferringSameTypeRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.RANDOM_PREFER_SAME_TYPE);
        pmsUnchangedRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.UNCHANGED);
        pmsMetronomeOnlyModeRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.METRONOME_ONLY);
        pmsGuaranteedLevel1MovesCheckBox.setSelected(settings.isStartWithGuaranteedMoves());
        pmsGuaranteedLevel1MovesSlider.setValue(settings.getGuaranteedMoveCount());
        pmsReorderDamagingMovesCheckBox.setSelected(settings.isReorderDamagingMoves());
        pmsForceGoodDamagingCheckBox.setSelected(settings.isMovesetsForceGoodDamaging());
        pmsForceGoodDamagingSpinSlider.setValue(settings.getMovesetsGoodDamagingPercent());
        pmsNoGameBreakingMovesCheckBox.setSelected(settings.isBlockBrokenMovesetMoves());
        pmsEvolutionMovesCheckBox.setSelected(settings.isEvolutionMovesForAll());

        tpSimilarStrengthCheckBox.setSelected(settings.isTrainersUsePokemonOfSimilarStrength());
        tpAvoidDuplicatesCheckBox.setSelected(settings.isTrainersAvoidDuplicates());
        tpComboBox.setSelectedItem(trainerSettings.get(settings.getTrainersMod().ordinal()));
        tpRivalCarriesStarterCheckBox.setSelected(settings.isRivalCarriesStarterThroughout());
        tpWeightTypesCheckBox.setSelected(settings.isTrainersMatchTypingDistribution());
        tpDontUseLegendariesCheckBox.setSelected(settings.isTrainersBlockLegendaries());
        tpUseLocalPokemonCheckBox.setSelected(settings.isTrainersUseLocalPokemon());
        tpNoEarlyWonderGuardCheckBox.setSelected(settings.isTrainersBlockEarlyWonderGuard());
        tpTrainersEvolveTheirPokemonCheckbox.setSelected(settings.isTrainersEvolveTheirPokemon());
        tpPercentageEvolutionLevelModifierSpinSlider.setValue(settings.getTrainersEvolutionLevelModifier());
        tpPercentageLevelModifierCheckBox.setSelected(settings.isTrainersLevelModified());
        tpPercentageLevelModifierSpinSlider.setValue(settings.getTrainersLevelModifier());
        tpEliteFourUniquePokemonCheckBox.setSelected(settings.getEliteFourUniquePokemonNumber() > 0);
        tpEliteFourUniquePokemonSpinner.setValue(settings.getEliteFourUniquePokemonNumber() > 0 ? settings.getEliteFourUniquePokemonNumber() : 1);
        tpAllowAlternateFormesCheckBox.setSelected(settings.isAllowTrainerAlternateFormes());
        tpSwapMegaEvosCheckBox.setSelected(settings.isSwapTrainerMegaEvos());
        tbsUnchangedStyleRadioButton.setSelected(settings.getBattleStyle().getModification() == BattleStyle.Modification.UNCHANGED);
        tbsRandomStyleRadioButton.setSelected(settings.getBattleStyle().getModification() == BattleStyle.Modification.RANDOM);
        tpSingleStyleRadioButton.setSelected(settings.getBattleStyle().getModification() == BattleStyle.Modification.SINGLE_STYLE);
        tpBattleStyleCombobox.setSelectedItem(selectableBattleStyles.get(settings.getBattleStyle().getStyle().ordinal()));
        tpBossTrainersCheckBox.setSelected(settings.getAdditionalBossTrainerPokemon() > 0);
        tpBossTrainersSpinner.setValue(settings.getAdditionalBossTrainerPokemon() > 0 ? settings.getAdditionalBossTrainerPokemon() : 1);
        tpImportantTrainersCheckBox.setSelected(settings.getAdditionalImportantTrainerPokemon() > 0);
        tpImportantTrainersSpinner.setValue(settings.getAdditionalImportantTrainerPokemon() > 0 ? settings.getAdditionalImportantTrainerPokemon() : 1);
        tpRegularTrainersCheckBox.setSelected(settings.getAdditionalRegularTrainerPokemon() > 0);
        tpRegularTrainersSpinner.setValue(settings.getAdditionalRegularTrainerPokemon() > 0 ? settings.getAdditionalRegularTrainerPokemon() : 1);
        tpBossTrainersItemsCheckBox.setSelected(settings.isRandomizeHeldItemsForBossTrainerPokemon());
        tpImportantTrainersItemsCheckBox.setSelected(settings.isRandomizeHeldItemsForImportantTrainerPokemon());
        tpRegularTrainersItemsCheckBox.setSelected(settings.isRandomizeHeldItemsForRegularTrainerPokemon());
        tpConsumableItemsOnlyCheckBox.setSelected(settings.isConsumableItemsOnlyForTrainers());
        tpSensibleItemsCheckBox.setSelected(settings.isSensibleItemsOnlyForTrainers());
        tpHighestLevelGetsItemCheckBox.setSelected(settings.isHighestLevelGetsItemsForTrainers());
        tpBossTrainersTypeDiversityCheckBox.setSelected(settings.isDiverseTypesForBossTrainers());
        tpImportantTrainersTypeDiversityCheckBox.setSelected(settings.isDiverseTypesForImportantTrainers());
        tpRegularTrainersTypeDiversityCheckBox.setSelected(settings.isDiverseTypesForRegularTrainers());

        tpRandomShinyTrainerPokemonCheckBox.setSelected(settings.isShinyChance());
        tpBetterMovesetsBossTrainersCheckBox.setSelected(settings.isBetterBossTrainerMovesets());
        tpBetterMovesetsImportantTrainersCheckBox.setSelected(settings.isBetterImportantTrainerMovesets());
        tpBetterMovesetsRegularTrainersCheckBox.setSelected(settings.isBetterRegularTrainerMovesets());

        totpUnchangedRadioButton.setSelected(settings.getTotemPokemonMod() == SettingsManager.TotemPokemonMod.UNCHANGED);
        totpRandomRadioButton.setSelected(settings.getTotemPokemonMod() == SettingsManager.TotemPokemonMod.RANDOM);
        totpRandomSimilarStrengthRadioButton.setSelected(settings.getTotemPokemonMod() == SettingsManager.TotemPokemonMod.SIMILAR_STRENGTH);
        totpAllyUnchangedRadioButton.setSelected(settings.getAllyPokemonMod() == SettingsManager.AllyPokemonMod.UNCHANGED);
        totpAllyRandomRadioButton.setSelected(settings.getAllyPokemonMod() == SettingsManager.AllyPokemonMod.RANDOM);
        totpAllyRandomSimilarStrengthRadioButton.setSelected(settings.getAllyPokemonMod() == SettingsManager.AllyPokemonMod.SIMILAR_STRENGTH);
        totpAuraUnchangedRadioButton.setSelected(settings.getAuraMod() == SettingsManager.AuraMod.UNCHANGED);
        totpAuraRandomRadioButton.setSelected(settings.getAuraMod() == SettingsManager.AuraMod.RANDOM);
        totpAuraRandomSameStrengthRadioButton.setSelected(settings.getAuraMod() == SettingsManager.AuraMod.SAME_STRENGTH);
        totpRandomizeHeldItemsCheckBox.setSelected(settings.isRandomizeTotemHeldItems());
        totpAllowAltFormesCheckBox.setSelected(settings.isAllowTotemAltFormes());
        totpPercentageLevelModifierCheckBox.setSelected(settings.isTotemLevelsModified());
        totpPercentageLevelModifierSpinSlider.setValue(settings.getTotemLevelModifier());

        wpRandomizeWildPokemonCheckBox.setSelected(settings.isRandomizeWildPokemon());

        wpZoneNoneRadioButton.setSelected(settings.getWildPokemonZoneMod() == SettingsManager.WildPokemonZoneMod.NONE);
        wpZoneEncounterSetRadioButton.setSelected(settings.getWildPokemonZoneMod() == SettingsManager.WildPokemonZoneMod.ENCOUNTER_SET);
        wpZoneMapRadioButton.setSelected(settings.getWildPokemonZoneMod() == SettingsManager.WildPokemonZoneMod.MAP);
        wpZoneNamedLocationRadioButton.setSelected(settings.getWildPokemonZoneMod() == SettingsManager.WildPokemonZoneMod.NAMED_LOCATION);
        wpZoneGameRadioButton.setSelected(settings.getWildPokemonZoneMod() == SettingsManager.WildPokemonZoneMod.GAME);
        wpSplitByEncounterTypesCheckBox.setSelected(settings.isSplitWildZoneByEncounterTypes());

        wpTRNoneRadioButton.setSelected(settings.getWildPokemonTypeMod() == SettingsManager.WildPokemonTypeMod.NONE);
        wpTRThemedAreasRadioButton.setSelected(settings.getWildPokemonTypeMod() == SettingsManager.WildPokemonTypeMod.RANDOM_THEMES);
        wpTRKeepPrimaryRadioButton.setSelected(settings.getWildPokemonTypeMod() == SettingsManager.WildPokemonTypeMod.KEEP_PRIMARY);
        wpTRKeepThemesCheckBox.setSelected(settings.isKeepWildTypeThemes());

        wpERNoneRadioButton.setSelected(settings.getWildPokemonEvolutionMod() == SettingsManager.WildPokemonEvolutionMod.NONE);
        wpERBasicOnlyRadioButton.setSelected(settings.getWildPokemonEvolutionMod() ==
                SettingsManager.WildPokemonEvolutionMod.BASIC_ONLY);
        wpERSameEvolutionStageRadioButton.setSelected(settings.getWildPokemonEvolutionMod() ==
                SettingsManager.WildPokemonEvolutionMod.KEEP_STAGE);
        wpERKeepEvolutionsCheckBox.setSelected(settings.isKeepWildEvolutionFamilies());

        wpCatchEmAllModeCheckBox.setSelected(settings.isCatchEmAllEncounters());
        wpSimilarStrengthCheckBox.setSelected(settings.isSimilarStrengthEncounters());

        wpRemoveTimeBasedEncountersCheckBox.setSelected(settings.isUseTimeBasedEncounters());
        wpSetMinimumCatchRateCheckBox.setSelected(settings.isUseMinimumCatchRate());
        wpSetMinimumCatchRateSlider.setValue(settings.getMinimumCatchRateLevel());
        wpDontUseLegendariesCheckBox.setSelected(settings.isBlockWildLegendaries());
        wpRandomizeHeldItemsCheckBox.setSelected(settings.isRandomizeWildPokemonHeldItems());
        wpBanBadItemsCheckBox.setSelected(settings.isBanBadRandomWildPokemonHeldItems());
        wpBalanceShakingGrassPokemonCheckBox.setSelected(settings.isBalanceShakingGrass());
        wpPercentageLevelModifierCheckBox.setSelected(settings.isWildLevelsModified());
        wpPercentageLevelModifierSpinSlider.setValue(settings.getWildLevelModifier());
        wpAllowAltFormesCheckBox.setSelected(settings.isAllowWildAltFormes());

        stpUnchangedRadioButton.setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.UNCHANGED);
        stpSwapLegendariesSwapStandardsRadioButton.setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.RANDOM_MATCHING);
        stpRandomCompletelyRadioButton
                .setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.COMPLETELY_RANDOM);
        stpRandomSimilarStrengthRadioButton
                .setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.SIMILAR_STRENGTH);
        stpLimitMainGameLegendariesCheckBox.setSelected(settings.isLimitMainGameLegendaries());
        stpRandomize600BSTCheckBox.setSelected(settings.isLimit600());
        stpAllowAltFormesCheckBox.setSelected(settings.isAllowStaticAltFormes());
        stpSwapMegaEvosCheckBox.setSelected(settings.isSwapStaticMegaEvos());
        stpPercentageLevelModifierCheckBox.setSelected(settings.isStaticLevelModified());
        stpPercentageLevelModifierSpinSlider.setValue(settings.getStaticLevelModifier());
        stpFixMusicCheckBox.setSelected(settings.isCorrectStaticMusic());

        thcRandomCompletelyRadioButton
                .setSelected(settings.getTmsHmsCompatibilityMod() == SettingsManager.TMsHMsCompatibilityMod.COMPLETELY_RANDOM);
        thcRandomPreferSameTypeRadioButton
                .setSelected(settings.getTmsHmsCompatibilityMod() == SettingsManager.TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE);
        thcUnchangedRadioButton
                .setSelected(settings.getTmsHmsCompatibilityMod() == SettingsManager.TMsHMsCompatibilityMod.UNCHANGED);
        tmmRandomRadioButton.setSelected(settings.getTmsMod() == SettingsManager.TMsMod.RANDOM);
        tmmUnchangedRadioButton.setSelected(settings.getTmsMod() == SettingsManager.TMsMod.UNCHANGED);
        thcLevelupMoveSanityCheckBox.setSelected(settings.isTmLevelUpMoveSanity());
        tmmKeepFieldMoveTMsCheckBox.setSelected(settings.isKeepFieldMoveTMs());
        thcFullCompatibilityRadioButton.setSelected(settings.getTmsHmsCompatibilityMod() == SettingsManager.TMsHMsCompatibilityMod.FULL);
        thcFullHMCompatibilityCheckBox.setSelected(settings.isFullHMCompat());
        tmmForceGoodDamagingCheckBox.setSelected(settings.isTmsForceGoodDamaging());
        tmmForceGoodDamagingSpinSlider.setValue(settings.getTmsGoodDamagingPercent());
        tmmNoGameBreakingMovesCheckBox.setSelected(settings.isBlockBrokenTMMoves());
        thcFollowEvolutionsCheckBox.setSelected(settings.isTmsFollowEvolutions());

        mtcRandomCompletelyRadioButton
                .setSelected(settings.getMoveTutorsCompatibilityMod() == SettingsManager.MoveTutorsCompatibilityMod.COMPLETELY_RANDOM);
        mtcRandomPreferSameTypeRadioButton
                .setSelected(settings.getMoveTutorsCompatibilityMod() == SettingsManager.MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE);
        mtcUnchangedRadioButton
                .setSelected(settings.getMoveTutorsCompatibilityMod() == SettingsManager.MoveTutorsCompatibilityMod.UNCHANGED);
        mtmRandomRadioButton.setSelected(settings.getMoveTutorMovesMod() == SettingsManager.MoveTutorMovesMod.RANDOM);
        mtmUnchangedRadioButton.setSelected(settings.getMoveTutorMovesMod() == SettingsManager.MoveTutorMovesMod.UNCHANGED);
        mtcLevelupMoveSanityCheckBox.setSelected(settings.isTutorLevelUpMoveSanity());
        mtmKeepFieldMoveTutorsCheckBox.setSelected(settings.isKeepFieldMoveTutors());
        mtcFullCompatibilityRadioButton
                .setSelected(settings.getMoveTutorsCompatibilityMod() == SettingsManager.MoveTutorsCompatibilityMod.FULL);
        mtmForceGoodDamagingCheckBox.setSelected(settings.isTutorsForceGoodDamaging());
        mtmForceGoodDamagingSpinSlider.setValue(settings.getTutorsGoodDamagingPercent());
        mtmNoGameBreakingMovesCheckBox.setSelected(settings.isBlockBrokenTutorMoves());
        mtcFollowEvolutionsCheckBox.setSelected(settings.isTutorFollowEvolutions());

        igtRandomizeBothRadioButton
                .setSelected(settings.getInGameTradesMod() == SettingsManager.InGameTradesMod.RANDOMIZE_GIVEN_AND_REQUESTED);
        igtRandomizeGivenRadioButton.setSelected(settings.getInGameTradesMod() == SettingsManager.InGameTradesMod.RANDOMIZE_GIVEN);
        igtRandomizeItemsCheckBox.setSelected(settings.isRandomizeInGameTradesItems());
        igtRandomizeIVsCheckBox.setSelected(settings.isRandomizeInGameTradesIVs());
        igtRandomizeNicknamesCheckBox.setSelected(settings.isRandomizeInGameTradesNicknames());
        igtRandomizeOTsCheckBox.setSelected(settings.isRandomizeInGameTradesOTs());
        igtUnchangedRadioButton.setSelected(settings.getInGameTradesMod() == SettingsManager.InGameTradesMod.UNCHANGED);

        fiRandomRadioButton.setSelected(settings.getFieldItemsMod() == SettingsManager.FieldItemsMod.RANDOM);
        fiRandomEvenDistributionRadioButton.setSelected(settings.getFieldItemsMod() == SettingsManager.FieldItemsMod.RANDOM_EVEN);
        fiShuffleRadioButton.setSelected(settings.getFieldItemsMod() == SettingsManager.FieldItemsMod.SHUFFLE);
        fiUnchangedRadioButton.setSelected(settings.getFieldItemsMod() == SettingsManager.FieldItemsMod.UNCHANGED);
        fiBanBadItemsCheckBox.setSelected(settings.isBanBadRandomFieldItems());

        shRandomRadioButton.setSelected(settings.getShopItemsMod() == SettingsManager.ShopItemsMod.RANDOM);
        shShuffleRadioButton.setSelected(settings.getShopItemsMod() == SettingsManager.ShopItemsMod.SHUFFLE);
        shUnchangedRadioButton.setSelected(settings.getShopItemsMod() == SettingsManager.ShopItemsMod.UNCHANGED);
        shBanBadItemsCheckBox.setSelected(settings.isBanBadRandomShopItems());
        shBanRegularShopItemsCheckBox.setSelected(settings.isBanRegularShopItems());
        shBanOverpoweredShopItemsCheckBox.setSelected(settings.isBanOPShopItems());
        shGuaranteeEvolutionItemsCheckBox.setSelected(settings.isGuaranteeEvolutionItems());
        shGuaranteeXItemsCheckBox.setSelected(settings.isGuaranteeXItems());
        shBalanceShopItemPricesCheckBox.setSelected(settings.isBalanceShopPrices());
        shAddRareCandyCheckBox.setSelected(settings.isAddCheapRareCandiesToShops());

        puUnchangedRadioButton.setSelected(settings.getPickupItemsMod() == SettingsManager.PickupItemsMod.UNCHANGED);
        puRandomRadioButton.setSelected(settings.getPickupItemsMod() == SettingsManager.PickupItemsMod.RANDOM);
        puBanBadItemsCheckBox.setSelected(settings.isBanBadRandomPickupItems());

        teUnchangedRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.UNCHANGED);
        teRandomRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.RANDOM);
        teRandomBalancedRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.RANDOM_BALANCED);
        teKeepTypeIdentitiesRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.KEEP_IDENTITIES);
        teInverseRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.INVERSE);
        teAddRandomImmunitiesCheckBox.setSelected(settings.isInverseTypesRandomImmunities());
        teUpdateCheckbox.setSelected(settings.isUpdateTypeEffectiveness());

        ppalUnchangedRadioButton.setSelected(settings.getPokemonPalettesMod() == SettingsManager.PokemonPalettesMod.UNCHANGED);
        ppalRandomRadioButton.setSelected(settings.getPokemonPalettesMod() == SettingsManager.PokemonPalettesMod.RANDOM);
        ppalFollowTypesCheckBox.setSelected(settings.isPokemonPalettesFollowTypes());
        ppalFollowEvolutionsCheckBox.setSelected(settings.isPokemonPalettesFollowEvolutions());
        ppalShinyFromNormalCheckBox.setSelected(settings.isPokemonPalettesShinyFromNormal());

        int mtsSelected = settings.getCurrentMiscTweaks();
        int mtCount = MiscTweak.allTweaks.size();

        for (int mti = 0; mti < mtCount; mti++) {
            MiscTweak mt = MiscTweak.allTweaks.get(mti);
            JCheckBox mtCB = tweakCheckBoxes.get(mti);
            mtCB.setSelected((mtsSelected & mt.getValue()) != 0);
        }

        this.enableOrDisableSubControls();
        **/
    }

    private void attemptToLogException(Exception ex, String baseMessageKey, String noLogMessageKey,
                                       String settingsString, String seedString) {
        attemptToLogException(ex, baseMessageKey, noLogMessageKey, false, settingsString, seedString);
    }

    private void attemptToLogException(Exception ex, String baseMessageKey, String noLogMessageKey, boolean showMessage,
                                       String settingsString, String seedString) {

        // Make sure the operation dialog doesn't show up over the error
        // dialog
        SwingUtilities.invokeLater(() -> RandomizerGUI.this.opDialog.setVisible(false));

        Date now = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        try {
            String errlog = "error_" + ft.format(now) + ".txt";
            PrintStream ps = new PrintStream(new FileOutputStream(errlog));
            ps.println("Randomizer Version: " + Version.LATEST.name);
            if (seedString != null) {
                ps.println("Seed: " + seedString);
            }
            if (settingsString != null) {
                ps.println("Settings String: " + settingsString);
            }
            ps.println("Java Version: " + System.getProperty("java.version") + ", " + System.getProperty("java.vm.name"));
            PrintStream e1 = System.err;
            System.setErr(ps);
            if (this.romHandler != null) {
                try {
                    ps.println("ROM: " + romHandler.getROMName());
                    ps.println("Code: " + romHandler.getROMCode());
                    ps.println("Reported Support Level: " + romHandler.getSupportLevel());
                    ps.println();
                } catch (Exception ex2) {
                    // Do nothing, just don't fail
                }
            }
            ex.printStackTrace();
            ps.println();
            ps.println("--ROM Diagnostics--");
            if (!romHandler.isRomValid(null)) {
                ps.println(bundle.getString("Log.InvalidRomLoaded"));
            }
            romHandler.printRomDiagnostics(ps);
            System.setErr(e1);
            ps.close();
            if (showMessage) {
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString(baseMessageKey), ex.getMessage(), errlog));
            } else {
                JOptionPane.showMessageDialog(mainPanel, String.format(bundle.getString(baseMessageKey), errlog));
            }
        } catch (Exception logex) {
            if (showMessage) {
                JOptionPane.showMessageDialog(mainPanel, String.format(bundle.getString(noLogMessageKey), ex.getMessage()));
            } else {
                JOptionPane.showMessageDialog(mainPanel, bundle.getString(noLogMessageKey));
            }
        }
    }

    private void initialState() {

        romNameLabel.setText(bundle.getString("GUI.header.romInformationPanel.noRomLoaded"));
        romCodeLabel.setText("");
        romSupportLabel.setText("");

        gameMascotLabel.setIcon(emptyIcon);

        setInitialButtonState(
                lsNoIrregularAltFormesCheckBox, lsNoPrematureEvosCheckbox, raceModeCheckBox, coRandomIntroMonCheckBox);

        currentRestrictions = null;

        setInitialButtonState(openROMButton, randomizeSaveButton, premadeSeedButton, settingsButton,
                loadSettingsButton, saveSettingsButton);
        enableButtons(openROMButton, randomizeSaveButton, premadeSeedButton, settingsButton);

        // the buttons in the main part of the gui (randomization options):

        setInitialButtonState(sbstUnchangedRadioButton, sbstRandomBuffNerfRadioButton, sbstShuffleRadioButton,
                sbstRandomRadioButton, sbstFollowEvolutionsCheckBox, sbstSwapLegendariesCheckBox);
        spCustom1ComboBox.setEnabled(false);
        sbstRandomBuffNerfSpinSlider.setEnabled(false);
        sbstRandomBuffNerfSpinSlider.setValue(0);

        setInitialButtonState(sbsdUnchangedRadioButton, sbsdShuffleRadioButton, sbsdRandomRadioButton,
                secLegendariesSlowRadioButton, secStrongLegendariesSlowRadioButton, secAllSpeciesRadioButton,
                secStandardizeEXPCurvesCheckBox, sbsdFollowEvolutionsCheckBox, sbsUpdateBaseStatsCheckBox,
                sbsdFollowMegaEvosCheckBox, sbsdAssignEvoStatsRandomlyCheckBox);
		secEXPCurveComboBox.setVisible(true);
		secEXPCurveComboBox.setEnabled(false);
		secEXPCurveComboBox.setSelectedIndex(0);
		secEXPCurveComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "Medium Fast" }));

        setInitialButtonState(stUnchangedRadioButton, stRandomFollowEvolutionsRadioButton, stRandomCompletelyRadioButton,
                stFollowMegaEvosCheckBox, stForceDualTypeCheckBox);

		speciesAbilitiesPanel.setVisible(true);
        setInitialButtonState(saUnchangedRadioButton, saRandomRadioButton, saBanWonderGuardCheckBox,
                saFollowEvolutionsCheckBox, saBanTrappingAbilitiesCheckBox, saBanNegativeAbilitiesCheckBox,
                saBanMinorAbilitiesCheckBox, saFollowMegaEvosCheckBox, saWeighDuplicatesTogetherCheckBox,
                saForceTwoAbilitiesCheckbox);

        setInitialButtonState(peUnchangedRadioButton, peRandomRadioButton, peRandomEveryLevelRadioButton,
				peSimilarStrengthCheckBox, peSameTypingCheckBox, peLimitEvolutionsToThreeCheckBox,
				peForceChangeCheckBox, peChangeImpossibleEvosCheckBox, peMakeEvolutionsEasierCheckBox,
                peUseEstimatedInsteadOfHardcodedLevelsCheckBox, peRemoveTimeBasedEvolutionsCheckBox,
                peAllowAltFormesCheckBox, peForceGrowthCheckBox, peNoConvergenceCheckBox, peAdjustLevelsCheckBox);
        peMakeEvolutionsEasierLvlSlider.setVisible(true);
        peMakeEvolutionsEasierLvlSlider.setEnabled(false);
        peMakeEvolutionsEasierLvlSlider.setValue(SettingsManager.MAKE_EVOLUTIONS_EASIER_DEFAULT_LVL);

        setInitialButtonState(spUnchangedRadioButton, spCustomRadioButton, spRandomRadioButton,
                spTypeNoneRadioButton, spTypeFwgRadioButton, spTypeTriangleRadioButton,
				spTypeUniqueRadioButton, spTypeSingleRadioButton, spTypeNoDualCheckbox,
                spNoLegendariesCheckBox,
				spRandomizeStarterHeldItemsCheckBox, spBanMinorItemsCheckBox, spAllowAltFormesCheckBox,
                spBSTMinimumCheckbox, spBSTMaximumCheckbox);
		spCustom1ComboBox.setVisible(true);
		spCustom1ComboBox.setEnabled(false);
		spCustom1ComboBox.setSelectedIndex(0);
		spCustom1ComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "--" }));
		spCustom2ComboBox.setVisible(true);
		spCustom2ComboBox.setEnabled(false);
		spCustom2ComboBox.setSelectedIndex(0);
		spCustom2ComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "--" }));
		spCustom3ComboBox.setVisible(true);
		spCustom3ComboBox.setEnabled(false);
		spCustom3ComboBox.setSelectedIndex(0);
		spCustom3ComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "--" }));
        spBSTMinimumSpinner.setVisible(true);
        spBSTMinimumSpinner.setEnabled(false);
        spBSTMinimumSpinner.setValue(0);
        spBSTMaximumSpinner.setVisible(true);
        spBSTMaximumSpinner.setEnabled(false);
        spBSTMaximumSpinner.setValue(0);

        setInitialButtonState(stpUnchangedRadioButton, stpSwapLegendariesSwapStandardsRadioButton,
				stpRandomCompletelyRadioButton, stpRandomSimilarStrengthRadioButton, stpPercentageLevelModifierCheckBox,
				stpLimitMainGameLegendariesCheckBox, stpRandomize600BSTCheckBox, stpAllowAltFormesCheckBox,
				stpSwapMegaEvosCheckBox, stpFixMusicCheckBox);
		stpPercentageLevelModifierSpinSlider.setVisible(true);
		stpPercentageLevelModifierSpinSlider.setEnabled(false);
		stpPercentageLevelModifierSpinSlider.setValue(0);

        setInitialButtonState(igtRandomizeNicknamesCheckBox, igtRandomizeOTsCheckBox,
				igtRandomizeIVsCheckBox, igtRandomizeItemsCheckBox);

        setInitialButtonState(mdRandomizeMovePowerCheckBox, mdRandomizeMoveAccuracyCheckBox, mdRandomizeMovePPCheckBox,
            mdRandomizeMoveTypesCheckBox, mdRandomizeMoveCategoryCheckBox, mdUpdateMovesCheckBox, mdRandomizeMoveNamesCheckBox);
		mdUpdateComboBox.setVisible(true);
		mdUpdateComboBox.setEnabled(false);
		mdUpdateComboBox.setSelectedIndex(0);
		mdUpdateComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "--" }));

        setInitialButtonState(pmsUnchangedRadioButton, pmsRandomPreferringSameTypeRadioButton, pmsRandomCompletelyRadioButton,
				pmsMetronomeOnlyModeRadioButton, pmsGuaranteedLevel1MovesCheckBox, pmsReorderDamagingMovesCheckBox,
				pmsNoGameBreakingMovesCheckBox, pmsForceGoodDamagingCheckBox, pmsEvolutionMovesCheckBox);
		pmsGuaranteedLevel1MovesSlider.setVisible(true);
		pmsGuaranteedLevel1MovesSlider.setEnabled(false);
		pmsGuaranteedLevel1MovesSlider.setValue(pmsGuaranteedLevel1MovesSlider.getMinimum());
		pmsForceGoodDamagingSpinSlider.setVisible(true);
		pmsForceGoodDamagingSpinSlider.setEnabled(false);
		pmsForceGoodDamagingSpinSlider.setValue(pmsForceGoodDamagingSpinSlider.getMinimum());

        setInitialButtonState(tpRivalCarriesStarterCheckBox, tpSimilarStrengthCheckBox, tpAvoidDuplicatesCheckBox,
                tpWeightTypesCheckBox, tpUseLocalPokemonCheckBox,
				tpDontUseLegendariesCheckBox, tpNoEarlyWonderGuardCheckBox, coRandomizeTrainerNamesCheckBox,
                coRandomizeTrainerClassNamesCheckBox,
                tpTrainersEvolveTheirPokemonCheckbox, tpPercentageLevelModifierCheckBox,
				tpEliteFourUniquePokemonCheckBox, tpAllowAlternateFormesCheckBox, tpSwapMegaEvosCheckBox,
				tpBossTrainersCheckBox, tpImportantTrainersCheckBox,
				tpRegularTrainersCheckBox, tpBossTrainersItemsCheckBox, tpImportantTrainersItemsCheckBox,
				tpRegularTrainersItemsCheckBox, tpConsumableItemsOnlyCheckBox, tpSensibleItemsCheckBox,
				tpHighestLevelGetsItemCheckBox, tpRandomShinyTrainerPokemonCheckBox,
                tpBetterMovesetsBossTrainersCheckBox, tpBetterMovesetsImportantTrainersCheckBox,
                tpBetterMovesetsRegularTrainersCheckBox,
                tpBossTrainersTypeDiversityCheckBox, tpImportantTrainersTypeDiversityCheckBox,
                tpRegularTrainersTypeDiversityCheckBox);
		tpPercentageEvolutionLevelModifierSpinSlider.setVisible(true);
		tpPercentageEvolutionLevelModifierSpinSlider.setEnabled(false);
		tpPercentageEvolutionLevelModifierSpinSlider.setValue(0);
        tpCalculatedFullyEvolvedLvlLabel.setVisible(true);
        tpCalculatedFullyEvolvedLvlLabel.setEnabled(false);
        tpCalculatedFullyEvolvedLvlLabel.setText(String.format(bundle.getString("GUI.foeTab.trainersPanel.calculatedFullyEvolvedLvlLabel.text"), "--"));
		tpPercentageLevelModifierSpinSlider.setVisible(true);
		tpPercentageLevelModifierSpinSlider.setEnabled(false);
        tpPercentageLevelModifierSpinSlider.setValue(0);
		tpEliteFourUniquePokemonSpinner.setVisible(true);
		tpEliteFourUniquePokemonSpinner.setEnabled(false);
		tpEliteFourUniquePokemonSpinner.setValue(1);
		tpBossTrainersSpinner.setVisible(true);
		tpBossTrainersSpinner.setEnabled(false);
		tpBossTrainersSpinner.setValue(1);
		tpImportantTrainersSpinner.setVisible(true);
		tpImportantTrainersSpinner.setEnabled(false);
		tpImportantTrainersSpinner.setValue(1);
		tpRegularTrainersSpinner.setVisible(true);
		tpRegularTrainersSpinner.setEnabled(false);
		tpRegularTrainersSpinner.setValue(1);
		tpAdditionalPokemonForLabel.setVisible(true);
		tpHeldItemsLabel.setVisible(true);

        tbsUnchangedStyleRadioButton.setVisible(true);
        tbsUnchangedStyleRadioButton.setEnabled(false);
        tbsUnchangedStyleRadioButton.setSelected(true);
        tbsRandomStyleRadioButton.setVisible(true);
        tbsRandomStyleRadioButton.setEnabled(false);
        tbsRandomStyleRadioButton.setSelected(false);

		totpPanel.setVisible(true);
        setInitialButtonState(totpUnchangedRadioButton, totpRandomRadioButton, totpRandomSimilarStrengthRadioButton,
				totpAllyUnchangedRadioButton, totpAllyRandomRadioButton, totpAllyRandomSimilarStrengthRadioButton,
				totpAuraUnchangedRadioButton, totpAuraRandomRadioButton, totpAuraRandomSameStrengthRadioButton,
				totpPercentageLevelModifierCheckBox, totpRandomizeHeldItemsCheckBox, totpAllowAltFormesCheckBox);
		totpPercentageLevelModifierSpinSlider.setVisible(true);
		totpPercentageLevelModifierSpinSlider.setEnabled(false);
		totpPercentageLevelModifierSpinSlider.setValue(0);

        setInitialButtonState(wpRandomizeWildPokemonCheckBox, wpZoneNoneRadioButton, wpZoneEncounterSetRadioButton,
                wpZoneMapRadioButton, wpZoneNamedLocationRadioButton, wpZoneGameRadioButton,
                wpSplitByEncounterTypesCheckBox,
                wpTRNoneRadioButton, wpTRThemedAreasRadioButton, wpTRKeepPrimaryRadioButton, wpTRKeepThemesCheckBox,
                wpERNoneRadioButton, wpERBasicOnlyRadioButton, wpERSameEvolutionStageRadioButton,
                wpERKeepEvolutionsCheckBox, wpSimilarStrengthCheckBox, wpCatchEmAllModeCheckBox,
                wpRemoveTimeBasedEncountersCheckBox, wpDontUseLegendariesCheckBox, wpSetMinimumCatchRateCheckBox,
                        wpRandomizeHeldItemsCheckBox, wpBanBadItemsCheckBox, wpBalanceShakingGrassPokemonCheckBox,
                        wpPercentageLevelModifierCheckBox, wpAllowAltFormesCheckBox);

        wpRemoveTimeBasedEncountersCheckBox.setSelected(true);
		wpSetMinimumCatchRateSlider.setVisible(true);
		wpSetMinimumCatchRateSlider.setEnabled(false);
		wpSetMinimumCatchRateSlider.setValue(wpSetMinimumCatchRateSlider.getMinimum());
		wpPercentageLevelModifierSpinSlider.setVisible(true);
		wpPercentageLevelModifierSpinSlider.setEnabled(false);
		wpPercentageLevelModifierSpinSlider.setValue(0);

        setInitialButtonState(tmmUnchangedRadioButton, tmmRandomRadioButton, tmmNoGameBreakingMovesCheckBox,
                thcFullHMCompatibilityCheckBox, thcLevelupMoveSanityCheckBox, tmmKeepFieldMoveTMsCheckBox,
                tmmForceGoodDamagingCheckBox, thcFollowEvolutionsCheckBox, thcUnchangedRadioButton,
				thcRandomPreferSameTypeRadioButton, thcRandomCompletelyRadioButton, thcFullCompatibilityRadioButton,
                mtmUnchangedRadioButton, mtmRandomRadioButton, mtmNoGameBreakingMovesCheckBox, mtcLevelupMoveSanityCheckBox,
                mtcLevelupMoveSanityCheckBox, mtmKeepFieldMoveTutorsCheckBox, mtmForceGoodDamagingCheckBox,
                mtcFollowEvolutionsCheckBox, mtcUnchangedRadioButton, mtcRandomPreferSameTypeRadioButton,
				mtcRandomCompletelyRadioButton, mtcFullCompatibilityRadioButton);
		tmmForceGoodDamagingSpinSlider.setVisible(true);
		tmmForceGoodDamagingSpinSlider.setEnabled(false);
		tmmForceGoodDamagingSpinSlider.setValue(tmmForceGoodDamagingSpinSlider.getMinimum());
		mtmForceGoodDamagingSpinSlider.setVisible(true);
		mtmForceGoodDamagingSpinSlider.setEnabled(false);
		mtmForceGoodDamagingSpinSlider.setValue(mtmForceGoodDamagingSpinSlider.getMinimum());

        setInitialButtonState(fiUnchangedRadioButton, fiShuffleRadioButton, fiRandomRadioButton,
				fiRandomEvenDistributionRadioButton, fiBanBadItemsCheckBox, shUnchangedRadioButton,
				shShuffleRadioButton, shRandomRadioButton, shBanOverpoweredShopItemsCheckBox, shBanBadItemsCheckBox,
				shBanRegularShopItemsCheckBox, shBalanceShopItemPricesCheckBox, shGuaranteeEvolutionItemsCheckBox,
				shGuaranteeXItemsCheckBox, shAddRareCandyCheckBox, puUnchangedRadioButton, puRandomRadioButton,
                puBanBadItemsCheckBox);

        setInitialButtonState(teUnchangedRadioButton, teRandomRadioButton, teRandomBalancedRadioButton,
                teKeepTypeIdentitiesRadioButton, teInverseRadioButton, teAddRandomImmunitiesCheckBox,
                teUpdateCheckbox);

        setInitialButtonState(ppalUnchangedRadioButton, ppalRandomRadioButton, ppalFollowTypesCheckBox,
                ppalFollowEvolutionsCheckBox, ppalShinyFromNormalCheckBox,
                        cpgUnchangedRadioButton, cpgCustomRadioButton);
        cpgSelection.setInitialState();

        // TODO: why do these checkboxes exist? can't they just be generated from the MiscTweak objects?
        //Well, this lets them be named variables, which helps for code readability if nothing else...
        setInitialButtonState(btScalingEXPCheckBox, btNerfXAccuracyCheckBox, btUpdateCritRateCheckBox,
                qoltFastestTextCheckBox, qoltRunIndoorsCheckBox, miscRandomizePCPotionCheckBox,
                peAllowPikachuEvolutionCheckBox, qoltNationalDexCheckBox,
                coLowerCaseSpeciesNamesCheckBox, coRandomizeCatchingTutorialCheckBox, btBanLuckyEggCheckBox,
                btNoFreeLuckyEggCheckBox, miscBanBigMoneyManiacCheckBox);

        mtNoExistLabel.setVisible(false);
        qoltNoneAvailableLabel.setVisible(false);
        btNoneAvailableLabel.setVisible(false);
        ppalNotExistLabel.setVisible(false);
        ppalPartiallyImplementedLabel.setVisible(false);
    }

    /**
     * Sets all buttons given to the initial state (visible, disabled, deselected).
     * @param buttons The buttons to set to the initial state.
     */
    private static void setInitialButtonState(AbstractButton... buttons) {
        for(AbstractButton button : buttons ) {
            button.setVisible(true);
            button.setEnabled(false);
            button.setSelected(false);
        }
    }

    /**
     * Disables and deselects each button given.<br>
     * For radio buttons, use disableButtonsWithDefault.
     * @param buttons The buttons to disable and deselect.
     */
    private static void disableAndDeselectButtons(JCheckBox... buttons) {
        for(AbstractButton button : buttons ) {
            button.setEnabled(false);
            button.setSelected(false);
        }
    }

    /**
     * Disables each button given beyond the first.
     * If any disabled button is selected, changes that selection to the default button given.
     * For checkboxes, use disableAndDeselectButtons.
     * @param defaultButton The button to select if a disabled button was selected.
     * @param buttons The buttons to disable.
     */
    private static void disableButtonsWithDefault(JRadioButton defaultButton, JRadioButton... buttons) {
        for(JRadioButton button : buttons) {
            button.setEnabled(false);
            if(button.isSelected()) {
                defaultButton.setSelected(true);
            }
        }
    }

    /**
     * Disables each button given.<br>
     * Works with both radio buttons and checkboxes;
     * however, consider using disableAndDeselectButtons and/or disableButtonsWithDefault instead.
     * @param buttons The buttons to disable.
     */
    private static void disableButtons(AbstractButton... buttons) {
        for(AbstractButton button : buttons ) {
            button.setEnabled(false);
        }
    }

    /**
     * Enables each button given.
     * @param buttons The buttons to enable.
     */
    private static void enableButtons(AbstractButton... buttons) {
        for(AbstractButton button : buttons ) {
            button.setEnabled(true);
        }
    }

    /**
     * Enables each button given, if that button is visible.
     * Disables them if not.
     * @param buttons The buttons to enable (or disable).
     */
    private static void enableButtonsIfVisible(AbstractButton... buttons) {
        for(AbstractButton button : buttons ) {
            button.setEnabled(button.isVisible());
        }
    }

    /**
     * Selects each button given.
     * @param buttons The buttons to select.
     */
    //this one is less useful, but it completes the set & increases code clarity
    private static void selectButtons(AbstractButton... buttons) {
        for(AbstractButton button : buttons ) {
            button.setSelected(true);
        }
    }

    private void romLoaded() {

        try {
            int pokemonGeneration = romHandler.generationOfPokemon();

            setRomNameLabel();
            romCodeLabel.setText(romHandler.getROMCode());
            romSupportLabel.setText(bundle.getString("GUI.header.romInformationPanel.supportPrefix.text") + " "
                    + this.romHandler.getSupportLevel());

            if (!romHandler.isRomValid(null)) {
                romNameLabel.setForeground(Color.RED);
                romCodeLabel.setForeground(Color.RED);
                romSupportLabel.setForeground(Color.RED);
                romSupportLabel.setText("<html>" + bundle.getString("GUI.header.romInformationPanel.supportPrefix.text") + " <b>Unofficial ROM</b>");
                showInvalidRomPopup();
            } else {
                romNameLabel.setForeground(Color.BLACK);
                romCodeLabel.setForeground(Color.BLACK);
                romSupportLabel.setForeground(Color.BLACK);
            }

            lsNoIrregularAltFormesCheckBox.setVisible(pokemonGeneration >= 4);
            lsNoIrregularAltFormesCheckBox.setEnabled(pokemonGeneration >= 4);

            coRandomIntroMonCheckBox.setVisible(romHandler.canSetIntroPokemon());
            coRandomIntroMonCheckBox.setEnabled(romHandler.canSetIntroPokemon());

            raceModeCheckBox.setEnabled(true);

            loadSettingsButton.setEnabled(true);
            saveSettingsButton.setEnabled(true);

            // Pokemon Traits

            // Pokemon Base Stat Totals
            sbstUnchangedRadioButton.setEnabled(true);
            sbstUnchangedRadioButton.setSelected(true);
            sbstRandomBuffNerfRadioButton.setEnabled(true);
            sbstShuffleRadioButton.setEnabled(true);
            sbstRandomRadioButton.setEnabled(true);

            sbstFollowEvolutionsCheckBox.setEnabled(false);
            sbstSwapLegendariesCheckBox.setEnabled(false);

            // Pokemon Base Statistics
            sbsdUnchangedRadioButton.setEnabled(true);
            sbsdUnchangedRadioButton.setSelected(true);
            sbsdShuffleRadioButton.setEnabled(true);
            sbsdRandomRadioButton.setEnabled(true);

            secStandardizeEXPCurvesCheckBox.setEnabled(true);
            secLegendariesSlowRadioButton.setSelected(true);
            sbsUpdateBaseStatsCheckBox.setEnabled(pokemonGeneration < GlobalConstants.HIGHEST_POKEMON_GEN);
            sbsdFollowMegaEvosCheckBox.setVisible(romHandler.hasMegaEvolutions());
            ExpCurve[] expCurves = romHandler.getExpCurves();
            String[] expCurveNames = new String[expCurves.length];
            for (int i = 0; i < expCurves.length; i++) {
                expCurveNames[i] = expCurves[i].toString();
            }
            secEXPCurveComboBox.setModel(new DefaultComboBoxModel<>(expCurveNames));
            secEXPCurveComboBox.setSelectedIndex(0);

            // Pokemon Types
            stUnchangedRadioButton.setEnabled(true);
            stUnchangedRadioButton.setSelected(true);
            stRandomFollowEvolutionsRadioButton.setEnabled(true);
            stRandomCompletelyRadioButton.setEnabled(true);
            stFollowMegaEvosCheckBox.setVisible(romHandler.hasMegaEvolutions());
            stForceDualTypeCheckBox.setEnabled(false);

            // Pokemon Abilities
            if (pokemonGeneration >= 3) {
                saUnchangedRadioButton.setEnabled(true);
                saUnchangedRadioButton.setSelected(true);
                saRandomRadioButton.setEnabled(true);

                saBanWonderGuardCheckBox.setEnabled(false);
                saFollowEvolutionsCheckBox.setEnabled(false);
                saBanTrappingAbilitiesCheckBox.setEnabled(false);
                saBanNegativeAbilitiesCheckBox.setEnabled(false);
                saBanMinorAbilitiesCheckBox.setEnabled(false);
                saFollowMegaEvosCheckBox.setVisible(romHandler.hasMegaEvolutions());
                saWeighDuplicatesTogetherCheckBox.setEnabled(false);
                saForceTwoAbilitiesCheckbox.setEnabled(false);
            } else {
                speciesAbilitiesPanel.setVisible(false);
            }

            // Pokemon Evolutions
            peUnchangedRadioButton.setEnabled(true);
            peUnchangedRadioButton.setSelected(true);
            peRandomRadioButton.setEnabled(true);
            peRandomEveryLevelRadioButton.setVisible(romHandler.canGiveEverySpeciesOneEvolutionEach());
            peRandomEveryLevelRadioButton.setEnabled(romHandler.canGiveEverySpeciesOneEvolutionEach());
            peChangeImpossibleEvosCheckBox.setEnabled(true);
            peMakeEvolutionsEasierCheckBox.setEnabled(true);
            peMakeEvolutionsEasierLvlSlider.setMaximum(
                    Math.max(SettingsManager.MAKE_EVOLUTIONS_EASIER_DEFAULT_LVL, romHandler.getHighestEvoLvl()));
            guaranteeMaximumValueTick(peMakeEvolutionsEasierLvlSlider);
            peRemoveTimeBasedEvolutionsCheckBox.setVisible(romHandler.hasTimeBasedEvolutions());
            peRemoveTimeBasedEvolutionsCheckBox.setEnabled(romHandler.hasTimeBasedEvolutions());
            peAllowAltFormesCheckBox.setVisible(pokemonGeneration >= 7);

            // Starters, Statics & Trades

            // Starter Pokemon
            spUnchangedRadioButton.setEnabled(true);
            spUnchangedRadioButton.setSelected(true);

            spCustomRadioButton.setEnabled(true);
            spRandomRadioButton.setEnabled(true);
            if (romHandler.isYellow()) {
                spCustom3ComboBox.setVisible(false);
            }
            populateDropdowns();

            boolean typeTriangleSupport = romHandler.hasStarterTypeTriangleSupport();
            spTypeFwgRadioButton.setVisible(typeTriangleSupport);
            spTypeTriangleRadioButton.setVisible(typeTriangleSupport);

            spAllowAltFormesCheckBox.setVisible(romHandler.hasStarterAltFormes());
            boolean supportsStarterHeldItems = romHandler.supportsStarterHeldItems();
            spRandomizeStarterHeldItemsCheckBox.setEnabled(supportsStarterHeldItems);
            spRandomizeStarterHeldItemsCheckBox.setVisible(supportsStarterHeldItems);
            spBanMinorItemsCheckBox.setEnabled(false);
            spBanMinorItemsCheckBox.setVisible(supportsStarterHeldItems);
            //TODO: pull these numbers from the romHandler rather than nowhere
            if(romHandler.generationOfPokemon() == 1) {
                spBSTMinimumSpinner.setModel(new SpinnerNumberModel(249, 1, 1275, 1));
                spBSTMaximumSpinner.setModel(new SpinnerNumberModel(253, 1, 1275, 1));
            } else {
                spBSTMinimumSpinner.setModel(new SpinnerNumberModel(307, 1, 1530, 1));
                spBSTMaximumSpinner.setModel(new SpinnerNumberModel(320, 1, 1530, 1));
            }

            stpUnchangedRadioButton.setEnabled(true);
            stpUnchangedRadioButton.setSelected(true);
            if (romHandler.canChangeStaticPokemon()) {
                stpSwapLegendariesSwapStandardsRadioButton.setEnabled(true);
                stpRandomCompletelyRadioButton.setEnabled(true);
                stpRandomSimilarStrengthRadioButton.setEnabled(true);
                stpLimitMainGameLegendariesCheckBox.setVisible(romHandler.hasMainGameLegendaries());
                stpLimitMainGameLegendariesCheckBox.setEnabled(false);
                stpAllowAltFormesCheckBox.setVisible(romHandler.hasStaticAltFormes());
                stpSwapMegaEvosCheckBox.setVisible(pokemonGeneration == 6 && !romHandler.forceSwapStaticMegaEvos());
                stpPercentageLevelModifierCheckBox.setVisible(true);
                stpPercentageLevelModifierCheckBox.setEnabled(true);
                stpPercentageLevelModifierSpinSlider.setVisible(true);
                stpPercentageLevelModifierSpinSlider.setEnabled(false);
                stpFixMusicCheckBox.setVisible(romHandler.hasStaticMusicFix());
                stpFixMusicCheckBox.setEnabled(false);
            } else {
                stpSwapLegendariesSwapStandardsRadioButton.setVisible(false);
                stpRandomCompletelyRadioButton.setVisible(false);
                stpRandomSimilarStrengthRadioButton.setVisible(false);
                stpRandomize600BSTCheckBox.setVisible(false);
                stpLimitMainGameLegendariesCheckBox.setVisible(false);
                stpPercentageLevelModifierCheckBox.setVisible(false);
                stpPercentageLevelModifierSpinSlider.setVisible(false);
                stpFixMusicCheckBox.setVisible(false);
            }

            igtRandomizeNicknamesCheckBox.setEnabled(false);
            igtRandomizeOTsCheckBox.setEnabled(false);
            igtRandomizeIVsCheckBox.setEnabled(false);
            igtRandomizeItemsCheckBox.setEnabled(false);

            if (pokemonGeneration == 1) {
                igtRandomizeOTsCheckBox.setVisible(false);
                igtRandomizeIVsCheckBox.setVisible(false);
                igtRandomizeItemsCheckBox.setVisible(false);
            }

            // Move Data
            mdRandomizeMovePowerCheckBox.setEnabled(true);
            mdRandomizeMoveAccuracyCheckBox.setEnabled(true);
            mdRandomizeMovePPCheckBox.setEnabled(true);
            mdRandomizeMoveTypesCheckBox.setEnabled(true);
            boolean canRandomizeMoveNames = romHandler.isEnglish();
            mdRandomizeMoveNamesCheckBox.setEnabled(canRandomizeMoveNames);
            mdRandomizeMoveNamesCheckBox.setVisible(canRandomizeMoveNames);
            mdRandomizeMoveCategoryCheckBox.setEnabled(romHandler.hasPhysicalSpecialSplit());
            mdRandomizeMoveCategoryCheckBox.setVisible(romHandler.hasPhysicalSpecialSplit());
            mdUpdateMovesCheckBox.setEnabled(pokemonGeneration < 8);
            mdUpdateMovesCheckBox.setVisible(pokemonGeneration < 8);

            // Pokemon Movesets
            pmsUnchangedRadioButton.setEnabled(true);
            pmsUnchangedRadioButton.setSelected(true);
            pmsRandomPreferringSameTypeRadioButton.setEnabled(true);
            pmsRandomCompletelyRadioButton.setEnabled(true);
            pmsMetronomeOnlyModeRadioButton.setEnabled(true);

            pmsGuaranteedLevel1MovesCheckBox.setVisible(romHandler.supportsFourStartingMoves());
            pmsGuaranteedLevel1MovesSlider.setVisible(romHandler.supportsFourStartingMoves());
            pmsEvolutionMovesCheckBox.setVisible(pokemonGeneration >= 7);

            tpAllowAlternateFormesCheckBox.setVisible(romHandler.hasFunctionalFormes());
            tpTrainersEvolveTheirPokemonCheckbox.setEnabled(true);
            tpPercentageLevelModifierCheckBox.setEnabled(true);
            tpSwapMegaEvosCheckBox.setVisible(romHandler.hasMegaEvolutions());
            tpBattleStylePanel.setVisible(pokemonGeneration >= 3);
            if (tpBattleStylePanel.isVisible()) {
                tbsUnchangedStyleRadioButton.setVisible(pokemonGeneration >= 3);
                tbsUnchangedStyleRadioButton.setEnabled(true);
                tbsUnchangedStyleRadioButton.setSelected(true);

                tbsRandomStyleRadioButton.setVisible(pokemonGeneration >= 3);
                tbsRandomStyleRadioButton.setEnabled(true);
                tbsRandomStyleRadioButton.setSelected(false);
            }

            boolean canAddPokesToBoss = romHandler.canAddPokemonToBossTrainers();
            boolean canAddPokesToImportant = romHandler.canAddPokemonToImportantTrainers();
            boolean canAddPokesToRegular = romHandler.canAddPokemonToRegularTrainers();
            boolean additionalPokemonAvailable = canAddPokesToBoss || canAddPokesToImportant || canAddPokesToRegular;

            tpAdditionalPokemonForLabel.setVisible(additionalPokemonAvailable);
            tpBossTrainersCheckBox.setVisible(canAddPokesToBoss);
            tpBossTrainersCheckBox.setEnabled(canAddPokesToBoss);
            tpBossTrainersSpinner.setVisible(canAddPokesToBoss);
            tpImportantTrainersCheckBox.setVisible(canAddPokesToImportant);
            tpImportantTrainersCheckBox.setEnabled(canAddPokesToImportant);
            tpImportantTrainersSpinner.setVisible(canAddPokesToImportant);
            tpRegularTrainersCheckBox.setVisible(canAddPokesToRegular);
            tpRegularTrainersCheckBox.setEnabled(canAddPokesToRegular);
            tpRegularTrainersSpinner.setVisible(canAddPokesToRegular);

            boolean canAddHeldItemsToBoss = romHandler.canAddHeldItemsToBossTrainers();
            boolean canAddHeldItemsToImportant = romHandler.canAddHeldItemsToImportantTrainers();
            boolean canAddHeldItemsToRegular = romHandler.canAddHeldItemsToRegularTrainers();
            boolean heldItemsAvailable = canAddHeldItemsToBoss || canAddHeldItemsToImportant || canAddHeldItemsToRegular;

            tpHeldItemsLabel.setVisible(heldItemsAvailable);
            tpBossTrainersItemsCheckBox.setVisible(canAddHeldItemsToBoss);
            tpBossTrainersItemsCheckBox.setEnabled(false);
            tpImportantTrainersItemsCheckBox.setVisible(canAddHeldItemsToImportant);
            tpImportantTrainersItemsCheckBox.setEnabled(false);
            tpRegularTrainersItemsCheckBox.setVisible(canAddHeldItemsToRegular);
            tpRegularTrainersItemsCheckBox.setEnabled(false);
            tpConsumableItemsOnlyCheckBox.setVisible(heldItemsAvailable);
            tpConsumableItemsOnlyCheckBox.setEnabled(false);
            tpSensibleItemsCheckBox.setVisible(heldItemsAvailable);
            tpSensibleItemsCheckBox.setEnabled(false);
            tpHighestLevelGetsItemCheckBox.setVisible(heldItemsAvailable);
            tpHighestLevelGetsItemCheckBox.setEnabled(false);

            disableAndDeselectButtons(tpRegularTrainersTypeDiversityCheckBox, tpImportantTrainersTypeDiversityCheckBox,
                    tpBossTrainersTypeDiversityCheckBox);

            enableButtons(coRandomizeTrainerNamesCheckBox, coRandomizeTrainerClassNamesCheckBox);

            tpNoEarlyWonderGuardCheckBox.setVisible(romHandler.abilitiesPerSpecies() != 0);
            tpRandomShinyTrainerPokemonCheckBox.setVisible(pokemonGeneration >= 7);

            boolean canGiveMovesetsToBoss = romHandler.canGiveCustomMovesetsToBossTrainers();
            boolean canGiveMovesetsToImportant = romHandler.canGiveCustomMovesetsToImportantTrainers();
            boolean canGiveMovesetsToRegular = romHandler.canGiveCustomMovesetsToRegularTrainers();
            boolean betterMovesetsAvailable = canGiveMovesetsToBoss || canGiveMovesetsToImportant || canGiveMovesetsToRegular;

            tpBetterMovesetsLabel.setVisible(betterMovesetsAvailable);
            tpBetterMovesetsBossTrainersCheckBox.setEnabled(canGiveMovesetsToBoss);
            tpBetterMovesetsBossTrainersCheckBox.setVisible(canGiveMovesetsToBoss);
            tpBetterMovesetsImportantTrainersCheckBox.setEnabled(canGiveMovesetsToImportant);
            tpBetterMovesetsImportantTrainersCheckBox.setVisible(canGiveMovesetsToImportant);
            tpBetterMovesetsRegularTrainersCheckBox.setEnabled(canGiveMovesetsToRegular);
            tpBetterMovesetsRegularTrainersCheckBox.setVisible(canGiveMovesetsToRegular);

            totpPanel.setVisible(romHandler.hasTotemPokemon());
            if (totpPanel.isVisible()) {
                totpUnchangedRadioButton.setEnabled(true);
                totpUnchangedRadioButton.setSelected(true);
                totpRandomRadioButton.setEnabled(true);
                totpRandomSimilarStrengthRadioButton.setEnabled(true);

                totpAllyUnchangedRadioButton.setEnabled(true);
                totpAllyUnchangedRadioButton.setSelected(true);
                totpAllyRandomRadioButton.setEnabled(true);
                totpAllyRandomSimilarStrengthRadioButton.setEnabled(true);

                totpAuraUnchangedRadioButton.setEnabled(true);
                totpAuraUnchangedRadioButton.setSelected(true);
                totpAuraRandomRadioButton.setEnabled(true);
                totpAuraRandomSameStrengthRadioButton.setEnabled(true);

                totpRandomizeHeldItemsCheckBox.setEnabled(true);
                totpAllowAltFormesCheckBox.setEnabled(false);
                totpPercentageLevelModifierCheckBox.setEnabled(true);
                totpPercentageLevelModifierSpinSlider.setEnabled(false);
            }

            // Wild Pokemon
            wpRandomizeWildPokemonCheckBox.setEnabled(true);
            wpRandomizeWildPokemonCheckBox.setSelected(false);
            wpZoneNamedLocationRadioButton.setVisible(romHandler.hasEncounterLocations());
            if(romHandler.hasMapIndices()) {
                wpZoneEncounterSetRadioButton.setVisible(false);
                wpZoneMapRadioButton.setVisible(true);
            } else {
                wpZoneEncounterSetRadioButton.setVisible(true);
                wpZoneMapRadioButton.setVisible(false);
            }
            wpZoneGameRadioButton.setSelected(true);
            wpTRNoneRadioButton.setSelected(true);
            wpERNoneRadioButton.setSelected(true);
            wpRemoveTimeBasedEncountersCheckBox.setVisible(romHandler.hasTimeBasedEncounters());
            wpRemoveTimeBasedEncountersCheckBox.setSelected(true);
            wpSetMinimumCatchRateCheckBox.setEnabled(true);
            wpRandomizeHeldItemsCheckBox.setEnabled(true);
            wpRandomizeHeldItemsCheckBox.setVisible(pokemonGeneration != 1);
            wpBanBadItemsCheckBox.setVisible(pokemonGeneration != 1);
            wpPercentageLevelModifierCheckBox.setEnabled(true);
            wpAllowAltFormesCheckBox.setVisible(romHandler.hasWildAltFormes());

            tmmUnchangedRadioButton.setEnabled(true);
            tmmUnchangedRadioButton.setSelected(true);
            tmmRandomRadioButton.setEnabled(true);
            thcFullHMCompatibilityCheckBox.setVisible(pokemonGeneration < 7);
            if (thcFullHMCompatibilityCheckBox.isVisible()) {
                thcFullHMCompatibilityCheckBox.setEnabled(true);
            }

            thcUnchangedRadioButton.setEnabled(true);
            thcUnchangedRadioButton.setSelected(true);
            thcRandomPreferSameTypeRadioButton.setEnabled(true);
            thcRandomCompletelyRadioButton.setEnabled(true);
            thcFullCompatibilityRadioButton.setEnabled(true);

            if (romHandler.hasMoveTutors()) {
                mtMovesPanel.setVisible(true);
                mtCompatPanel.setVisible(true);
                mtNoExistLabel.setVisible(false);

                mtmUnchangedRadioButton.setEnabled(true);
                mtmUnchangedRadioButton.setSelected(true);
                mtmRandomRadioButton.setEnabled(true);

                mtcUnchangedRadioButton.setEnabled(true);
                mtcUnchangedRadioButton.setSelected(true);
                mtcRandomPreferSameTypeRadioButton.setEnabled(true);
                mtcRandomCompletelyRadioButton.setEnabled(true);
                mtcFullCompatibilityRadioButton.setEnabled(true);
            } else {
                mtMovesPanel.setVisible(false);
                mtCompatPanel.setVisible(false);
                mtNoExistLabel.setVisible(true);
            }

            fiUnchangedRadioButton.setEnabled(true);
            fiUnchangedRadioButton.setSelected(true);
            fiShuffleRadioButton.setEnabled(true);
            fiRandomRadioButton.setEnabled(true);
            fiRandomEvenDistributionRadioButton.setEnabled(true);

            // Gen 1 doesn't really have any interesting special shops/items to put in them,
            // so it might be worth hiding that panel.
            shopItemsPanel.setVisible(romHandler.hasShopSupport());
            shUnchangedRadioButton.setEnabled(true);
            shUnchangedRadioButton.setSelected(true);
            shShuffleRadioButton.setEnabled(true);
            shRandomRadioButton.setEnabled(true);
            shBalanceShopItemPricesCheckBox.setEnabled(true);
            shAddRareCandyCheckBox.setVisible(romHandler.canChangeShopSizes());
            shAddRareCandyCheckBox.setEnabled(romHandler.canChangeShopSizes());

            pickupItemsPanel.setVisible(romHandler.abilitiesPerSpecies() > 0);
            puUnchangedRadioButton.setEnabled(true);
            puUnchangedRadioButton.setSelected(true);
            puRandomRadioButton.setEnabled(true);

            // Types
            boolean typeSupport = romHandler.hasTypeEffectivenessSupport();
            //typesPanel.setVisible(typeSupport);
            //We shouldn't use setVisible on the panels directly in the tabbedPane; it causes strange bleedover
            //Disable it instead
            randomizationSettingsTabbedPane.setEnabledAt(7, typeSupport);
            teUnchangedRadioButton.setEnabled(typeSupport);
            teUnchangedRadioButton.setSelected(true);
            teRandomRadioButton.setEnabled(typeSupport);
            teRandomBalancedRadioButton.setEnabled(typeSupport);
            teKeepTypeIdentitiesRadioButton.setEnabled(typeSupport);
            teInverseRadioButton.setEnabled(typeSupport);
            disableAndDeselectButtons(teAddRandomImmunitiesCheckBox);
            teUpdateCheckbox.setVisible(typeSupport && pokemonGeneration < TypeEffectivenessUpdater.UPDATE_TO_GEN);
            teUpdateCheckbox.setEnabled(typeSupport && pokemonGeneration < TypeEffectivenessUpdater.UPDATE_TO_GEN);
            teUpdateCheckbox.setSelected(false);

            // Graphics
            boolean ppalSupport = romHandler.hasPokemonPaletteSupport();
            ppalNotExistLabel.setVisible(!ppalSupport);
            boolean ppalPartialSupport = romHandler.pokemonPaletteSupportIsPartial();
            ppalPartiallyImplementedLabel.setVisible(ppalPartialSupport);
            ppalUnchangedRadioButton.setVisible(ppalSupport);
            ppalUnchangedRadioButton.setEnabled(ppalSupport);
            ppalUnchangedRadioButton.setSelected(true);
            ppalRandomRadioButton.setVisible(ppalSupport);
            ppalRandomRadioButton.setEnabled(ppalSupport);
            ppalFollowTypesCheckBox.setVisible(ppalSupport);
            ppalFollowTypesCheckBox.setEnabled(false);
            ppalFollowEvolutionsCheckBox.setVisible(ppalSupport);
            ppalFollowEvolutionsCheckBox.setEnabled(false);
            ppalShinyFromNormalCheckBox.setVisible(!(romHandler instanceof Gen1RomHandler) && ppalSupport);
            ppalShinyFromNormalCheckBox.setEnabled(false);

            boolean cpgSupport = romHandler.hasCustomPlayerGraphicsSupport();
            cpgNotExistLabel.setVisible(!cpgSupport);
            cpgUnchangedRadioButton.setVisible(cpgSupport);
            cpgUnchangedRadioButton.setEnabled(cpgSupport);
            cpgUnchangedRadioButton.setSelected(true);
            cpgCustomRadioButton.setVisible(cpgSupport);
            cpgCustomRadioButton.setEnabled(cpgSupport);
            cpgSelection.setVisible(cpgSupport);
            if (cpgSupport) {
                cpgSelection.fillComboBox(romHandler);
            }
            boolean cpgReplaceChoiceSupport = cpgSupport && romHandler.hasMultiplePlayerCharacters();
            cpgSelection.setReplaceChoiceVisible(cpgReplaceChoiceSupport);

            randomizationSettingsTabbedPane.setEnabledAt(8, ppalSupport || cpgSupport);

            if (romHandler.generationOfPokemon() < 6) {
                applyGameUpdateMenuItem.setVisible(false);
            } else {
                applyGameUpdateMenuItem.setVisible(true);
            }

            if (romHandler.hasGameUpdateLoaded()) {
                removeGameUpdateMenuItem.setVisible(true);
            } else {
                removeGameUpdateMenuItem.setVisible(false);
            }

            gameMascotLabel.setIcon(makeMascotIcon());

            if (romHandler.getResourceLifetime() == RomHandler.ResourceLifetime.LOAD_ONLY) {
                romHandler.closeResources();
            }
        } catch (Exception e) {
            attemptToLogException(e, "GUI.loadROM.processFailedDialog.message", "GUI.loadROM.processFailedNoLogDialog.message", null, null);
            unloadRomHandler();
            initialState();
        }
    }

    private void guaranteeMaximumValueTick(JSlider slider) {
        // Create standard labels (only up to the last multiple)
        Dictionary<Integer, JComponent> table = slider.createStandardLabels(slider.getMajorTickSpacing(), slider.getMinimum());

        // Force label at the exact maximum
        int max = slider.getMaximum();
        table.put(max, new JLabel(String.valueOf(max)));

        slider.setLabelTable(table);
    }

    private void setRomNameLabel() {
        if (romHandler.hasGameUpdateLoaded()) {
            romNameLabel.setText(romHandler.getROMName() + " (" + romHandler.getGameUpdateVersion() + ")");
        } else {
            romNameLabel.setText(romHandler.getROMName());
        }
    }

    private void enableOrDisableSubControls() {
        //TODO: Check that current SettingRestrictions match these, then remove this entire block
        if(romHandler == null) {
            //shouldn't be in this method right now
            return;
        }

        /*
        if (limitPokemonCheckBox.isSelected()) {
            limitPokemonButton.setEnabled(true);
        } else {
            limitPokemonButton.setEnabled(false);
        }
         */

        boolean followEvolutionControlsEnabled = !peRandomEveryLevelRadioButton.isSelected();
        boolean followMegaEvolutionControlsEnabled = !(peRandomEveryLevelRadioButton.isSelected() && !lsNoIrregularAltFormesCheckBox.isSelected() && peAllowAltFormesCheckBox.isSelected());

        if (peRandomEveryLevelRadioButton.isSelected()) {
            // If Evolve Every Level is enabled, unselect all "Follow Evolutions" controls
            sbstFollowEvolutionsCheckBox.setSelected(false);
            sbsdFollowEvolutionsCheckBox.setSelected(false);
            stRandomFollowEvolutionsRadioButton.setEnabled(false);
            if (stRandomFollowEvolutionsRadioButton.isSelected()) {
                stRandomFollowEvolutionsRadioButton.setSelected(false);
                stRandomCompletelyRadioButton.setSelected(true);
            }
            /*
            spRandomTwoEvosRadioButton.setEnabled(false);
            if (spRandomTwoEvosRadioButton.isSelected()) {
                spRandomTwoEvosRadioButton.setSelected(false);
                spRandomRadioButton.setSelected(true);
            }
            spRandomBasicRadioButton.setEnabled(false);
            if (spRandomBasicRadioButton.isSelected()) {
                spRandomBasicRadioButton.setSelected(false);
                spRandomRadioButton.setSelected(true);
            }
            */
            saFollowEvolutionsCheckBox.setSelected(false);
            thcFollowEvolutionsCheckBox.setSelected(false);
            mtcFollowEvolutionsCheckBox.setSelected(false);

            // If the Follow Mega Evolution controls should be disabled, deselect them here too
            if (!followMegaEvolutionControlsEnabled) {
                sbsdFollowMegaEvosCheckBox.setSelected(false);
                stFollowMegaEvosCheckBox.setSelected(false);
                saFollowMegaEvosCheckBox.setSelected(false);
            }

            // Also disable/unselect all the settings that make evolutions easier/possible,
            // since they aren't relevant in this scenario at all.
            disableAndDeselectButtons(peChangeImpossibleEvosCheckBox, peUseEstimatedInsteadOfHardcodedLevelsCheckBox,
                    peMakeEvolutionsEasierCheckBox, peRemoveTimeBasedEvolutionsCheckBox);
            peMakeEvolutionsEasierLvlSlider.setEnabled(false);
            peMakeEvolutionsEasierLvlSlider.setValue(SettingsManager.MAKE_EVOLUTIONS_EASIER_DEFAULT_LVL);

            // Disable "Trainers Evolve their Pokemon" as well as "No Premature Evolutions"
            disableAndDeselectButtons(tpTrainersEvolveTheirPokemonCheckbox, lsNoPrematureEvosCheckbox);
        } else {
            // All other "Follow Evolutions" controls get properly set/unset below
            // except this one, so manually enable it again.
            stRandomFollowEvolutionsRadioButton.setEnabled(true);
            /*
            spRandomTwoEvosRadioButton.setEnabled(true);
            spRandomBasicRadioButton.setEnabled(true);
             */

            // The controls that make evolutions easier/possible, however,
            // need to all be manually re-enabled.
            peChangeImpossibleEvosCheckBox.setEnabled(true);
            peMakeEvolutionsEasierCheckBox.setEnabled(true);
            peRemoveTimeBasedEvolutionsCheckBox.setEnabled(true);
            // Only enable 'Use estimated level' if 'Change Impossible Evolutions' or 'Make Evolutions Easier' is
            // selected, otherwise disable and deselect it
            if (peChangeImpossibleEvosCheckBox.isSelected() || peMakeEvolutionsEasierCheckBox.isSelected()) {
                peUseEstimatedInsteadOfHardcodedLevelsCheckBox.setEnabled(true);
            } else {
                disableAndDeselectButtons(peUseEstimatedInsteadOfHardcodedLevelsCheckBox);
            }

            // Re-enable "Trainers Evolve their Pokemon" as well as "No Premature Evolutions"
            tpTrainersEvolveTheirPokemonCheckbox.setEnabled(true);
            lsNoPrematureEvosCheckbox.setEnabled(true);
        }

        // shuffle BST+follow evolutions does not make sense if the evos are random
        if ((sbstRandomBuffNerfRadioButton.isSelected()
                || (sbstShuffleRadioButton.isSelected() && !peRandomRadioButton.isSelected()))
                && !peRandomEveryLevelRadioButton.isSelected()) {
            enableButtons(sbstFollowEvolutionsCheckBox);
        } else {
            disableAndDeselectButtons(sbstFollowEvolutionsCheckBox);
        }

        if (sbstRandomBuffNerfRadioButton.isSelected()) {
            sbstRandomBuffNerfSpinSlider.setEnabled(true);
        } else {
            sbstRandomBuffNerfSpinSlider.setEnabled(false);
            sbstRandomBuffNerfSpinSlider.setValue(0);
        }

        if (sbstShuffleRadioButton.isSelected()) {
            enableButtons(sbstSwapLegendariesCheckBox);
        } else {
            disableAndDeselectButtons(sbstSwapLegendariesCheckBox);
        }

        if (sbsdUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(sbsdFollowEvolutionsCheckBox, sbsdFollowMegaEvosCheckBox);
        } else {
            enableButtons(sbsdFollowEvolutionsCheckBox, sbsdFollowMegaEvosCheckBox);
        }

        if (sbsdRandomRadioButton.isSelected()) {
            if (sbsdFollowEvolutionsCheckBox.isSelected() || sbsdFollowMegaEvosCheckBox.isSelected()) {
                enableButtons(sbsdAssignEvoStatsRandomlyCheckBox);
            } else {
                disableAndDeselectButtons(sbsdAssignEvoStatsRandomlyCheckBox);
            }
        } else {
            disableAndDeselectButtons(sbsdAssignEvoStatsRandomlyCheckBox);
        }

        if (secStandardizeEXPCurvesCheckBox.isSelected()) {
            secLegendariesSlowRadioButton.setEnabled(true);
            secStrongLegendariesSlowRadioButton.setEnabled(true);
            secAllSpeciesRadioButton.setEnabled(true);
            secEXPCurveComboBox.setEnabled(true);
        } else {
            secLegendariesSlowRadioButton.setEnabled(false);
            secLegendariesSlowRadioButton.setSelected(true);
            secStrongLegendariesSlowRadioButton.setEnabled(false);
            secAllSpeciesRadioButton.setEnabled(false);
            secEXPCurveComboBox.setEnabled(false);
        }

        /* Commenting this so it can compile again
        if (sbsUpdateBaseStatsCheckBox.isSelected()) {
            pbsUpdateComboBox.setEnabled(true);
        } else {
            pbsUpdateComboBox.setEnabled(false);
        }
        */

        if (stUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(stFollowMegaEvosCheckBox, stForceDualTypeCheckBox);
        } else {
            stFollowMegaEvosCheckBox.setEnabled(followMegaEvolutionControlsEnabled);
            stForceDualTypeCheckBox.setEnabled(true);
        }

        if (saRandomRadioButton.isSelected()) {
            enableButtons(saBanWonderGuardCheckBox, saFollowEvolutionsCheckBox,
                    saBanTrappingAbilitiesCheckBox, saBanNegativeAbilitiesCheckBox, saBanMinorAbilitiesCheckBox,
                    saFollowMegaEvosCheckBox, saWeighDuplicatesTogetherCheckBox, saForceTwoAbilitiesCheckbox);
        } else {
            disableAndDeselectButtons(saBanWonderGuardCheckBox, saFollowEvolutionsCheckBox,
                    saBanTrappingAbilitiesCheckBox, saBanNegativeAbilitiesCheckBox, saBanMinorAbilitiesCheckBox,
                    saFollowMegaEvosCheckBox, saWeighDuplicatesTogetherCheckBox, saForceTwoAbilitiesCheckbox);
        }

        if (peRandomRadioButton.isSelected()) {
            enableButtons(peSimilarStrengthCheckBox, peSameTypingCheckBox, peLimitEvolutionsToThreeCheckBox,
                    peForceChangeCheckBox, peAllowAltFormesCheckBox, peForceGrowthCheckBox, peNoConvergenceCheckBox);
        } else if (peRandomEveryLevelRadioButton.isSelected()) {
            enableButtons(peSameTypingCheckBox, peForceChangeCheckBox,
                    peAllowAltFormesCheckBox, peNoConvergenceCheckBox);
            disableAndDeselectButtons(peSimilarStrengthCheckBox,
                    peLimitEvolutionsToThreeCheckBox, peForceGrowthCheckBox);
        } else {
            disableAndDeselectButtons(peSimilarStrengthCheckBox, peSameTypingCheckBox, peLimitEvolutionsToThreeCheckBox,
                    peForceChangeCheckBox, peAllowAltFormesCheckBox, peForceGrowthCheckBox, peNoConvergenceCheckBox);
        }

        if (peRandomRadioButton.isSelected() || !sbstUnchangedRadioButton.isSelected()) {
            enableButtons(peAdjustLevelsCheckBox);
        } else {
            disableAndDeselectButtons(peAdjustLevelsCheckBox);
        }

        if (peMakeEvolutionsEasierCheckBox.isSelected()) {
            peMakeEvolutionsEasierLvlSlider.setEnabled(true);
        } else {
            peMakeEvolutionsEasierLvlSlider.setEnabled(false);
            peMakeEvolutionsEasierLvlSlider.setValue(SettingsManager.MAKE_EVOLUTIONS_EASIER_DEFAULT_LVL);
        }

        boolean spCustomStatus = spCustomRadioButton.isSelected();
        spCustom1ComboBox.setEnabled(spCustomStatus);
        spCustom2ComboBox.setEnabled(spCustomStatus);
        spCustom3ComboBox.setEnabled(spCustomStatus);

        if (spRandomizeStarterHeldItemsCheckBox.isSelected()) {
            enableButtons(spBanMinorItemsCheckBox);
        } else {
            disableAndDeselectButtons(spBanMinorItemsCheckBox);
        }

        boolean isCustomRandom = (spCustom1ComboBox.getSelectedIndex() == 0 || spCustom2ComboBox.getSelectedIndex() == 0
                || spCustom3ComboBox.getSelectedIndex() == 0) && spCustomRadioButton.isSelected();

        if (spUnchangedRadioButton.isSelected() || (spCustomRadioButton.isSelected() && !isCustomRandom)) {
            disableButtonsWithDefault(spTypeNoneRadioButton,
                    spTypeNoneRadioButton, spTypeFwgRadioButton, spTypeTriangleRadioButton,
                    spTypeUniqueRadioButton, spTypeSingleRadioButton);
            disableAndDeselectButtons(spTypeNoDualCheckbox, spAllowAltFormesCheckBox,spNoLegendariesCheckBox,
                    spBSTMinimumCheckbox, spBSTMaximumCheckbox);
        } else {
            enableButtons(spTypeNoneRadioButton, spTypeUniqueRadioButton, spTypeSingleRadioButton);

            //we can't do triangles when we don't have control of all three starters
            if(isCustomRandom) {
                disableButtonsWithDefault(spTypeNoneRadioButton,
                        spTypeFwgRadioButton, spTypeTriangleRadioButton);
            } else {
                enableButtons(spTypeFwgRadioButton, spTypeTriangleRadioButton);
            }

            spTypeNoDualCheckbox.setEnabled(!stForceDualTypeCheckBox.isSelected());
            stForceDualTypeCheckBox.setEnabled(!spTypeNoDualCheckbox.isSelected());

            enableButtons(spAllowAltFormesCheckBox, spNoLegendariesCheckBox,
                    spBSTMinimumCheckbox, spBSTMaximumCheckbox);
        }

        spBSTMinimumSpinner.setEnabled(spBSTMinimumCheckbox.isSelected());
        spBSTMaximumSpinner.setEnabled(spBSTMaximumCheckbox.isSelected());

        spTypeSingleComboBox.setEnabled(spTypeSingleRadioButton.isSelected());

        if (stpUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(stpRandomize600BSTCheckBox, stpAllowAltFormesCheckBox,
                    stpSwapMegaEvosCheckBox, stpFixMusicCheckBox);
        } else {
            enableButtons(stpRandomize600BSTCheckBox, stpAllowAltFormesCheckBox,
                    stpSwapMegaEvosCheckBox, stpFixMusicCheckBox);
        }

        if (stpRandomSimilarStrengthRadioButton.isSelected()) {
            stpLimitMainGameLegendariesCheckBox.setEnabled(stpLimitMainGameLegendariesCheckBox.isVisible());
        } else {
            disableAndDeselectButtons(stpLimitMainGameLegendariesCheckBox);
        }

        if (stpPercentageLevelModifierCheckBox.isSelected()) {
            stpPercentageLevelModifierSpinSlider.setEnabled(true);
        } else {
            stpPercentageLevelModifierSpinSlider.setEnabled(false);
            stpPercentageLevelModifierSpinSlider.setValue(0);
        }

        /*
        if (igtUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(igtRandomizeItemsCheckBox, igtRandomizeIVsCheckBox,
                    igtRandomizeNicknamesCheckBox, igtRandomizeOTsCheckBox);
        } else {
            enableButtons(igtRandomizeItemsCheckBox, igtRandomizeIVsCheckBox,
                    igtRandomizeNicknamesCheckBox, igtRandomizeOTsCheckBox);
        }
         */

        if (mdUpdateMovesCheckBox.isSelected()) {
            mdUpdateComboBox.setEnabled(true);
        } else {
            mdUpdateComboBox.setEnabled(false);
        }

        if (pmsMetronomeOnlyModeRadioButton.isSelected() || pmsUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(pmsGuaranteedLevel1MovesCheckBox, pmsForceGoodDamagingCheckBox,
                    pmsReorderDamagingMovesCheckBox, pmsNoGameBreakingMovesCheckBox, pmsEvolutionMovesCheckBox);
        } else {
            enableButtons(pmsGuaranteedLevel1MovesCheckBox, pmsForceGoodDamagingCheckBox,
                    pmsReorderDamagingMovesCheckBox, pmsNoGameBreakingMovesCheckBox, pmsEvolutionMovesCheckBox);
        }

        if (pmsGuaranteedLevel1MovesCheckBox.isSelected()) {
            pmsGuaranteedLevel1MovesSlider.setEnabled(true);
        } else {
            pmsGuaranteedLevel1MovesSlider.setEnabled(false);
            pmsGuaranteedLevel1MovesSlider.setValue(pmsGuaranteedLevel1MovesSlider.getMinimum());
        }

        if (pmsForceGoodDamagingCheckBox.isSelected()) {
            pmsForceGoodDamagingSpinSlider.setEnabled(true);
        } else {
            pmsForceGoodDamagingSpinSlider.setEnabled(false);
            pmsForceGoodDamagingSpinSlider.setValue(pmsForceGoodDamagingSpinSlider.getMinimum());
        }

        boolean pokemonAdded = tpBossTrainersCheckBox.isSelected() || tpImportantTrainersCheckBox.isSelected() ||
                tpRegularTrainersCheckBox.isSelected();
        if (isTrainerSetting(TRAINER_UNCHANGED) && pokemonAdded) {
            disableAndDeselectButtons(tpSwapMegaEvosCheckBox,
                    tpBossTrainersItemsCheckBox, tpImportantTrainersItemsCheckBox, tpRegularTrainersItemsCheckBox,
                    tpConsumableItemsOnlyCheckBox, tpSensibleItemsCheckBox, tpHighestLevelGetsItemCheckBox,
                    tpEliteFourUniquePokemonCheckBox);
            enableButtons(tpSimilarStrengthCheckBox, tpAvoidDuplicatesCheckBox, tpDontUseLegendariesCheckBox,
                    tpUseLocalPokemonCheckBox, tpNoEarlyWonderGuardCheckBox, tpAllowAlternateFormesCheckBox,
                    tpRandomShinyTrainerPokemonCheckBox);
            if (tpBossTrainersCheckBox.isSelected()) {
                tpBossTrainersTypeDiversityCheckBox.setEnabled(true);
            } else {
                disableAndDeselectButtons(tpBossTrainersTypeDiversityCheckBox);
            }
            if (tpImportantTrainersCheckBox.isSelected()) {
                tpImportantTrainersTypeDiversityCheckBox.setEnabled(true);
            } else {
                disableAndDeselectButtons(tpImportantTrainersTypeDiversityCheckBox);
            }
            if (tpRegularTrainersCheckBox.isSelected()) {
                tpRegularTrainersTypeDiversityCheckBox.setEnabled(true);
            } else {
                disableAndDeselectButtons(tpRegularTrainersTypeDiversityCheckBox);
            }
        } else if (isTrainerSetting(TRAINER_UNCHANGED)) {
            disableAndDeselectButtons(tpSimilarStrengthCheckBox, tpAvoidDuplicatesCheckBox, tpDontUseLegendariesCheckBox,
                    tpUseLocalPokemonCheckBox, tpNoEarlyWonderGuardCheckBox, tpAllowAlternateFormesCheckBox,
                    tpSwapMegaEvosCheckBox, tpRandomShinyTrainerPokemonCheckBox,
                    tpBossTrainersItemsCheckBox, tpImportantTrainersItemsCheckBox, tpRegularTrainersItemsCheckBox,
                    tpConsumableItemsOnlyCheckBox, tpSensibleItemsCheckBox, tpHighestLevelGetsItemCheckBox,
                    tpBossTrainersTypeDiversityCheckBox, tpImportantTrainersTypeDiversityCheckBox,
                    tpRegularTrainersTypeDiversityCheckBox,
                    tpEliteFourUniquePokemonCheckBox);
        } else {
            enableButtons(tpSimilarStrengthCheckBox, tpAvoidDuplicatesCheckBox, tpDontUseLegendariesCheckBox,
                    tpUseLocalPokemonCheckBox, tpNoEarlyWonderGuardCheckBox, tpAllowAlternateFormesCheckBox,
                    tpRandomShinyTrainerPokemonCheckBox);

            boolean isTypeTheme = isTrainerSetting(TRAINER_TYPE_THEMED) || isTrainerSetting(TRAINER_TYPE_THEMED_ELITE4_GYMS)
                    || isTrainerSetting(TRAINER_KEEP_THEMED) || isTrainerSetting(TRAINER_KEEP_THEME_OR_PRIMARY);
            if (currentRestrictions == null || currentRestrictions.allowTrainerSwapMegaEvolvables(
                    romHandler.forceSwapStaticMegaEvos(), isTypeTheme)) {
                enableButtons(tpSwapMegaEvosCheckBox);
            } else {
                disableAndDeselectButtons(tpSwapMegaEvosCheckBox);
            }
            enableButtonsIfVisible(tpBossTrainersCheckBox, tpImportantTrainersCheckBox,
                    tpRegularTrainersCheckBox, tpBossTrainersItemsCheckBox, tpImportantTrainersItemsCheckBox,
                    tpRegularTrainersItemsCheckBox, tpEliteFourUniquePokemonCheckBox);

            if(isTrainerSetting(TRAINER_TYPE_THEMED) || isTrainerSetting(TRAINER_KEEP_THEME_OR_PRIMARY)) {
                disableAndDeselectButtons(tpBossTrainersTypeDiversityCheckBox, tpImportantTrainersTypeDiversityCheckBox,
                        tpRegularTrainersTypeDiversityCheckBox);
            } else {
                enableButtons(tpBossTrainersTypeDiversityCheckBox, tpImportantTrainersTypeDiversityCheckBox,
                        tpRegularTrainersTypeDiversityCheckBox);
            }
        }

        //tpBattleStyleCombobox.setEnabled(tpSingleStyleRadioButton.isSelected());

        if (tpTrainersEvolveTheirPokemonCheckbox.isSelected()) {
            tpPercentageEvolutionLevelModifierSpinSlider.setEnabled(true);
            // Only enable fully evolved lvl label if trainer Pokemon are forced to evolve
            tpCalculatedFullyEvolvedLvlLabel.setEnabled(tpTrainersEvolveTheirPokemonCheckbox.isSelected());
        } else {
            tpPercentageEvolutionLevelModifierSpinSlider.setEnabled(false);
            tpPercentageEvolutionLevelModifierSpinSlider.setValue(0);
            tpCalculatedFullyEvolvedLvlLabel.setEnabled(false);
        }

        if (tpCalculatedFullyEvolvedLvlLabel.isEnabled()) {
            updateFullyEvolvedAtLvlLabel();
        } else {
            tpCalculatedFullyEvolvedLvlLabel.setText(String.format(bundle.getString("GUI.foeTab.trainersPanel.calculatedFullyEvolvedLvlLabel.text"), "--"));
        }

        if (tpPercentageLevelModifierCheckBox.isSelected()) {
            tpPercentageLevelModifierSpinSlider.setEnabled(true);
        } else {
            tpPercentageLevelModifierSpinSlider.setEnabled(false);
            tpPercentageLevelModifierSpinSlider.setValue(0);
        }

        if (tpBossTrainersCheckBox.isSelected()) {
            tpBossTrainersSpinner.setEnabled(true);
        } else {
            tpBossTrainersSpinner.setEnabled(false);
            tpBossTrainersSpinner.setValue(1);
        }

        if (tpImportantTrainersCheckBox.isSelected()) {
            tpImportantTrainersSpinner.setEnabled(true);
        } else {
            tpImportantTrainersSpinner.setEnabled(false);
            tpImportantTrainersSpinner.setValue(1);
        }

        if (tpRegularTrainersCheckBox.isSelected()) {
            tpRegularTrainersSpinner.setEnabled(true);
        } else {
            tpRegularTrainersSpinner.setEnabled(false);
            tpRegularTrainersSpinner.setValue(1);
        }

        if (tpBossTrainersItemsCheckBox.isSelected() || tpImportantTrainersItemsCheckBox.isSelected() ||
                tpRegularTrainersItemsCheckBox.isSelected()) {
            enableButtons(tpConsumableItemsOnlyCheckBox, tpSensibleItemsCheckBox, tpHighestLevelGetsItemCheckBox);
        } else {
            disableAndDeselectButtons(tpConsumableItemsOnlyCheckBox, tpSensibleItemsCheckBox,
                    tpHighestLevelGetsItemCheckBox);
        }

        if (!peRandomEveryLevelRadioButton.isSelected() && (!spUnchangedRadioButton.isSelected() || !isTrainerSetting(TRAINER_UNCHANGED))) {
            enableButtons(tpRivalCarriesStarterCheckBox);
        } else {
            disableAndDeselectButtons(tpRivalCarriesStarterCheckBox);
        }

        if (isTrainerSetting(TRAINER_TYPE_THEMED)) {
            enableButtons(tpWeightTypesCheckBox);
        } else {
            disableAndDeselectButtons(tpWeightTypesCheckBox);
        }

        if (tpEliteFourUniquePokemonCheckBox.isSelected()) {
            tpEliteFourUniquePokemonSpinner.setEnabled(true);
        } else {
            tpEliteFourUniquePokemonSpinner.setEnabled(false);
            tpEliteFourUniquePokemonSpinner.setValue(1);
        }

        if (!totpUnchangedRadioButton.isSelected() || !totpAllyUnchangedRadioButton.isSelected()) {
            enableButtons(totpAllowAltFormesCheckBox);
        } else {
            disableAndDeselectButtons(totpAllowAltFormesCheckBox);
        }

        if (totpPercentageLevelModifierCheckBox.isSelected()) {
            totpPercentageLevelModifierSpinSlider.setEnabled(true);
        } else {
            totpPercentageLevelModifierSpinSlider.setEnabled(false);
            totpPercentageLevelModifierSpinSlider.setValue(0);
        }

        if (!wpRandomizeWildPokemonCheckBox.isSelected()) {
            disableButtonsWithDefault(wpZoneGameRadioButton,
                    wpZoneGameRadioButton, wpZoneNamedLocationRadioButton, wpZoneMapRadioButton,
                    wpZoneEncounterSetRadioButton, wpZoneNoneRadioButton);
            disableButtonsWithDefault(wpTRNoneRadioButton,
                    wpTRNoneRadioButton, wpTRKeepPrimaryRadioButton, wpTRThemedAreasRadioButton);
            disableButtonsWithDefault(wpERNoneRadioButton,
                    wpERNoneRadioButton, wpERBasicOnlyRadioButton, wpERSameEvolutionStageRadioButton);
            disableAndDeselectButtons(wpERKeepEvolutionsCheckBox, wpSimilarStrengthCheckBox, wpCatchEmAllModeCheckBox,
                    wpTRKeepThemesCheckBox, wpDontUseLegendariesCheckBox, wpAllowAltFormesCheckBox,
                    wpSplitByEncounterTypesCheckBox);
            disableButtons(wpRemoveTimeBasedEncountersCheckBox);
            selectButtons(wpRemoveTimeBasedEncountersCheckBox);
        } else {
            enableButtons(wpZoneGameRadioButton, wpZoneNamedLocationRadioButton, wpZoneMapRadioButton,
                    wpZoneEncounterSetRadioButton, wpZoneNoneRadioButton);

            enableButtons(wpTRNoneRadioButton, wpTRKeepPrimaryRadioButton, wpTRKeepThemesCheckBox);

            if(!wpZoneEncounterSetRadioButton.isSelected() && !wpZoneNoneRadioButton.isSelected()) {
                enableButtons(wpSplitByEncounterTypesCheckBox);
            } else {
                disableAndDeselectButtons(wpSplitByEncounterTypesCheckBox);
            }

            if(!wpZoneGameRadioButton.isSelected()) {
                enableButtons(wpTRThemedAreasRadioButton, wpCatchEmAllModeCheckBox);
            } else {
                disableButtonsWithDefault(wpTRNoneRadioButton,
                        wpTRThemedAreasRadioButton);
                if(!wpSplitByEncounterTypesCheckBox.isSelected()) {
                    disableAndDeselectButtons(wpCatchEmAllModeCheckBox);
                } else {
                    enableButtons(wpCatchEmAllModeCheckBox);
                }
            }

            enableButtons(wpDontUseLegendariesCheckBox, wpAllowAltFormesCheckBox, wpRemoveTimeBasedEncountersCheckBox,
                    wpSimilarStrengthCheckBox);

            if(!peRandomEveryLevelRadioButton.isSelected()) {
                enableButtons(wpERNoneRadioButton, wpERBasicOnlyRadioButton, wpERSameEvolutionStageRadioButton);
            } else {
                disableButtonsWithDefault(wpERNoneRadioButton,
                        wpERNoneRadioButton, wpERBasicOnlyRadioButton, wpERSameEvolutionStageRadioButton);
            }

            if(!wpZoneNoneRadioButton.isSelected()) {
                enableButtons(wpERKeepEvolutionsCheckBox);
            } else {
                disableAndDeselectButtons(wpERKeepEvolutionsCheckBox);
            }
        }

        if (wpSimilarStrengthCheckBox.isSelected()) {
            enableButtons(wpBalanceShakingGrassPokemonCheckBox);
        } else {
            disableAndDeselectButtons(wpBalanceShakingGrassPokemonCheckBox);
        }

        if (wpRandomizeHeldItemsCheckBox.isSelected()
                && wpRandomizeHeldItemsCheckBox.isVisible()
                && wpRandomizeHeldItemsCheckBox.isEnabled()) { // ??? why all three
            enableButtons(wpBanBadItemsCheckBox);
        } else {
            disableAndDeselectButtons(wpBanBadItemsCheckBox);
        }

        if (wpSetMinimumCatchRateCheckBox.isSelected()) {
            wpSetMinimumCatchRateSlider.setEnabled(true);
        } else {
            wpSetMinimumCatchRateSlider.setEnabled(false);
            wpSetMinimumCatchRateSlider.setValue(0);
        }

        if (wpPercentageLevelModifierCheckBox.isSelected()) {
            wpPercentageLevelModifierSpinSlider.setEnabled(true);
        } else {
            wpPercentageLevelModifierSpinSlider.setEnabled(false);
            wpPercentageLevelModifierSpinSlider.setValue(0);
        }

        if (pmsMetronomeOnlyModeRadioButton.isSelected()) {
            disableButtonsWithDefault(tmmUnchangedRadioButton,
                    tmmUnchangedRadioButton, tmmRandomRadioButton);
            disableAndDeselectButtons(thcLevelupMoveSanityCheckBox, tmmKeepFieldMoveTMsCheckBox,
                    tmmForceGoodDamagingCheckBox, tmmNoGameBreakingMovesCheckBox, thcFollowEvolutionsCheckBox);

            disableButtonsWithDefault(mtmUnchangedRadioButton,
                    mtmUnchangedRadioButton, mtmRandomRadioButton);
            disableAndDeselectButtons(mtcLevelupMoveSanityCheckBox, mtmKeepFieldMoveTutorsCheckBox,
                    mtmForceGoodDamagingCheckBox, mtmNoGameBreakingMovesCheckBox, mtcFollowEvolutionsCheckBox);
        } else {
            enableButtons(tmmUnchangedRadioButton, tmmRandomRadioButton);
            enableButtons(mtmUnchangedRadioButton, mtmRandomRadioButton);

            if (!(pmsUnchangedRadioButton.isSelected()) || !(tmmUnchangedRadioButton.isSelected())
                    || !(thcUnchangedRadioButton.isSelected())) {
                enableButtons(thcLevelupMoveSanityCheckBox);
            } else {
                disableAndDeselectButtons(thcLevelupMoveSanityCheckBox);
            }

            if ((!thcUnchangedRadioButton.isSelected()) || (thcLevelupMoveSanityCheckBox.isSelected())) {
                thcFollowEvolutionsCheckBox.setEnabled(followEvolutionControlsEnabled);
            }
            else {
                disableAndDeselectButtons(thcFollowEvolutionsCheckBox);
            }

            if (!(tmmUnchangedRadioButton.isSelected())) {
                enableButtons(tmmKeepFieldMoveTMsCheckBox, tmmForceGoodDamagingCheckBox, tmmNoGameBreakingMovesCheckBox);
            } else {
                disableAndDeselectButtons(tmmKeepFieldMoveTMsCheckBox, tmmForceGoodDamagingCheckBox,
                        tmmNoGameBreakingMovesCheckBox);
            }

            if (romHandler.hasMoveTutors()
                    && (!(pmsUnchangedRadioButton.isSelected()) || !(mtmUnchangedRadioButton.isSelected())
                    || !(mtcUnchangedRadioButton.isSelected()))) {
                enableButtons(mtcLevelupMoveSanityCheckBox);
            } else {
                disableAndDeselectButtons(mtcLevelupMoveSanityCheckBox);
            }

            if (!(mtcUnchangedRadioButton.isSelected()) || (mtcLevelupMoveSanityCheckBox.isSelected())) {
                mtcFollowEvolutionsCheckBox.setEnabled(followEvolutionControlsEnabled);
            }
            else {
                disableAndDeselectButtons(mtcFollowEvolutionsCheckBox);
            }

            if (romHandler.hasMoveTutors() && !(mtmUnchangedRadioButton.isSelected())) {
                enableButtons(mtmKeepFieldMoveTutorsCheckBox, mtmForceGoodDamagingCheckBox,
                        mtmNoGameBreakingMovesCheckBox);
            } else {
                disableAndDeselectButtons(mtmKeepFieldMoveTutorsCheckBox, mtmForceGoodDamagingCheckBox,
                        mtmNoGameBreakingMovesCheckBox);
            }
        }

        if (tmmForceGoodDamagingCheckBox.isSelected()) {
            tmmForceGoodDamagingSpinSlider.setEnabled(true);
        } else {
            tmmForceGoodDamagingSpinSlider.setEnabled(false);
            tmmForceGoodDamagingSpinSlider.setValue(tmmForceGoodDamagingSpinSlider.getMinimum());
        }

        if (mtmForceGoodDamagingCheckBox.isSelected()) {
            mtmForceGoodDamagingSpinSlider.setEnabled(true);
        } else {
            mtmForceGoodDamagingSpinSlider.setEnabled(false);
            mtmForceGoodDamagingSpinSlider.setValue(mtmForceGoodDamagingSpinSlider.getMinimum());
        }

        thcFullHMCompatibilityCheckBox.setEnabled(!thcFullCompatibilityRadioButton.isSelected());

        if (fiRandomRadioButton.isSelected() && fiRandomRadioButton.isVisible() && fiRandomRadioButton.isEnabled()) {
            enableButtons(fiBanBadItemsCheckBox);
        } else if (fiRandomEvenDistributionRadioButton.isSelected() && fiRandomEvenDistributionRadioButton.isVisible()
                && fiRandomEvenDistributionRadioButton.isEnabled()) {
            enableButtons(fiBanBadItemsCheckBox);
        } else {
            disableAndDeselectButtons(fiBanBadItemsCheckBox);
        }

        if (shRandomRadioButton.isSelected() && shRandomRadioButton.isVisible() && shRandomRadioButton.isEnabled()) {
            enableButtons(shBanBadItemsCheckBox, shBanRegularShopItemsCheckBox,
                    shBanOverpoweredShopItemsCheckBox, shGuaranteeEvolutionItemsCheckBox,
                    shGuaranteeXItemsCheckBox);
        } else {
            disableAndDeselectButtons(shBanBadItemsCheckBox, shBanRegularShopItemsCheckBox,
                    shBanOverpoweredShopItemsCheckBox, shGuaranteeEvolutionItemsCheckBox,
                    shGuaranteeXItemsCheckBox);
        }

        if (puRandomRadioButton.isSelected() && puRandomRadioButton.isVisible() && puRandomRadioButton.isEnabled()) {
            enableButtons(puBanBadItemsCheckBox);
        } else {
            disableAndDeselectButtons(puBanBadItemsCheckBox);
        }

        if (teInverseRadioButton.isSelected()) {
            enableButtons(teAddRandomImmunitiesCheckBox);
        } else {
            disableAndDeselectButtons(teAddRandomImmunitiesCheckBox);
        }

        if (ppalRandomRadioButton.isSelected() && ppalRandomRadioButton.isVisible()
                && ppalRandomRadioButton.isEnabled()) {
            enableButtons(ppalFollowTypesCheckBox, ppalFollowEvolutionsCheckBox,
                    ppalShinyFromNormalCheckBox);
        } else {
            disableAndDeselectButtons(ppalFollowTypesCheckBox, ppalFollowEvolutionsCheckBox,
                    ppalShinyFromNormalCheckBox);
        }

        cpgSelection.setEnabled(cpgCustomRadioButton.isSelected() && cpgCustomRadioButton.isVisible()
                && cpgCustomRadioButton.isEnabled());
        //*/
    }

    private void populateDropdowns() {
        List<Species> currentStarters = romHandler.getStarters();
        List<Species> allPokes =
                romHandler.generationOfPokemon() >= 6 ?
                        romHandler.getSpeciesInclFormes()
                                .stream()
                                .filter(pk -> pk == null || !pk.isEssentiallyCosmetic())
                                .toList() :
                        romHandler.getSpecies();
        String[] pokeNames = new String[allPokes.size()];
        pokeNames[0] = "Random";
        for (int i = 1; i < allPokes.size(); i++) {
            pokeNames[i] = allPokes.get(i).getFullName();

        }

        spCustom1ComboBox.setModel(new DefaultComboBoxModel<>(pokeNames));
        spCustom1ComboBox.setSelectedIndex(allPokes.indexOf(currentStarters.get(0)));
        spCustom2ComboBox.setModel(new DefaultComboBoxModel<>(pokeNames));
        spCustom2ComboBox.setSelectedIndex(allPokes.indexOf(currentStarters.get(1)));
        if (!romHandler.isYellow()) {
            spCustom3ComboBox.setModel(new DefaultComboBoxModel<>(pokeNames));
            spCustom3ComboBox.setSelectedIndex(allPokes.indexOf(currentStarters.get(2)));
        }

        int numTypes = romHandler.getTypeTable().getTypes().size();
        String[] typeNames = new String[numTypes + 1];
        typeNames[0] = "Random";
        for (int i = 1; i <= numTypes; i++) {
            typeNames[i] = Type.fromInt(i-1).toString();
        }
        spTypeSingleComboBox.setModel(new DefaultComboBoxModel<>(typeNames));

        String[] baseStatGenerationNumbers = new String[Math.min(4, GlobalConstants.HIGHEST_POKEMON_GEN - romHandler.generationOfPokemon())];
        int j = Math.max(6, romHandler.generationOfPokemon() + 1);
        for (int i = 0; i < baseStatGenerationNumbers.length; i++) {
            baseStatGenerationNumbers[i] = String.valueOf(j);
            j++;
        }

        String[] moveGenerationNumbers = new String[GlobalConstants.HIGHEST_POKEMON_GEN - romHandler.generationOfPokemon()];
        j = romHandler.generationOfPokemon() + 1;
        for (int i = 0; i < moveGenerationNumbers.length; i++) {
            moveGenerationNumbers[i] = String.valueOf(j);
            j++;
        }
        mdUpdateComboBox.setModel(new DefaultComboBoxModel<>(moveGenerationNumbers));
    }

    private ImageIcon makeMascotIcon() {
        try {
            BufferedImage handlerImg = new MascotGetter(RND).getMascotImage(romHandler);

            if (handlerImg == null) {
                return emptyIcon;
            }

            BufferedImage nImg = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            int hW = handlerImg.getWidth();
            int hH = handlerImg.getHeight();
            nImg.getGraphics().drawImage(handlerImg, 64 - hW / 2, 64 - hH / 2, frame);
            return new ImageIcon(nImg);
        } catch (Exception ex) {
            return emptyIcon;
        }
    }

    private void checkCustomNames() {
        if (OldCustomNamesImporter.hasOldNamesToImport()) {
            int response = JOptionPane.showConfirmDialog(frame,
                    bundle.getString("GUI.startup.convertNameFilesDialog.message"),
                    bundle.getString("GUI.startup.convertNameFilesDialog.title"), JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                try {
                    CustomNamesSet newNamesData = OldCustomNamesImporter.importOldNames();
                    CustomNamesSet.writeNamesToFile(newNamesData);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, bundle.getString("GUI.startup.convertNameFilesFailedDialog.message"));
                }
            }

            haveCheckedCustomNames = true;
            attemptWriteConfig();
        }
    }

    private void attemptReadConfig() {
        // Things that should be true by default should be manually set here
        unloadGameOnSuccess = true;
        batchRandomizationSettings = new BatchRandomizationSettings();
        File fh = new File(RootPath.path + "config.ini");
        if (!fh.exists() || !fh.canRead()) {
            return;
        }

        try {
            Scanner sc = new Scanner(fh, StandardCharsets.UTF_8);
            boolean isReadingUpdates = false;
            while (sc.hasNextLine()) {
                String q = sc.nextLine().trim();
                if (q.contains("//")) {
                    q = q.substring(0, q.indexOf("//")).trim();
                }
                if (q.equals("[Game Updates]")) {
                    isReadingUpdates = true;
                    continue;
                }
                if (!q.isEmpty()) {
                    String[] tokens = q.split("=", 2);
                    if (tokens.length == 2) {
                        String key = tokens[0].trim();
                        if (isReadingUpdates) {
                            gameUpdates.put(key, tokens[1]);
                        }

                        if (key.equals("theme")) {
                            Theme theme;
                            try {
                                theme = Theme.valueOf(tokens[1].trim());
                            } catch (IllegalArgumentException ignored) {
                                theme = Theme.DEFAULT;
                            }
                            setTheme(theme);

                        } else if (key.equals("checkedcustomnamesfvx")) {
                            // it is named like this to not overlap with ancient config vars;
                            // do NOT rename it to "checkedcustomnames", just in case someone comes
                            // along with a similarly ancient config it could cause troubles
                            haveCheckedCustomNames = Boolean.parseBoolean(tokens[1].trim());

                        } else if (key.equals("hasvisitedcustomnameseditor")) {
                            hasVisitedCustomNamesEditor = Boolean.parseBoolean(tokens[1].trim());

                        } else if (key.equals("firststart")) {
                            String val = tokens[1];
                            if (val.equals(Version.LATEST.name)) {
                                initialPopup = false;
                            }

                        } else if (key.equals("unloadgameonsuccess")) {
                            unloadGameOnSuccess = Boolean.parseBoolean(tokens[1].trim());

                        } else if (key.equals("showinvalidrompopup")) {
                            showInvalidRomPopup = Boolean.parseBoolean(tokens[1].trim());

                        } else if (key.equals("inputdirectory")) {
                            openDirectory = tokens[1].trim();

                        } else if (key.equals("outputdirectory")) {
                            saveDirectory = tokens[1].trim();

                        } else if (key.equals("batchrandomization.enabled")) {
                            batchRandomizationSettings.setBatchRandomizationEnabled(Boolean.parseBoolean(tokens[1].trim()));

                        } else if (key.equals("batchrandomization.generatelogfiles")) {
                            batchRandomizationSettings.setGenerateLogFile(Boolean.parseBoolean(tokens[1].trim()));

                        } else if (key.equals("batchrandomization.autoadvanceindex")) {
                            batchRandomizationSettings.setAutoAdvanceStartingIndex(Boolean.parseBoolean(tokens[1].trim()));

                        } else if (key.equals("batchrandomization.numberofrandomizedroms")) {
                            batchRandomizationSettings.setNumberOfRandomizedROMs(Integer.parseInt(tokens[1].trim()));

                        } else if (key.equals("batchrandomization.startingindex")) {
                            batchRandomizationSettings.setStartingIndex(Integer.parseInt(tokens[1].trim()));

                        } else if (key.equals("batchrandomization.filenameprefix")) {
                            batchRandomizationSettings.setFileNamePrefix(tokens[1].trim());

                        } else if (key.equals("batchrandomization.outputdirectory")) {
                            batchRandomizationSettings.setOutputDirectory(tokens[1].trim());

                        } else if (key.startsWith("lastusedcpg.")) {
                            String k = key.substring("lastusedcpg.".length());
                            lastUsedCPGConfigs.put(k, tokens[1].trim());
                        }
                    }
                } else if (isReadingUpdates) {
                    isReadingUpdates = false;
                }
            }
            sc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean attemptWriteConfig() {
        File fh = new File(RootPath.path + "config.ini");
        if (fh.exists() && !fh.canWrite()) {
            return false;
        }

        try {
            PrintStream ps = new PrintStream(Files.newOutputStream(fh.toPath()), true, StandardCharsets.UTF_8);
            ps.println("theme=" + theme);
            ps.println("checkedcustomnamesfvx=" + haveCheckedCustomNames);
            ps.println("hasvisitedcustomnameseditor=" + hasVisitedCustomNamesEditor);
            ps.println("unloadgameonsuccess=" + unloadGameOnSuccess);
            ps.println("showinvalidrompopup=" + showInvalidRomPopup);
            ps.println("inputdirectory=" + openDirectory);
            ps.println("outputdirectory=" + saveDirectory);
            ps.println(batchRandomizationSettings.toString());
            if (!initialPopup) {
                ps.println("firststart=" + Version.LATEST.name);
            }
            if (!gameUpdates.isEmpty()) {
                ps.println();
                ps.println("[Game Updates]");
                for (Map.Entry<String, String> update : gameUpdates.entrySet()) {
                    ps.format("%s=%s", update.getKey(), update.getValue());
                    ps.println();
                }
            }
            for (Map.Entry<String, String> entry : lastUsedCPGConfigs.entrySet()) {
                ps.println("lastusedcpg." + entry.getKey() + "=" + entry.getValue());
            }
            ps.close();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    private String[] getTrainerSettingsForGeneration(int generation) {
        List<String> result = new ArrayList<>(trainerSettings);
        if (generation != 5) {
            result.remove(bundle.getString("GUI.tpMain3RandomEvenDistributionMainGame.text"));
        }
        return result.toArray(new String[0]);
    }

    private String[] getBattleStylesForGeneration(int generation) {
        List<String> result = new ArrayList<>(selectableBattleStyles);
        if (generation <= 4 || generation >= 7) {
            result.remove(bundle.getString("GUI.foeTab.trainersPanel.battleStylePanel.excludeTripleCheckBox.text"));
            result.remove(bundle.getString("GUI.foeTab.trainersPanel.battleStylePanel.excludeRotationCheckBox.text"));
        }
        return result.toArray(new String[0]);
    }

    //TODO: remove these functions
    private boolean isTrainerSetting(int setting) {
        //return trainerSettings.indexOf(tpComboBox.getSelectedItem()) == setting;
        return true;
    }

    private boolean isBattleStyle(int setting) {
        //return selectableBattleStyles.indexOf(tpBattleStyleCombobox.getSelectedItem()) == setting;
        return true;
    }

    public static void main(String[] args) {
        setRootPath();

        String firstCliArg = args.length > 0 ? args[0] : "";
        // invoke as CLI program
        if (firstCliArg.equals("cli")) {
            // snip the "cli" flag arg off the args array and invoke command
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
            int exitCode = CliRandomizer.invoke(commandArgs);
            System.exit(exitCode);
        } else {
            launcherInput = firstCliArg;
            if (launcherInput.equals("please-use-the-launcher")) usedLauncher = true;
            SwingUtilities.invokeLater(() -> {
                frame = new JFrame("RandomizerGUI");
                frame.setContentPane(new RandomizerGUI().mainPanel);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            });
        }
    }

    private static void setRootPath() {
        // Honestly I don't know why the Randomizer needs a different RootPath from just "./",
        // but it was written in earlier versions so it feels safer to just keep it.
        // Feel free to investigate if you feel like it. Maybe it's entirely redundant.
        // --voliol 2025-04-27
        URL location = RandomizerGUI.class.getProtectionDomain().getCodeSource().getLocation();
        String file = location.getFile();
        String plusEncoded = file.replaceAll("\\+", "%2b");
        File f = new File(java.net.URLDecoder.decode(plusEncoded, StandardCharsets.UTF_8));
        RootPath.path = f.getParentFile() + File.separator;
    }
}
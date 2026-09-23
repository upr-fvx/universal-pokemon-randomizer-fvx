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

import com.uprfvx.random.BatchRandomizationSettings;
import com.uprfvx.random.GameRandomizer;
import com.uprfvx.random.SysConstants;
import com.uprfvx.random.Version;
import com.uprfvx.random.cli.CliRandomizer;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.random.customnames.OldCustomNamesImporter;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.random.gui.SettingElementCoordinators.*;
import com.uprfvx.random.random.SeedPicker;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.Settings.*;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.RootPath;
import com.uprfvx.romio.exceptions.CannotWriteToLocationException;
import com.uprfvx.romio.exceptions.EncryptedROMException;
import com.uprfvx.romio.gamedata.ExpCurve;
import com.uprfvx.romio.graphics.packs.CustomPlayerGraphics;
import com.uprfvx.romio.romhandlers.*;
import com.uprfvx.romio.romio.ROMFilter;
import com.uprfvx.romio.romio.RomOpener;
import filefunctions.FileNameFunctions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JLabel romNameLabel;
    private JLabel romCodeLabel;
    private JLabel romSupportLabel;
    private JCheckBox raceModeCheckBox;
    private JButton loadSettingsButton;
    private JButton saveSettingsButton;
    private JLabel versionLabel;

    //Header center
    private JLabel gameMascotLabel;

    //Header right
    private JButton openROMButton;
    private JButton randomizeSaveButton;
    private JButton premadeSeedButton;
    private JButton settingsButton;
    private JLabel websiteLinkLabel;
    private JLabel wikiLinkLabel;

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
    private JSpinner sbsUpdateGenerationChoiceSpinner;
    //--Totals
    private JRadioButton sbstUnchangedRadioButton;
    private JRadioButton sbstRandomBuffNerfRadioButton;
    private SpinSlider sbstRandomBuffNerfSpinSlider;
    private JRadioButton sbstShuffleRadioButton;
    private JRadioButton sbstRandomRadioButton;
    private JCheckBox sbstFollowEvolutionsCheckBox;
    private JCheckBox sbstSwapLegendariesCheckBox;
    //--Distribution
    private JRadioButton sbsdUnchangedRadioButton;
    private JRadioButton sbsdShuffleRadioButton;
    private JRadioButton sbsdRandomRadioButton;
    private JCheckBox sbsdFollowEvolutionsCheckBox;
    private JCheckBox sbsdFollowMegaEvosCheckBox;
    private JCheckBox sbsdAssignEvoStatsRandomlyCheckBox;

    //Types
    private JRadioButton stUnchangedRadioButton;
    private JRadioButton stRandomFollowEvolutionsRadioButton;
    private JRadioButton stRandomCompletelyRadioButton;
    private JCheckBox stFollowMegaEvosCheckBox;
    private JCheckBox stForceDualTypeCheckBox;
    private JCheckBox stUpdateRotomCheckBox;

    //Abilities
    private JPanel speciesAbilitiesPanel;
    private JRadioButton saUnchangedRadioButton;
    private JRadioButton saRandomRadioButton;
    private JCheckBox saForceTwoAbilitiesCheckbox;
    private JCheckBox saWeighDuplicatesTogetherCheckBox;
    private JCheckBox saFollowEvolutionsCheckBox;
    private JCheckBox saFollowMegaEvosCheckBox;
    //--Ban
    private JPanel speciesBanAbilitiesPanel;
    private JCheckBox saBanWonderGuardCheckBox;
    private JCheckBox saBanTrappingAbilitiesCheckBox;
    private JCheckBox saBanNegativeAbilitiesCheckBox;
    private JCheckBox saBanMinorAbilitiesCheckBox;

    //Evolutions
    private JCheckBox peChangeImpossibleEvosCheckBox;
    private JCheckBox peRemoveTimeBasedEvolutionsCheckBox;
    private JCheckBox peAllowPikachuEvolutionCheckBox;
    private JCheckBox peUseEstimatedInsteadOfHardcodedLevelsCheckBox;
    private JCheckBox peMakeEvolutionsEasierCheckBox;
    private JSlider peMakeEvolutionsEasierLvlSlider;
    //--Randomize
    private JRadioButton peUnchangedRadioButton;
    private JRadioButton peRandomRadioButton;
    private JRadioButton peRandomEveryLevelRadioButton;
    private JCheckBox peSimilarStrengthCheckBox;
    private JCheckBox peShareTypingCheckBox;
    private JCheckBox peMaxThreeStagesCheckBox;
    private JCheckBox peAllowAltFormesCheckBox;
    private JCheckBox peForceChangeCheckBox;
    private JCheckBox peForceGrowthCheckBox;
    private JCheckBox peNoConvergenceCheckBox;
    private JCheckBox peAdjustLevelsCheckBox;

    //EXP Curves
    private JCheckBox secStandardizeEXPCurvesCheckBox;
    private JComboBox<String> secEXPCurveComboBox;
    private JRadioButton secLegendariesSlowRadioButton;
    private JRadioButton secStrongLegendariesSlowRadioButton;
    private JRadioButton secAllSpeciesRadioButton;

    //endregion

    //region *** Given Pokemon tab ***

    //Starter Pokemon
    private JRadioButton spUnchangedRadioButton;
    private JRadioButton spCustomRadioButton;
    private JRadioButton spRandomRadioButton;
    private JComboBox<String> spCustom1ComboBox;
    private JComboBox<String> spCustom2ComboBox;
    private JComboBox<String> spCustom3ComboBox;
    private JLabel spBSTLimitsLabel;
    private JCheckBox spBSTMinimumCheckbox;
    private JCheckBox spBSTMaximumCheckbox;
    private JSpinner spBSTMinimumSpinner;
    private JSpinner spBSTMaximumSpinner;
    private JCheckBox spNoLegendariesCheckBox;
    private JCheckBox spRandomizeStarterHeldItemsCheckBox;
    private JCheckBox spBanMinorItemsCheckBox;
    private JCheckBox spAllowAltFormesCheckBox;
    //--Evolution Restrictions
    private JCheckBox spBasicOnlyCheckBox;
    private JCheckBox spHasEvolutionsCheckBox;
    private JSlider spHasEvolutionCountSlider;
    //--Type Restrictions
    private JRadioButton spTypeNoneRadioButton;
    private JRadioButton spTypeFwgRadioButton;
    private JRadioButton spTypeTriangleRadioButton;
    private JRadioButton spTypeSingleRadioButton;
    private JComboBox<String> spTypeSingleComboBox;
    private JRadioButton spTypeUniqueRadioButton;
    private JCheckBox spTypeNoDualCheckbox;

    //In-Game Trades
    private JCheckBox igtRandomizeGivenSpeciesCheckBox;
    private JCheckBox igtRandomizeRequestedSpeciesCheckBox;
    private JCheckBox igtRandomizeNicknamesCheckBox;
    private JCheckBox igtRandomizeOTsCheckBox;
    private JCheckBox igtRandomizeIVsCheckBox;
    private JCheckBox igtRandomizeItemsCheckBox;

    private JLabel giftsInStaticsLabel;

    //endregion

    //region *** Moves and Movesets ***

    //Move Traits
    private JCheckBox mtRandomizeMovePowerCheckBox;
    private JCheckBox mtRandomizeMoveAccuracyCheckBox;
    private JCheckBox mtRandomizeMovePPCheckBox;
    private JCheckBox mtRandomizeMoveTypesCheckBox;
    private JCheckBox mtRandomizeMoveCategoryCheckBox;
    private JCheckBox mtRandomizeMoveNamesCheckBox;
    private JCheckBox mdUpdateMovesCheckBox;
    private JComboBox<String> mdUpdateComboBox;

    //Species Learned Movesets
    private JRadioButton slmUnchangedRadioButton;
    private JRadioButton slmRandomPreferringSameTypeRadioButton;
    private JRadioButton slmRandomCompletelyRadioButton;
    private JRadioButton slmMetronomeOnlyModeRadioButton;
    private JCheckBox slmGuaranteedLevel1MovesCheckBox;
    private JSlider slmGuaranteedLevel1MovesSlider;
    private JCheckBox slmReorderDamagingMovesCheckBox;
    private JCheckBox slmNoGameBreakingMovesCheckBox;
    private JCheckBox slmForceGoodDamagingCheckBox;
    private SpinSlider slmForceGoodDamagingSpinSlider;
    private JCheckBox slmEvolutionMovesCheckBox;

    //endregion

    //region *** Foe Pokemon ***

    //Trainer Pokemon
    private JCheckBox tpRandomizeTrainerPokemonCheckBox;
    //--B-I-R column
    private JLabel tpAdditionalPokemonForLabel;
    private JCheckBox tpAddToBossTrainersCheckBox;
    private JSpinner tpAddToBossTrainersSpinner;
    private JCheckBox tpAddToImportantTrainersCheckBox;
    private JSpinner tpAddToImportantTrainersSpinner;
    private JCheckBox tpAddToRegularTrainersCheckBox;
    private JSpinner tpAddToRegularTrainersSpinner;
    private JLabel tpBetterMovesetsLabel;
    private JCheckBox tpBetterMovesetsBossTrainersCheckBox;
    private JCheckBox tpBetterMovesetsImportantTrainersCheckBox;
    private JCheckBox tpBetterMovesetsRegularTrainersCheckBox;
    private JLabel tpHeldItemsLabel;
    private JCheckBox tpBossTrainersItemsCheckBox;
    private JCheckBox tpImportantTrainersItemsCheckBox;
    private JCheckBox tpRegularTrainersItemsCheckBox;
    private JCheckBox tpConsumableItemsOnlyCheckBox;
    private JCheckBox tpSensibleItemsCheckBox;
    private JCheckBox tpHighestLevelGetsItemCheckBox;
    private JLabel tpTypeDiversityLabel;
    private JCheckBox tpBossTrainersTypeDiversityCheckBox;
    private JCheckBox tpImportantTrainersTypeDiversityCheckBox;
    private JCheckBox tpRegularTrainersTypeDiversityCheckBox;
    //--Battle Style
    private JPanel tpBattleStylePanel;
    private JRadioButton tbsUnchangedStyleRadioButton;
    private JRadioButton tbsRandomStyleRadioButton;
    private JCheckBox tbsExtendTeamsCheckBox;
    private JCheckBox tbsExcludeSingleBattlesCheckBox;
    private JCheckBox tbsExcludeDoubleBattlesCheckBox;
    private JCheckBox tbsExcludeTripleBattlesCheckBox;
    private JCheckBox tbsExcludeRotationBattlesCheckBox;
    //--Type Restrictions
    private JRadioButton tpTypesUnrestrictedRadioButton;
    private JRadioButton tpRandomTypeThemesRadioButton;
    private JRadioButton tpKeepTypeThemesRadioButton;
    private JRadioButton tpKeepThemesOrPrimaryRadioButton;
    private JCheckBox tpTypeGymsAndElitesOnlyCheckBox;
    private JCheckBox tpWeightTypesCheckBox;
    //--Bools column
    private JCheckBox tpUseLocalPokemonCheckBox;
    private JCheckBox tpDontUseLegendariesCheckBox;
    private JCheckBox tpAllowAlternateFormesCheckBox;
    private JCheckBox tpSimilarStrengthCheckBox;
    private JCheckBox tpDistributeSpeciesCheckBox;
    private JCheckBox tpDistributeInMainGameOnly;
    private JCheckBox tpAvoidDuplicatesCheckBox;
    private JCheckBox tpNoEarlyWonderGuardCheckBox;
    private JCheckBox tpRivalCarriesStarterCheckBox;
    private JCheckBox tpSwapMegaEvosCheckBox;
    private JCheckBox tpRandomShinyTrainerPokemonCheckBox;
    //--Quantified column
    private JCheckBox tpEliteFourUniquePokemonCheckBox;
    private JSpinner tpEliteFourUniquePokemonSpinner;
    private JCheckBox tpTrainersEvolveTheirPokemonCheckbox;
    private SpinSlider tpPercentageEvolutionLevelModifierSpinSlider;
    private JLabel tpCalculatedFullyEvolvedLvlLabel;
    private JCheckBox tpPercentageLevelModifierCheckBox;
    private SpinSlider tpPercentageLevelModifierSpinSlider;

    //Totem Pokemon
    private JPanel totpPanel;
    private JRadioButton totpUnchangedRadioButton;
    private JRadioButton totpRandomRadioButton;
    private JRadioButton totpRandomSimilarStrengthRadioButton;
    private JCheckBox totpRandomizeHeldItemsCheckBox;
    private JCheckBox totpAllowAltFormesCheckBox;
    private JCheckBox totpPercentageLevelModifierCheckBox;
    private SpinSlider totpPercentageLevelModifierSpinSlider;
    //--Allies
    private JPanel totpAllyPanel;
    private JRadioButton totpAllyUnchangedRadioButton;
    private JRadioButton totpAllyRandomRadioButton;
    private JRadioButton totpAllyRandomSimilarStrengthRadioButton;
    //--Auras
    private JPanel totpAuraPanel;
    private JRadioButton totpAuraUnchangedRadioButton;
    private JRadioButton totpAuraRandomRadioButton;
    private JRadioButton totpAuraRandomSameStrengthRadioButton;

    //endregion

    //region *** Wild Pokemon ***

    //Random Encounters
    private JCheckBox wpRandomizeWildPokemonCheckBox;
    //--Replacement Zone
    private JRadioButton wpZoneGameRadioButton;
    private JRadioButton wpZoneNamedLocationRadioButton;
    private JRadioButton wpZoneMapRadioButton;
    private JCheckBox wpSplitByEncounterTypesCheckBox;
    private JRadioButton wpZoneEncounterSetRadioButton;
    private JRadioButton wpZoneNoneRadioButton;
    private JCheckBox wpRemoveTimeBasedEncountersCheckBox;
    //--Type Restrictions
    private JRadioButton wpTRNoneRadioButton;
    private JRadioButton wpTRThemedAreasRadioButton;
    private JRadioButton wpTRKeepPrimaryRadioButton;
    private JCheckBox wpTRKeepThemesCheckBox;
    //--Evolution Restrictions
    private JRadioButton wpERNoneRadioButton;
    private JRadioButton wpERBasicOnlyRadioButton;
    private JRadioButton wpERSameEvolutionStageRadioButton;
    private JCheckBox wpERKeepEvolutionsCheckBox;
    //--Randomization Options
    private JCheckBox wpDontUseLegendariesCheckBox;
    private JCheckBox wpCatchEmAllModeCheckBox;
    private JCheckBox wpSimilarStrengthCheckBox;
    private JCheckBox wpBalanceShakingGrassPokemonCheckBox;
    private JCheckBox wpAllowAltFormesCheckBox;
    //--Other Options
    private JCheckBox wpRandomizeHeldItemsCheckBox;
    private JCheckBox wpBanMinorItemsCheckBox;
    private JCheckBox wpSetMinimumCatchRateCheckBox;
    private JSlider wpSetMinimumCatchRateSlider;
    private JCheckBox wpPercentageLevelModifierCheckBox;
    private SpinSlider wpPercentageLevelModifierSpinSlider;
    private JCheckBox wpSOSForAllCheckBox;

    //Static Encounters
    private JRadioButton seUnchangedRadioButton;
    private JRadioButton seSwapLegendariesSwapStandardsRadioButton;
    private JRadioButton seRandomCompletelyRadioButton;
    private JRadioButton seRandomSimilarStrengthRadioButton;
    private JCheckBox seRandomize600BSTCheckBox;
    private JCheckBox seLimitMainGameLegendariesCheckBox;
    private JCheckBox seAllowAltFormesCheckBox;
    private JCheckBox seSwapMegaEvosCheckBox;
    private JCheckBox seFixMusicCheckBox;
    private JCheckBox sePercentageLevelModifierCheckBox;
    private SpinSlider sePercentageLevelModifierSpinSlider;
    private JCheckBox seBalanceGivenLevelsCheckBox;

    //endregion

    //region *** Move Teaching ***

    //TMs & HMs
    //--Moves
    private JRadioButton tmmUnchangedRadioButton;
    private JRadioButton tmmRandomRadioButton;
    private JCheckBox tmmNoGameBreakingMovesCheckBox;
    private JCheckBox tmmKeepFieldMoveTMsCheckBox;
    private JCheckBox tmmForceGoodDamagingCheckBox;
    private SpinSlider tmmForceGoodDamagingSpinSlider;
    //--Compatibility
    private JRadioButton thcUnchangedRadioButton;
    private JRadioButton thcRandomPreferSameTypeRadioButton;
    private JRadioButton thcRandomCompletelyRadioButton;
    private JRadioButton thcFullCompatibilityRadioButton;
    private JCheckBox thcLevelupMoveSanityCheckBox;
    private JCheckBox thcFollowEvolutionsCheckBox;
    private JCheckBox thcFullHMCompatibilityCheckBox;

    //Tutors
    private JPanel tutorsPanel;
    private JLabel mtNoExistLabel;
    //--Moves
    private JPanel mtMovesPanel;
    private JRadioButton mtmUnchangedRadioButton;
    private JRadioButton mtmRandomRadioButton;
    private JCheckBox mtmNoGameBreakingMovesCheckBox;
    private JCheckBox mtmKeepFieldMoveTutorsCheckBox;
    private JCheckBox mtmForceGoodDamagingCheckBox;
    private SpinSlider mtmForceGoodDamagingSpinSlider;
    //--Compatibility
    private JPanel mtCompatPanel;
    private JRadioButton mtcUnchangedRadioButton;
    private JRadioButton mtcRandomPreferSameTypeRadioButton;
    private JRadioButton mtcRandomCompletelyRadioButton;
    private JRadioButton mtcFullCompatibilityRadioButton;
    private JCheckBox mtcLevelupMoveSanityCheckBox;
    private JCheckBox mtcFollowEvolutionsCheckBox;

    //endregion

    //region *** Items ***

    //General
    private JPanel generalItemsPanel;
    private JCheckBox giBanLuckyEggCheckBox;
    private JCheckBox giBanBigMoneyManiacCheckBox;
    private JCheckBox giRandomizePCPotionCheckBox;
    private JCheckBox giNoFreeLuckyEggCheckBox;

    //Field Items
    private JRadioButton fiUnchangedRadioButton;
    private JRadioButton fiShuffleRadioButton;
    private JRadioButton fiRandomRadioButton;
    private JRadioButton fiRandomEvenDistributionRadioButton;
    private JCheckBox fiBanMinorItemsCheckBox;

    //Shop Items
    private JPanel shopItemsPanel;
    private JCheckBox shBalanceShopItemPricesCheckBox;
    private JCheckBox shAddRareCandyCheckBox;
    //--Special Shops
    private JPanel specialShopsPanel;
    private JRadioButton shUnchangedRadioButton;
    private JRadioButton shShuffleRadioButton;
    private JRadioButton shRandomRadioButton;
    private JCheckBox shBanMinorItemsCheckBox;
    private JCheckBox shBanRegularShopItemsCheckBox;
    private JCheckBox shBanOverpoweredShopItemsCheckBox;
    private JCheckBox shGuaranteeEvolutionItemsCheckBox;
    private JCheckBox shGuaranteeXItemsCheckBox;

    //Pickup Items
    private JPanel pickupItemsPanel;
    private JRadioButton puUnchangedRadioButton;
    private JRadioButton puRandomRadioButton;
    private JCheckBox puBanMinorItemsCheckBox;

    //endregion

    //region *** Types ***
    private JPanel typesPanel;

    //Type Effectiveness
    private JRadioButton teUnchangedRadioButton;
    private JRadioButton teRandomRadioButton;
    private JRadioButton teRandomBalancedRadioButton;
    private JRadioButton teKeepTypeIdentitiesRadioButton;
    private JRadioButton teInverseRadioButton;
    private JCheckBox teAddRandomImmunitiesCheckBox;
    private JCheckBox teUpdateCheckbox;

    //endregion

    //region *** Graphics ***

    private JPanel graphicsPanel;
    //Species Palettes
    private JLabel spalNotExistLabel;
    private JLabel spalPartiallyImplementedLabel;
    private JRadioButton spalUnchangedRadioButton;
    private JRadioButton spalRandomRadioButton;
    private JCheckBox spalFollowTypesCheckBox;
    private JCheckBox spalFollowEvolutionsCheckBox;
    private JCheckBox spalShinyFromNormalCheckBox;

    //Custom Player Graphics
    private JLabel cpgNotExistLabel;
    private JRadioButton cpgUnchangedRadioButton;
    private JRadioButton cpgCustomRadioButton;
    private CPGSelection cpgSelection;

    // TODO: move panels up
    private JPanel banSpeciesPanel;
    private JPanel limitPanel;
    private JPanel statsPanel;
    private JPanel totalsPanel;
    private JPanel distPanel;
    private JPanel traitsTypesPanel;
    private JPanel evolutionPanel;
    private JPanel expCurvesPanel;
    private JPanel startersPanel;
    private JPanel spTypesPanel;
    private JPanel spEvolutionPanel;
    private JPanel tradesPanel;
    private JPanel moveTraitsPanel;
    private JPanel movesetsPanel;
    private JPanel trainersPanel;
    private JPanel tpTypesPanel;
    private JPanel tpInnerPanel;
    private JPanel wpPanel;
    private JPanel wpReplacementsPanel;
    private JPanel wpTypesPanel;
    private JPanel wpEvolutionPanel;
    private JPanel staticsPanel;
    private JPanel tmsPanel;
    private JPanel tmCompatPanel;
    private JPanel tmMovesPanel;
    private JPanel fieldItemsPanel;
    private JPanel palettesPanel;
    private JPanel customPlayerPanel;

    //endregion

    //endregion

    private static final Random RND = new Random();

    private static JFrame frame;

    public static boolean usedLauncher = false;

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

    private final SettingsManager settingsManager;

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
        initVisibilityListeners();

        openROMButton.addActionListener(_ -> selectAndOpenRom());

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

        //If nothing else, the List will be helpful for test purposes.

        //--foxoftheasterisk, August 2026

        List<SettingCoordinator<?, ?>> settingUICoordinators = List.of(
                //TODO: remove I think
                associateCheckBox(Name.RACE_MODE, raceModeCheckBox),

                // *** GENERAL ***
                //Cosmetic
                associateCheckBox(Name.COSMETIC_RANDOM_INTRO_MON, coRandomIntroMonCheckBox),
                associateCheckBox(Name.COSMETIC_RANDOMIZE_CATCHING_TUTORIAL, coRandomizeCatchingTutorialCheckBox),
                associateCheckBox(Name.COSMETIC_LOWER_CASE_SPECIES_NAMES, coLowerCaseSpeciesNamesCheckBox),
                associateCheckBox(Name.COSMETIC_RANDOMIZE_TRAINER_NAMES, coRandomizeTrainerNamesCheckBox),
                associateCheckBox(Name.COSMETIC_RANDOMIZE_TRAINER_CLASS_NAMES, coRandomizeTrainerClassNamesCheckBox),

                //Limit Species
                associateCheckBox(Name.LIMIT_BAN_GENERATION_1, lsBanGeneration1CheckBox),
                associateCheckBox(Name.LIMIT_BAN_GENERATION_2, lsBanGeneration2CheckBox),
                associateCheckBox(Name.LIMIT_BAN_GENERATION_3, lsBanGeneration3CheckBox),
                associateCheckBox(Name.LIMIT_BAN_GENERATION_4, lsBanGeneration4CheckBox),
                associateCheckBox(Name.LIMIT_BAN_GENERATION_5, lsBanGeneration5CheckBox),
                associateCheckBox(Name.LIMIT_BAN_GENERATION_6, lsBanGeneration6CheckBox),
                associateCheckBox(Name.LIMIT_BAN_GENERATION_7, lsBanGeneration7CheckBox),
                associateCheckBox(Name.LIMIT_ALLOW_RELATIVES, lsAllowRelativesCheckBox),
                associateCheckBox(Name.LIMIT_NO_TEMPORARY_ALT_FORMES, lsNoIrregularAltFormesCheckBox),
                associateCheckBox(Name.LIMIT_RETAIN_TEMPORARY_FORMES, lsRetainAltFormesCheckBox),
                associateCheckBox(Name.LIMIT_NO_PREMATURE_EVOLUTIONS, lsNoPrematureEvosCheckbox),

                //Quality of Life Tweaks
                associateCheckBox(Name.QUALITY_FASTEST_TEXT, qoltFastestTextCheckBox),
                associateCheckBox(Name.QUALITY_RUN_INDOORS, qoltRunIndoorsCheckBox),
                associateCheckBox(Name.QUALITY_NATIONAL_DEX_AT_START, qoltNationalDexCheckBox),
                associateCheckBox(Name.QUALITY_RUN_WITHOUT_RUNNING_SHOES, qoltRunWithoutRunningShoesCheckBox),
                associateCheckBox(Name.QUALITY_FASTER_HP_AND_EXP_BARS, qoltFasterHPAndEXPBarsCheckBox),
                associateCheckBox(Name.QUALITY_FAST_DISTORTION_WORLD, qoltFastDistortionWorldCheckBox),
                associateCheckBox(Name.QUALITY_DISABLE_LOW_HP_MUSIC, qoltDisableLowHPMusicCheckBox),
                associateCheckBox(Name.QUALITY_FAST_EGG_HATCHING, qoltFastEggsCheckBox),
                associateCheckBox(Name.QUALITY_REUSABLE_TMS, qoltReusableTMsCheckBox),
                associateCheckBox(Name.QUALITY_FORGETTABLE_HMS, qoltForgettableHMsCheckBox),

                //Balance Tweaks
                associateCheckBox(Name.BALANCE_NERF_X_ACCURACY, btNerfXAccuracyCheckBox),
                associateCheckBox(Name.BALANCE_UPDATE_CRIT_RATE, btUpdateCritRateCheckBox),
                associateCheckBox(Name.BALANCE_USE_SCALED_EXPERIENCE,btScalingEXPCheckBox),
                associateCheckBox(Name.BALANCE_FORCE_CHALLENGE_MODE, btForceChallengeModeCheckBox),
                associateCheckBox(Name.BALANCE_NO_EV_YIELDS, btNoEVYieldsCheckBox),

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
                associateCheckBox(Name.SPECIES_TYPES_UPDATE_ROTOM_TYPING, stUpdateRotomCheckBox),

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
                associateCheckBox(Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE, peChangeImpossibleEvosCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED, peRemoveTimeBasedEvolutionsCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_ALLOW_PIKACHU_EVOLUTION, peAllowPikachuEvolutionCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_CHANGES_USE_ESTIMATED_LEVELS, peUseEstimatedInsteadOfHardcodedLevelsCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_MAKE_EASIER, peMakeEvolutionsEasierCheckBox),
                associateSlider(Name.SPECIES_EVOLUTIONS_EASIER_SCALING_LEVEL, peMakeEvolutionsEasierLvlSlider),
                //--Randomize
                associateButtonSet(Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                        Map.of(
                                EvolutionsMod.UNCHANGED, peUnchangedRadioButton,
                                EvolutionsMod.RANDOM, peRandomRadioButton,
                                EvolutionsMod.RANDOM_EVERY_LEVEL, peRandomEveryLevelRadioButton
                        )),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_USE_SIMILAR_STRENGTH, peSimilarStrengthCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_STAGES_MUST_SHARE_TYPE, peShareTypingCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_MAX_THREE_STAGES, peMaxThreeStagesCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_ALLOW_ALT_FORMES, peAllowAltFormesCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_FORCE_CHANGE, peForceChangeCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_FORCE_GROWTH, peForceGrowthCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_NO_CONVERGENCE, peNoConvergenceCheckBox),
                associateCheckBox(Name.SPECIES_EVOLUTIONS_ADJUST_LEVELS_FOR_STRENGTH, peAdjustLevelsCheckBox),

                //EXP Curves
                associateCheckBox(Name.STANDARDIZE_SPECIES_EXP_CURVES, secStandardizeEXPCurvesCheckBox),
                associateComboBoxUsingToString(Name.SPECIES_EXP_CURVE_STANDARD_SELECTION, secEXPCurveComboBox,
                        Arrays.asList(ExpCurve.values())),
                associateButtonSet(Name.SPECIES_EXP_CURVE_STANDARDIZE_EXTENT,
                        Map.of(
                                ExpCurveExtentMod.ALL, secAllSpeciesRadioButton,
                                ExpCurveExtentMod.STRONG_LEGENDARIES, secStrongLegendariesSlowRadioButton,
                                ExpCurveExtentMod.LEGENDARIES, secLegendariesSlowRadioButton
                        )),

                // *** GIVEN POKEMON ***
                //Starters
                associateButtonSet(Name.RANDOMIZE_STARTERS,
                        Map.of(
                                StartersMod.UNCHANGED, spUnchangedRadioButton,
                                StartersMod.CUSTOM, spCustomRadioButton,
                                StartersMod.RANDOM, spRandomRadioButton
                        )),
                //TODO: figure out how to handle species combo boxes
                associateSpinner(Name.STARTERS_BST_MINIMUM, spBSTMinimumSpinner, spBSTMinimumCheckbox),
                associateSpinner(Name.STARTERS_BST_MAXIMUM, spBSTMaximumSpinner, spBSTMaximumCheckbox),
                associateCheckBox(Name.STARTERS_NO_LEGENDARIES, spNoLegendariesCheckBox),
                associateCheckBox(Name.STARTERS_RANDOMIZE_HELD_ITEMS, spRandomizeStarterHeldItemsCheckBox),
                associateCheckBox(Name.STARTERS_BAN_MINOR_HELD_ITEMS, spBanMinorItemsCheckBox),
                associateCheckBox(Name.STARTERS_ALLOW_ALT_FORMES, spAllowAltFormesCheckBox),
                //--Evolution Restrictions
                associateCheckBox(Name.STARTERS_BASIC_ONLY, spBasicOnlyCheckBox),
                associateSlider(Name.STARTERS_MINIMUM_EVOLUTION_STAGES, spHasEvolutionCountSlider,
                        spHasEvolutionsCheckBox),
                //--Type Restrictions
                associateButtonSet(Name.STARTERS_TYPE_RESTRICTION,
                        Map.of(
                                StartersTypeMod.NONE, spTypeNoneRadioButton,
                                StartersTypeMod.FIRE_WATER_GRASS, spTypeFwgRadioButton,
                                StartersTypeMod.TRIANGLE, spTypeTriangleRadioButton,
                                StartersTypeMod.UNIQUE, spTypeUniqueRadioButton,
                                StartersTypeMod.SINGLE_TYPE, spTypeSingleRadioButton
                        )),
                associateCheckBox(Name.STARTERS_NO_DUAL_TYPES, spTypeNoDualCheckbox),
                //TODO: also figure out handling for types+random combobox

                // In-Game Trades
                // TODO: how to associate an underlying enum to two checkboxes?
                associateCheckBox(Name.TRADES_RANDOMIZE_NICKNAMES, igtRandomizeNicknamesCheckBox),
                associateCheckBox(Name.TRADES_RANDOMIZE_ORIGINAL_TRAINERS, igtRandomizeOTsCheckBox),
                associateCheckBox(Name.TRADES_RANDOMIZE_IVS, igtRandomizeIVsCheckBox),
                associateCheckBox(Name.TRADES_RANDOMIZE_HELD_ITEMS, igtRandomizeItemsCheckBox),

                // *** MOVES & MOVESETS ***
                //Move Traits
                associateCheckBox(Name.MOVES_RANDOMIZE_POWER, mtRandomizeMovePowerCheckBox),
                associateCheckBox(Name.MOVES_RANDOMIZE_ACCURACY, mtRandomizeMoveAccuracyCheckBox),
                associateCheckBox(Name.MOVES_RANDOMIZE_PP, mtRandomizeMovePPCheckBox),
                associateCheckBox(Name.MOVES_RANDOMIZE_TYPE, mtRandomizeMoveTypesCheckBox),
                associateCheckBox(Name.MOVES_RANDOMIZE_CATEGORY, mtRandomizeMoveCategoryCheckBox),
                associateCheckBox(Name.MOVES_RANDOMIZE_NAME, mtRandomizeMoveNamesCheckBox),
                associateCheckBox(Name.UPDATE_MOVES, mdUpdateMovesCheckBox),
                // TODO: this numeric combo box

                //Species Learned Movesets
                associateButtonSet(Name.RANDOMIZE_SPECIES_MOVESETS,
                        Map.of(
                            MovesetsMod.UNCHANGED, slmUnchangedRadioButton,
                            MovesetsMod.RANDOM_PREFER_SAME_TYPE, slmRandomPreferringSameTypeRadioButton,
                            MovesetsMod.COMPLETELY_RANDOM, slmRandomCompletelyRadioButton,
                            MovesetsMod.METRONOME_ONLY, slmMetronomeOnlyModeRadioButton
                        )),
                associateSlider(Name.MOVESETS_GUARANTEED_LEVEL_1_MOVE_COUNT, slmGuaranteedLevel1MovesSlider,
                        slmGuaranteedLevel1MovesCheckBox),
                associateCheckBox(Name.MOVESETS_ORDER_BY_DAMAGE, slmReorderDamagingMovesCheckBox),
                associateCheckBox(Name.MOVESETS_BAN_OVERPOWERED, slmNoGameBreakingMovesCheckBox),
                associateSpinSlider(Name.MOVESETS_FORCE_GOOD_DAMAGING_PERCENT, slmForceGoodDamagingSpinSlider,
                        slmForceGoodDamagingCheckBox),
                associateCheckBox(Name.MOVESETS_GUARANTEE_EVOLUTION_MOVES, slmEvolutionMovesCheckBox),

                // *** FOE POKEMON ***
                //Trainer Pokemon
                // TODO: the main trainer mon enum
                //--B-I-R column
                associateSpinner(Name.TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT, tpAddToBossTrainersSpinner,
                        tpAddToBossTrainersCheckBox),
                associateSpinner(Name.TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT, tpAddToImportantTrainersSpinner,
                        tpAddToImportantTrainersCheckBox),
                associateSpinner(Name.TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT, tpAddToRegularTrainersSpinner,
                        tpAddToRegularTrainersCheckBox),
                associateCheckBox(Name.TRAINERS_BETTER_MOVESETS_FOR_BOSSES, tpBetterMovesetsBossTrainersCheckBox),
                associateCheckBox(Name.TRAINERS_BETTER_MOVESETS_FOR_IMPORTANT, tpBetterMovesetsImportantTrainersCheckBox),
                associateCheckBox(Name.TRAINERS_BETTER_MOVESETS_FOR_REGULAR, tpBetterMovesetsRegularTrainersCheckBox),
                associateCheckBox(Name.TRAINERS_ADD_HELD_ITEMS_TO_BOSSES, tpBossTrainersItemsCheckBox),
                associateCheckBox(Name.TRAINERS_ADD_HELD_ITEMS_TO_IMPORTANT, tpImportantTrainersItemsCheckBox),
                associateCheckBox(Name.TRAINERS_ADD_HELD_ITEMS_TO_REGULAR, tpRegularTrainersItemsCheckBox),
                associateCheckBox(Name.TRAINERS_HELD_ITEMS_CONSUMABLE_ONLY, tpConsumableItemsOnlyCheckBox),
                associateCheckBox(Name.TRAINER_HELD_ITEMS_SENSIBLE_ONLY, tpSensibleItemsCheckBox),
                associateCheckBox(Name.TRAINERS_HELD_ITEMS_ACES_ONLY, tpHighestLevelGetsItemCheckBox),
                associateCheckBox(Name.TRAINERS_BOSSES_USE_DIVERSE_TYPES, tpBossTrainersTypeDiversityCheckBox),
                associateCheckBox(Name.TRAINERS_IMPORTANT_USE_DIVERSE_TYPES, tpImportantTrainersTypeDiversityCheckBox),
                associateCheckBox(Name.TRAINERS_REGULAR_USE_DIVERSE_TYPES, tpRegularTrainersTypeDiversityCheckBox),
                //--Battle Style
                // TODO: what to do with battle styles? the GUI does not match the extant settings/internal logic.
                //  Implement new functionaly or revert GUI?
                //--Bools column
                associateCheckBox(Name.TRAINERS_USE_LOCAL, tpUseLocalPokemonCheckBox),
                associateCheckBox(Name.TRAINERS_NO_LEGENDARIES, tpDontUseLegendariesCheckBox),
                associateCheckBox(Name.TRAINERS_ALLOW_ALT_FORMES, tpAllowAlternateFormesCheckBox),
                associateCheckBox(Name.TRAINERS_USE_SIMILAR_STRENGTH, tpSimilarStrengthCheckBox),
                associateCheckBox(Name.TRAINERS_AVOID_DUPLICATES, tpAvoidDuplicatesCheckBox),
                associateCheckBox(Name.TRAINERS_NO_EARLY_WONDER_GUARD, tpNoEarlyWonderGuardCheckBox),
                associateCheckBox(Name.TRAINERS_RIVAL_CARRIES_STARTER, tpRivalCarriesStarterCheckBox),
                associateCheckBox(Name.TRAINERS_SWAP_MEGA_EVOLVABLES, tpSwapMegaEvosCheckBox),
                associateCheckBox(Name.TRAINERS_RANDOM_SHINY_POKEMON, tpRandomShinyTrainerPokemonCheckBox),
                //--Quantified column
                associateSpinner(Name.TRAINERS_POKEMON_LEAGUE_UNIQUE_COUNT, tpEliteFourUniquePokemonSpinner,
                        tpEliteFourUniquePokemonCheckBox),
                associateCheckBox(Name.TRAINERS_EVOLVE_POKEMON, tpTrainersEvolveTheirPokemonCheckbox),
                associateSpinSlider(Name.TRAINERS_EVOLVE_LEVEL_PERCENT_MODIFIER, tpPercentageEvolutionLevelModifierSpinSlider),
                associateSpinSlider(Name.TRAINERS_LEVEL_MODIFIER_PERCENT, tpPercentageLevelModifierSpinSlider,
                        tpPercentageLevelModifierCheckBox),

                //Totem Pokemon
                associateButtonSet(Name.RANDOMIZE_TOTEM_POKEMON,
                        Map.of(
                                TotemPokemonMod.UNCHANGED, totpUnchangedRadioButton,
                                TotemPokemonMod.RANDOM, totpRandomRadioButton,
                                TotemPokemonMod.SIMILAR_STRENGTH, totpRandomSimilarStrengthRadioButton
                        )),
                associateCheckBox(Name.TOTEMS_RANDOMIZE_HELD_ITEMS, totpRandomizeHeldItemsCheckBox),
                associateCheckBox(Name.TOTEMS_ALLOW_ALT_FORMES, totpAllowAltFormesCheckBox),
                associateSpinSlider(Name.TOTEMS_LEVEL_MODIFIER_PERCENT, totpPercentageLevelModifierSpinSlider,
                        totpPercentageLevelModifierCheckBox),
                //--Allies
                associateButtonSet(Name.TOTEMS_RANDOMIZE_ALLIES,
                        Map.of(
                                AllyPokemonMod.UNCHANGED, totpAllyUnchangedRadioButton,
                                AllyPokemonMod.RANDOM, totpAllyRandomRadioButton,
                                AllyPokemonMod.SIMILAR_STRENGTH, totpAllyRandomSimilarStrengthRadioButton
                        )),
                //--Auras
                associateButtonSet(Name.TOTEMS_RANDOMIZE_AURAS,
                        Map.of(
                                AuraMod.UNCHANGED, totpAuraUnchangedRadioButton,
                                AuraMod.RANDOM, totpAuraRandomRadioButton,
                                AuraMod.SAME_STRENGTH, totpAuraRandomSameStrengthRadioButton
                        )),

                // *** WILD POKEMON ***
                //Random Encounters
                associateCheckBox(Name.RANDOMIZE_WILD_ENCOUNTERS, wpRandomizeWildPokemonCheckBox),
                //--Replacement Zone
                associateButtonSet(Name.WILD_REPLACEMENT_ZONE,
                        Map.of(
                                WildPokemonZoneMod.GAME, wpZoneGameRadioButton,
                                WildPokemonZoneMod.NAMED_LOCATION, wpZoneNamedLocationRadioButton,
                                WildPokemonZoneMod.MAP, wpZoneMapRadioButton,
                                WildPokemonZoneMod.ENCOUNTER_SET, wpZoneEncounterSetRadioButton,
                                WildPokemonZoneMod.SINGLE_ENCOUNTER, wpZoneNoneRadioButton
                        )),
                associateCheckBox(Name.WILD_SPLIT_REPLACEMENT_ZONE_BY_ENCOUNTER_TYPES, wpSplitByEncounterTypesCheckBox),
                associateCheckBox(Name.WILD_REMOVE_TIME_BASED, wpRemoveTimeBasedEncountersCheckBox),
                //--Type Restrictions
                associateButtonSet(Name.WILD_TYPE_RESTRICTION,
                        Map.of(
                                WildPokemonTypeMod.NONE, wpTRNoneRadioButton,
                                WildPokemonTypeMod.RANDOM_THEMES, wpTRThemedAreasRadioButton,
                                WildPokemonTypeMod.KEEP_PRIMARY, wpTRKeepPrimaryRadioButton
                        )),
                associateCheckBox(Name.WILD_KEEP_TYPE_THEMES, wpTRKeepThemesCheckBox),
                //--Evolution Restrictions
                associateButtonSet(Name.WILD_EVOLUTION_RESTRICTION,
                        Map.of(
                                WildPokemonEvolutionMod.NONE, wpERNoneRadioButton,
                                WildPokemonEvolutionMod.BASIC_ONLY, wpERBasicOnlyRadioButton,
                                WildPokemonEvolutionMod.KEEP_STAGE, wpERSameEvolutionStageRadioButton
                        )),
                associateCheckBox(Name.WILD_EVOLUTION_KEEP_RELATIONS, wpERKeepEvolutionsCheckBox),
                //--Randomization Options
                associateCheckBox(Name.WILD_NO_LEGENDARIES, wpDontUseLegendariesCheckBox),
                associateCheckBox(Name.WILD_CATCH_EM_ALL, wpCatchEmAllModeCheckBox),
                associateCheckBox(Name.WILD_USE_SIMILAR_STRENGTH, wpSimilarStrengthCheckBox),
                associateCheckBox(Name.WILD_SIMILAR_STRENGTH_BALANCE_LOW_LEVEL, wpBalanceShakingGrassPokemonCheckBox),
                associateCheckBox(Name.WILD_ALLOW_ALT_FORMES, wpAllowAltFormesCheckBox),
                //--Other Options
                associateCheckBox(Name.WILD_RANDOMIZE_HELD_ITEMS, wpRandomizeHeldItemsCheckBox),
                associateCheckBox(Name.WILD_HELD_ITEMS_BAN_MINOR, wpBanMinorItemsCheckBox),
                // TODO: min catch rate should be a combo box,
                associateSpinSlider(Name.WILD_LEVEL_MODIFIER_PERCENT, wpPercentageLevelModifierSpinSlider,
                        wpPercentageLevelModifierCheckBox),
                associateCheckBox(Name.WILD_ALL_SPECIES_CALL_ALLIES, wpSOSForAllCheckBox),

                //Static Pokémon
                associateButtonSet(Name.RANDOMIZE_STATIC_ENCOUNTERS,
                        Map.of(
                                StaticPokemonMod.UNCHANGED, seUnchangedRadioButton,
                                StaticPokemonMod.RANDOM_MATCHING, seSwapLegendariesSwapStandardsRadioButton,
                                StaticPokemonMod.COMPLETELY_RANDOM, seRandomCompletelyRadioButton,
                                StaticPokemonMod.SIMILAR_STRENGTH, seRandomSimilarStrengthRadioButton
                        )),
                associateCheckBox(Name.STATICS_FULL_RANDOM_OVER_600_BST, seRandomize600BSTCheckBox),
                associateCheckBox(Name.STATICS_LIMIT_MAIN_GAME_LEGENDARIES, seLimitMainGameLegendariesCheckBox),
                associateCheckBox(Name.STATICS_ALLOW_ALT_FORMES, seAllowAltFormesCheckBox),
                associateCheckBox(Name.STATICS_SWAP_MEGA_EVOLVABLES, seSwapMegaEvosCheckBox),
                associateCheckBox(Name.STATICS_FIX_MUSIC, seFixMusicCheckBox),
                associateSpinSlider(Name.STATICS_LEVEL_MODIFIER_PERCENT, sePercentageLevelModifierSpinSlider,
                        sePercentageLevelModifierCheckBox),
                associateCheckBox(Name.STATICS_BALANCE_FOSSIL_LEVELS, seBalanceGivenLevelsCheckBox),

                // *** MOVE TEACHING ***
                //TMs & HMs
                //--Moves
                associateButtonSet(Name.RANDOMIZE_TM_MOVES,
                        Map.of(
                                TMMovesMod.UNCHANGED, tmmUnchangedRadioButton,
                                TMMovesMod.RANDOM, tmmRandomRadioButton
                        )),
                associateCheckBox(Name.TMS_BAN_OVERPOWERED, tmmNoGameBreakingMovesCheckBox),
                associateCheckBox(Name.TMS_KEEP_FIELD_MOVES, tmmKeepFieldMoveTMsCheckBox),
                associateSpinSlider(Name.TMS_GOOD_DAMAGING_PERCENT, tmmForceGoodDamagingSpinSlider,
                        tmmForceGoodDamagingCheckBox),
                //--Compatibility
                associateButtonSet(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                        Map.of(
                                TMsHMsCompatibilityMod.UNCHANGED, thcUnchangedRadioButton,
                                TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE, thcRandomPreferSameTypeRadioButton,
                                TMsHMsCompatibilityMod.COMPLETELY_RANDOM, thcRandomCompletelyRadioButton,
                                TMsHMsCompatibilityMod.FULL, thcFullCompatibilityRadioButton
                        )),
                associateCheckBox(Name.TM_COMPATABILITY_LEVEL_UP_SANITY, thcLevelupMoveSanityCheckBox),
                associateCheckBox(Name.TM_COMPATABILITY_FOLLOW_EVOLUTIONS, thcFollowEvolutionsCheckBox),
                associateCheckBox(Name.TMS_FULL_HM_COMPATABILITY, thcFullHMCompatibilityCheckBox),

                //Tutors
                associateButtonSet(Name.RANDOMIZE_TUTOR_MOVES,
                        Map.of(
                                MoveTutorMovesMod.UNCHANGED, mtmUnchangedRadioButton,
                                MoveTutorMovesMod.RANDOM, mtmRandomRadioButton
                        )),
                associateCheckBox(Name.TUTORS_BAN_OVERPOWERED, mtmNoGameBreakingMovesCheckBox),
                associateCheckBox(Name.TUTORS_KEEP_FIELD_MOVES, mtmKeepFieldMoveTutorsCheckBox),
                associateSpinSlider(Name.TUTORS_GOOD_DAMAGING_PERCENT, mtmForceGoodDamagingSpinSlider,
                        mtmForceGoodDamagingCheckBox),
                //--Compatibility
                associateButtonSet(Name.RANDOMIZE_TUTOR_COMPATABILITY,
                        Map.of(
                                MoveTutorsCompatibilityMod.UNCHANGED, mtcUnchangedRadioButton,
                                MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE, mtcRandomPreferSameTypeRadioButton,
                                MoveTutorsCompatibilityMod.COMPLETELY_RANDOM, mtcRandomCompletelyRadioButton,
                                MoveTutorsCompatibilityMod.FULL, mtcFullCompatibilityRadioButton
                        )),
                associateCheckBox(Name.TUTOR_COMPATABILITY_LEVEL_UP_SANITY, mtcLevelupMoveSanityCheckBox),
                associateCheckBox(Name.TUTOR_COMPATABILITY_FOLLOW_EVOLUTIONS, mtcFollowEvolutionsCheckBox),

                // *** Items ***
                //General
                associateCheckBox(Name.ITEMS_BAN_LUCKY_EGG, giBanLuckyEggCheckBox),
                associateCheckBox(Name.ITEMS_BAN_BIG_MONEY_MANIAC_ITEMS, giBanBigMoneyManiacCheckBox),
                associateCheckBox(Name.ITEMS_RANDOMIZE_PC_POTION, giRandomizePCPotionCheckBox),
                associateCheckBox(Name.ITEMS_NO_FREE_LUCKY_EGG, giNoFreeLuckyEggCheckBox),

                //Field Items
                associateButtonSet(Name.RANDOMIZE_FIELD_ITEMS,
                        Map.of(
                                FieldItemsMod.UNCHANGED, fiUnchangedRadioButton,
                                FieldItemsMod.SHUFFLE, fiShuffleRadioButton,
                                FieldItemsMod.RANDOM, fiRandomRadioButton,
                                FieldItemsMod.RANDOM_EVEN, fiRandomEvenDistributionRadioButton
                        )),
                associateCheckBox(Name.FIELD_ITEMS_BAN_MINOR, fiBanMinorItemsCheckBox),

                //Shop Items
                associateCheckBox(Name.SHOP_ITEMS_BALANCE_PRICES, shBalanceShopItemPricesCheckBox),
                associateCheckBox(Name.SHOP_ITEMS_ADD_CHEAP_RARE_CANDY, shAddRareCandyCheckBox),
                //--Special Shops
                associateButtonSet(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS,
                        Map.of(
                                ShopItemsMod.UNCHANGED, shUnchangedRadioButton,
                                ShopItemsMod.SHUFFLE, shShuffleRadioButton,
                                ShopItemsMod.RANDOM, shRandomRadioButton
                        )),
                associateCheckBox(Name.SHOP_ITEMS_BAN_MINOR, shBanMinorItemsCheckBox),
                associateCheckBox(Name.SHOP_ITEMS_BAN_REGULAR_SHOP_ITEMS, shBanRegularShopItemsCheckBox),
                associateCheckBox(Name.SHOP_ITEMS_BAN_OVERPOWERED, shBanOverpoweredShopItemsCheckBox),
                associateCheckBox(Name.SHOP_ITEMS_GUARANTEE_EVOLUTION_ITEMS, shGuaranteeEvolutionItemsCheckBox),
                associateCheckBox(Name.SHOP_ITEMS_GUARANTEE_X_ITEMS, shGuaranteeXItemsCheckBox),

                //Pickup Items
                associateButtonSet(Name.RANDOMIZE_PICKUP_ITEMS,
                        Map.of(
                                PickupItemsMod.UNCHANGED, puUnchangedRadioButton,
                                PickupItemsMod.RANDOM, puRandomRadioButton
                        )),
                associateCheckBox(Name.PICKUP_ITEMS_BAN_MINOR, puBanMinorItemsCheckBox),

                // *** TYPES ***
                //Type Effectiveness
                associateButtonSet(Name.RANDOMIZE_TYPE_EFFECTIVENESS,
                        Map.of(
                                TypeEffectivenessMod.UNCHANGED, teUnchangedRadioButton,
                                TypeEffectivenessMod.RANDOM, teRandomRadioButton,
                                TypeEffectivenessMod.RANDOM_BALANCED, teRandomBalancedRadioButton,
                                TypeEffectivenessMod.KEEP_IDENTITIES, teKeepTypeIdentitiesRadioButton,
                                TypeEffectivenessMod.INVERSE, teInverseRadioButton
                        )),
                associateCheckBox(Name.TYPE_INVERSE_ADD_RANDOM_IMMUNITIES, teAddRandomImmunitiesCheckBox),
                associateCheckBox(Name.UPDATE_TYPE_EFFECTIVENESS, teUpdateCheckbox),

                // *** GRAPHICS ***
                //Species Palettes
                associateButtonSet(Name.RANDOMIZE_SPECIES_PALETTES,
                        Map.of(
                                SpeciesPalettesMod.UNCHANGED, spalUnchangedRadioButton,
                                SpeciesPalettesMod.RANDOM, spalRandomRadioButton
                        )),
                associateCheckBox(Name.PALETTES_FOLLOW_TYPES, spalFollowTypesCheckBox),
                associateCheckBox(Name.PALETTES_FOLLOW_EVOLUTIONS, spalFollowEvolutionsCheckBox),
                associateCheckBox(Name.PALETTES_SHINY_FROM_NORMAL, spalShinyFromNormalCheckBox)

                // TODO: again, what to do with CPGs?
        );
    }

    //region associate controls helper methods

    private BooleanSettingCoordinator<CheckBoxManager> associateCheckBox(Name settingName, JCheckBox checkBox) {
        return new BooleanSettingCoordinator<>(settingName, settingsManager, new CheckBoxManager(checkBox));
    }

    private <E extends Enum<E>, J extends AbstractButton> EnumSettingCoordinator<E> associateButtonSet(
            Name settingName, Map<E, J> map) {
        return new EnumSettingCoordinator<>(settingName, settingsManager, new ButtonGroupManager<>(map));
    }

    private <E extends Enum<E>> EnumSettingCoordinator<E> associateComboBoxUsingToString(
            Name settingName, JComboBox<String> comboBox, List<E> valuesInOrder) {
        Map<E, String> valuesToDisplay = new HashMap<>();
        valuesInOrder.forEach(e -> valuesToDisplay.put(e, e.toString()));

        return new EnumSettingCoordinator<>(settingName, settingsManager, new EnumComboBoxManager<>(
                comboBox, valuesInOrder, valuesToDisplay));
    }

    private NumericSettingCoordinator<Integer, SliderManager> associateSlider(Name settingName, JSlider slider) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SliderManager(slider));
    }

    private NumericSettingCoordinator<Integer, SliderManager> associateSlider(
            Name settingName, JSlider slider, JCheckBox latch) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SliderManager(slider), latch);
    }

    private NumericSettingCoordinator<Integer, SpinSliderManager> associateSpinSlider(
            Name settingName, SpinSlider spinSlider) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SpinSliderManager(spinSlider));
    }

    private NumericSettingCoordinator<Integer, SpinSliderManager> associateSpinSlider(
            Name settingName, SpinSlider spinSlider, JCheckBox latch) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SpinSliderManager(spinSlider), latch);
    }

    private NumericSettingCoordinator<Integer, SpinnerManager> associateSpinner(Name settingName, JSpinner spinner) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SpinnerManager(spinner));
    }

    private NumericSettingCoordinator<Integer, SpinnerManager> associateSpinner(
            Name settingName, JSpinner spinner, JCheckBox latch) {
        return new NumericSettingCoordinator<>(settingName, settingsManager, new SpinnerManager(spinner), latch);
    }

    //endregion

    private void initVisibilityListeners() {
        System.out.println(banSpeciesPanel.getComponents().length);
        for (Component component : banSpeciesPanel.getComponents()) {
            System.out.println(component);
            System.out.println(component.getName());
        }

        // all panels in the tabs should be here
        List<JPanel> panels = List.of(
                // General Options
                cosmeticPanel, limitPanel, banSpeciesPanel, qolTweaksPanel, balanceTweaksPanel,
                // Species Traits
                statsPanel, totalsPanel, distPanel, traitsTypesPanel, speciesAbilitiesPanel, evolutionPanel,
                expCurvesPanel,
                // Given Pokémon
                startersPanel, spEvolutionPanel, spTypesPanel, tradesPanel,
                // Moves & Movesets
                moveTraitsPanel, movesetsPanel,
                // Foe Pokémon
                trainersPanel, tpInnerPanel, tpBattleStylePanel, tpTypesPanel, totpPanel, totpAllyPanel, totpAuraPanel,
                // Wild Pokémon
                wpPanel, wpReplacementsPanel, wpTypesPanel, wpEvolutionPanel, staticsPanel,
                // Move Teaching
                tmsPanel, tmMovesPanel, tmCompatPanel, tutorsPanel, mtMovesPanel, mtCompatPanel,
                // Items
                generalItemsPanel, fieldItemsPanel, shopItemsPanel, pickupItemsPanel,
                // Types
                typesPanel,
                // Graphics
                palettesPanel, customPlayerPanel
        );
        for (JPanel panel : panels) {
            ComponentListener autoHidePanelListener = new AutoHidePanelListener(panel);
            for (Component component : panel.getComponents()) {
                component.addComponentListener(autoHidePanelListener);
            }
        }
    }

    private static class AutoHidePanelListener implements ComponentListener {

        private final JPanel panel;

        public AutoHidePanelListener(JPanel panel) {
            this.panel = panel;
        }

        @Override
        public void componentResized(ComponentEvent e) {}
        @Override
        public void componentMoved(ComponentEvent e) {}

        @Override
        public void componentShown(ComponentEvent e) {
            panel.setVisible(true);
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            System.out.println("Applying listener to:" + panel);
            boolean anyComponentVisible = false;
            for (Component component : panel.getComponents()) {
                // spacers are empty JPanels. ignore them
                if (component instanceof JPanel subPanel && subPanel.getComponents().length == 0) continue;
                if (component.isVisible()) {
                    System.out.println("Still visible component:" + component);
                    anyComponentVisible = true;
                    break;
                }
            }
            panel.setVisible(anyComponentVisible);
        }
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
                    Math.clamp(modifiedLevel, 1, 100)));
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
        spalNotExistLabel.setVisible(false);
        spalPartiallyImplementedLabel.setVisible(false);
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

        sePercentageLevelModifierSpinSlider.setModel(new SpinnerNumberModel(
                0,
                -100,
                155,
                1
        ));

        slmForceGoodDamagingSpinSlider.setModel(new SpinnerNumberModel(
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

        tpAddToBossTrainersSpinner.setModel(bossTrainerModel);
        tpAddToImportantTrainersSpinner.setModel(importantTrainerModel);
        tpAddToRegularTrainersSpinner.setModel(regularTrainerModel);
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
        } else if (outputType == SaveType.FILE) {
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

                        // We got an annoying case of romHandler being reset (the ROM being re-opened)
                        // inside saveRandomizedRom()... but in a separate thread. Ideally all of this
                        // code should be rewritten to be properly thread-safe, but *hopefully* a full
                        // GUI rewrite is coming up soonish either way.
                        // With that in mind, a smelly busy-wait loop will have to do as a short-term
                        // bug resolver. --voliol 2026-08-01
                        RomHandler before = romHandler;
                        saveRandomizedRom(outputType, rom);
                        while (romHandler == before) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {}
                        }
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
                    settings.toStringOld(),
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
                        true, settings.toStringOld(), Long.toString(seed));
            }
            SwingUtilities.invokeLater(() -> finishRandomization(
                    filename, seed, cpg, baos, results.getCheckValue(), raceMode, batchRandomization
            ));
        } else {
            Exception e = results.getException();
            if (e instanceof RandomizationException) {
                attemptToLogException(e, "GUI.saveROM.saveFailedDialog.message", "GUI.saveROM.saveFailedNoLogDialog.message", true,
                        settings.toStringOld(), Long.toString(seed));
            } else if (e instanceof CannotWriteToLocationException) {
                JOptionPane.showMessageDialog(mainPanel,
                        String.format(bundle.getString("GUI.saveROM.cannotWriteToLocationDialog.message"), filename));
            } else {
                attemptToLogException(e, "GUI.saveROM.saveFailedIODialog.message", "GUI.saveROM.saveFailedIONoLogDialog.message",
                        settings.toStringOld(), Long.toString(seed));
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
        peShareTypingCheckBox.setSelected(settings.isEvosSameTyping());
        peMaxThreeStagesCheckBox.setSelected(settings.isEvosMaxThreeStages());
        peForceChangeCheckBox.setSelected(settings.isEvosForceChange());
        peAllowAltFormesCheckBox.setSelected(settings.isEvosAllowAltFormes());
        peForceGrowthCheckBox.setSelected(settings.isEvosForceGrowth());
        peNoConvergenceCheckBox.setSelected(settings.isEvosNoConvergence());
        peAdjustLevelsCheckBox.setSelected(settings.isAdjustEvolutionLevels());

        mtRandomizeMoveAccuracyCheckBox.setSelected(settings.isRandomizeMoveAccuracies());
        mtRandomizeMoveCategoryCheckBox.setSelected(settings.isRandomizeMoveCategory());
        mtRandomizeMovePowerCheckBox.setSelected(settings.isRandomizeMovePowers());
        mtRandomizeMovePPCheckBox.setSelected(settings.isRandomizeMovePPs());
        mtRandomizeMoveTypesCheckBox.setSelected(settings.isRandomizeMoveTypes());
        mtRandomizeMoveNamesCheckBox.setSelected(settings.isRandomizeMoveNames());

        slmRandomCompletelyRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.COMPLETELY_RANDOM);
        slmRandomPreferringSameTypeRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.RANDOM_PREFER_SAME_TYPE);
        slmUnchangedRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.UNCHANGED);
        slmMetronomeOnlyModeRadioButton.setSelected(settings.getMovesetsMod() == SettingsManager.MovesetsMod.METRONOME_ONLY);
        slmGuaranteedLevel1MovesCheckBox.setSelected(settings.isStartWithGuaranteedMoves());
        slmGuaranteedLevel1MovesSlider.setValue(settings.getGuaranteedMoveCount());
        slmReorderDamagingMovesCheckBox.setSelected(settings.isReorderDamagingMoves());
        slmForceGoodDamagingCheckBox.setSelected(settings.isMovesetsForceGoodDamaging());
        slmForceGoodDamagingSpinSlider.setValue(settings.getMovesetsGoodDamagingPercent());
        slmNoGameBreakingMovesCheckBox.setSelected(settings.isBlockBrokenMovesetMoves());
        slmEvolutionMovesCheckBox.setSelected(settings.isEvolutionMovesForAll());

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
        tpAddToBossTrainersCheckBox.setSelected(settings.getAdditionalBossTrainerPokemon() > 0);
        tpAddToBossTrainersSpinner.setValue(settings.getAdditionalBossTrainerPokemon() > 0 ? settings.getAdditionalBossTrainerPokemon() : 1);
        tpAddToImportantTrainersCheckBox.setSelected(settings.getAdditionalImportantTrainerPokemon() > 0);
        tpAddToImportantTrainersSpinner.setValue(settings.getAdditionalImportantTrainerPokemon() > 0 ? settings.getAdditionalImportantTrainerPokemon() : 1);
        tpAddToRegularTrainersCheckBox.setSelected(settings.getAdditionalRegularTrainerPokemon() > 0);
        tpAddToRegularTrainersSpinner.setValue(settings.getAdditionalRegularTrainerPokemon() > 0 ? settings.getAdditionalRegularTrainerPokemon() : 1);
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
        wpBanMinorItemsCheckBox.setSelected(settings.isBanBadRandomWildPokemonHeldItems());
        wpBalanceShakingGrassPokemonCheckBox.setSelected(settings.isBalanceShakingGrass());
        wpPercentageLevelModifierCheckBox.setSelected(settings.isWildLevelsModified());
        wpPercentageLevelModifierSpinSlider.setValue(settings.getWildLevelModifier());
        wpAllowAltFormesCheckBox.setSelected(settings.isAllowWildAltFormes());

        seUnchangedRadioButton.setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.UNCHANGED);
        seSwapLegendariesSwapStandardsRadioButton.setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.RANDOM_MATCHING);
        seRandomCompletelyRadioButton
                .setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.COMPLETELY_RANDOM);
        seRandomSimilarStrengthRadioButton
                .setSelected(settings.getStaticPokemonMod() == SettingsManager.StaticPokemonMod.SIMILAR_STRENGTH);
        seLimitMainGameLegendariesCheckBox.setSelected(settings.isLimitMainGameLegendaries());
        seRandomize600BSTCheckBox.setSelected(settings.isLimit600());
        seAllowAltFormesCheckBox.setSelected(settings.isAllowStaticAltFormes());
        seSwapMegaEvosCheckBox.setSelected(settings.isSwapStaticMegaEvos());
        sePercentageLevelModifierCheckBox.setSelected(settings.isStaticLevelModified());
        sePercentageLevelModifierSpinSlider.setValue(settings.getStaticLevelModifier());
        seFixMusicCheckBox.setSelected(settings.isCorrectStaticMusic());

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
        fiBanMinorItemsCheckBox.setSelected(settings.isBanBadRandomFieldItems());

        shRandomRadioButton.setSelected(settings.getShopItemsMod() == SettingsManager.ShopItemsMod.RANDOM);
        shShuffleRadioButton.setSelected(settings.getShopItemsMod() == SettingsManager.ShopItemsMod.SHUFFLE);
        shUnchangedRadioButton.setSelected(settings.getShopItemsMod() == SettingsManager.ShopItemsMod.UNCHANGED);
        shBanMinorItemsCheckBox.setSelected(settings.isBanBadRandomShopItems());
        shBanRegularShopItemsCheckBox.setSelected(settings.isBanRegularShopItems());
        shBanOverpoweredShopItemsCheckBox.setSelected(settings.isBanOPShopItems());
        shGuaranteeEvolutionItemsCheckBox.setSelected(settings.isGuaranteeEvolutionItems());
        shGuaranteeXItemsCheckBox.setSelected(settings.isGuaranteeXItems());
        shBalanceShopItemPricesCheckBox.setSelected(settings.isBalanceShopPrices());
        shAddRareCandyCheckBox.setSelected(settings.isAddCheapRareCandiesToShops());

        puUnchangedRadioButton.setSelected(settings.getPickupItemsMod() == SettingsManager.PickupItemsMod.UNCHANGED);
        puRandomRadioButton.setSelected(settings.getPickupItemsMod() == SettingsManager.PickupItemsMod.RANDOM);
        puBanMinorItemsCheckBox.setSelected(settings.isBanBadRandomPickupItems());

        teUnchangedRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.UNCHANGED);
        teRandomRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.RANDOM);
        teRandomBalancedRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.RANDOM_BALANCED);
        teKeepTypeIdentitiesRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.KEEP_IDENTITIES);
        teInverseRadioButton.setSelected(settings.getTypeEffectivenessMod() == SettingsManager.TypeEffectivenessMod.INVERSE);
        teAddRandomImmunitiesCheckBox.setSelected(settings.isInverseTypesRandomImmunities());
        teUpdateCheckbox.setSelected(settings.isUpdateTypeEffectiveness());

        spalUnchangedRadioButton.setSelected(settings.getPokemonPalettesMod() == SettingsManager.PokemonPalettesMod.UNCHANGED);
        spalRandomRadioButton.setSelected(settings.getPokemonPalettesMod() == SettingsManager.PokemonPalettesMod.RANDOM);
        spalFollowTypesCheckBox.setSelected(settings.isPokemonPalettesFollowTypes());
        spalFollowEvolutionsCheckBox.setSelected(settings.isPokemonPalettesFollowEvolutions());
        spalShinyFromNormalCheckBox.setSelected(settings.isPokemonPalettesShinyFromNormal());

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

        setInitialButtonState(openROMButton, randomizeSaveButton, premadeSeedButton, settingsButton,
                loadSettingsButton, saveSettingsButton);
        enableButtons(openROMButton, randomizeSaveButton, premadeSeedButton, settingsButton);

        // the buttons in the main part of the gui (randomization options):
        // TODO: set all settings to default (should set the connected GUI elements accordingly)

        // TODO: relabel the relevant spin boxes (i.e. starter selection)

        // TODO: look at these labels closer
        tpCalculatedFullyEvolvedLvlLabel.setVisible(true);
        tpCalculatedFullyEvolvedLvlLabel.setEnabled(false);
        tpCalculatedFullyEvolvedLvlLabel.setText(String.format(bundle.getString("GUI.foeTab.trainersPanel.calculatedFullyEvolvedLvlLabel.text"), "--"));
		tpAdditionalPokemonForLabel.setVisible(true);
		tpHeldItemsLabel.setVisible(true);
        mtNoExistLabel.setVisible(false);
        qoltNoneAvailableLabel.setVisible(false);
        btNoneAvailableLabel.setVisible(false);
        spalNotExistLabel.setVisible(false);
        spalPartiallyImplementedLabel.setVisible(false);
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

            raceModeCheckBox.setEnabled(true);

            loadSettingsButton.setEnabled(true);
            saveSettingsButton.setEnabled(true);

            settingsManager.unassociateGame();
            settingsManager.associateGame(romHandler);
            // TODO: make sure settings are reset if not supported at this point
            // TODO: clean up

            boolean canAddPokesToBoss = romHandler.canAddPokemonToBossTrainers();
            boolean canAddPokesToImportant = romHandler.canAddPokemonToImportantTrainers();
            boolean canAddPokesToRegular = romHandler.canAddPokemonToRegularTrainers();
            boolean additionalPokemonAvailable = canAddPokesToBoss || canAddPokesToImportant || canAddPokesToRegular;

            tpAdditionalPokemonForLabel.setVisible(additionalPokemonAvailable);

            boolean canAddHeldItemsToBoss = romHandler.canAddHeldItemsToBossTrainers();
            boolean canAddHeldItemsToImportant = romHandler.canAddHeldItemsToImportantTrainers();
            boolean canAddHeldItemsToRegular = romHandler.canAddHeldItemsToRegularTrainers();
            boolean heldItemsAvailable = canAddHeldItemsToBoss || canAddHeldItemsToImportant || canAddHeldItemsToRegular;

            tpHeldItemsLabel.setVisible(heldItemsAvailable);


            boolean canGiveMovesetsToBoss = romHandler.canGiveCustomMovesetsToBossTrainers();
            boolean canGiveMovesetsToImportant = romHandler.canGiveCustomMovesetsToImportantTrainers();
            boolean canGiveMovesetsToRegular = romHandler.canGiveCustomMovesetsToRegularTrainers();
            boolean betterMovesetsAvailable = canGiveMovesetsToBoss || canGiveMovesetsToImportant || canGiveMovesetsToRegular;

            tpBetterMovesetsLabel.setVisible(betterMovesetsAvailable);


            // Types
            boolean typeSupport = romHandler.hasTypeEffectivenessSupport();
            //typesPanel.setVisible(typeSupport);
            //We shouldn't use setVisible on the panels directly in the tabbedPane; it causes strange bleedover
            //Disable it instead
            randomizationSettingsTabbedPane.setEnabledAt(7, typeSupport);

            // Graphics
            boolean ppalSupport = romHandler.hasPokemonPaletteSupport();
            spalNotExistLabel.setVisible(!ppalSupport);
            boolean ppalPartialSupport = romHandler.pokemonPaletteSupportIsPartial();
            spalPartiallyImplementedLabel.setVisible(ppalPartialSupport);

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

    @Deprecated
    private void enableOrDisableSubControls() {
        //TODO: Check that current SettingRestrictions match these, then remove this entire block
        /*
        if(romHandler == null) {
            //shouldn't be in this method right now
            return;
        }

        if (limitPokemonCheckBox.isSelected()) {
            limitPokemonButton.setEnabled(true);
        } else {
            limitPokemonButton.setEnabled(false);
        }

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

            spRandomTwoEvosRadioButton.setEnabled(true);
            spRandomBasicRadioButton.setEnabled(true);


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

        if (sbsUpdateBaseStatsCheckBox.isSelected()) {
            pbsUpdateComboBox.setEnabled(true);
        } else {
            pbsUpdateComboBox.setEnabled(false);
        }

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
            enableButtons(peSimilarStrengthCheckBox, peShareTypingCheckBox, peMaxThreeStagesCheckBox,
                    peForceChangeCheckBox, peAllowAltFormesCheckBox, peForceGrowthCheckBox, peNoConvergenceCheckBox);
        } else if (peRandomEveryLevelRadioButton.isSelected()) {
            enableButtons(peShareTypingCheckBox, peForceChangeCheckBox,
                    peAllowAltFormesCheckBox, peNoConvergenceCheckBox);
            disableAndDeselectButtons(peSimilarStrengthCheckBox,
                    peMaxThreeStagesCheckBox, peForceGrowthCheckBox);
        } else {
            disableAndDeselectButtons(peSimilarStrengthCheckBox, peShareTypingCheckBox, peMaxThreeStagesCheckBox,
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

        if (seUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(seRandomize600BSTCheckBox, seAllowAltFormesCheckBox,
                    seSwapMegaEvosCheckBox, seFixMusicCheckBox);
        } else {
            enableButtons(seRandomize600BSTCheckBox, seAllowAltFormesCheckBox,
                    seSwapMegaEvosCheckBox, seFixMusicCheckBox);
        }

        if (seRandomSimilarStrengthRadioButton.isSelected()) {
            seLimitMainGameLegendariesCheckBox.setEnabled(seLimitMainGameLegendariesCheckBox.isVisible());
        } else {
            disableAndDeselectButtons(seLimitMainGameLegendariesCheckBox);
        }

        if (sePercentageLevelModifierCheckBox.isSelected()) {
            sePercentageLevelModifierSpinSlider.setEnabled(true);
        } else {
            sePercentageLevelModifierSpinSlider.setEnabled(false);
            sePercentageLevelModifierSpinSlider.setValue(0);
        }

        /*
        if (igtUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(igtRandomizeItemsCheckBox, igtRandomizeIVsCheckBox,
                    igtRandomizeNicknamesCheckBox, igtRandomizeOTsCheckBox);
        } else {
            enableButtons(igtRandomizeItemsCheckBox, igtRandomizeIVsCheckBox,
                    igtRandomizeNicknamesCheckBox, igtRandomizeOTsCheckBox);
        }

        if (mdUpdateMovesCheckBox.isSelected()) {
            mdUpdateComboBox.setEnabled(true);
        } else {
            mdUpdateComboBox.setEnabled(false);
        }

        if (slmMetronomeOnlyModeRadioButton.isSelected() || slmUnchangedRadioButton.isSelected()) {
            disableAndDeselectButtons(slmGuaranteedLevel1MovesCheckBox, slmForceGoodDamagingCheckBox,
                    slmReorderDamagingMovesCheckBox, slmNoGameBreakingMovesCheckBox, slmEvolutionMovesCheckBox);
        } else {
            enableButtons(slmGuaranteedLevel1MovesCheckBox, slmForceGoodDamagingCheckBox,
                    slmReorderDamagingMovesCheckBox, slmNoGameBreakingMovesCheckBox, slmEvolutionMovesCheckBox);
        }

        if (slmGuaranteedLevel1MovesCheckBox.isSelected()) {
            slmGuaranteedLevel1MovesSlider.setEnabled(true);
        } else {
            slmGuaranteedLevel1MovesSlider.setEnabled(false);
            slmGuaranteedLevel1MovesSlider.setValue(slmGuaranteedLevel1MovesSlider.getMinimum());
        }

        if (slmForceGoodDamagingCheckBox.isSelected()) {
            slmForceGoodDamagingSpinSlider.setEnabled(true);
        } else {
            slmForceGoodDamagingSpinSlider.setEnabled(false);
            slmForceGoodDamagingSpinSlider.setValue(slmForceGoodDamagingSpinSlider.getMinimum());
        }

        boolean pokemonAdded = tpAddToBossTrainersCheckBox.isSelected() || tpAddToImportantTrainersCheckBox.isSelected() ||
                tpAddToRegularTrainersCheckBox.isSelected();
        if (isTrainerSetting(TRAINER_UNCHANGED) && pokemonAdded) {
            disableAndDeselectButtons(tpSwapMegaEvosCheckBox,
                    tpBossTrainersItemsCheckBox, tpImportantTrainersItemsCheckBox, tpRegularTrainersItemsCheckBox,
                    tpConsumableItemsOnlyCheckBox, tpSensibleItemsCheckBox, tpHighestLevelGetsItemCheckBox,
                    tpEliteFourUniquePokemonCheckBox);
            enableButtons(tpSimilarStrengthCheckBox, tpAvoidDuplicatesCheckBox, tpDontUseLegendariesCheckBox,
                    tpUseLocalPokemonCheckBox, tpNoEarlyWonderGuardCheckBox, tpAllowAlternateFormesCheckBox,
                    tpRandomShinyTrainerPokemonCheckBox);
            if (tpAddToBossTrainersCheckBox.isSelected()) {
                tpBossTrainersTypeDiversityCheckBox.setEnabled(true);
            } else {
                disableAndDeselectButtons(tpBossTrainersTypeDiversityCheckBox);
            }
            if (tpAddToImportantTrainersCheckBox.isSelected()) {
                tpImportantTrainersTypeDiversityCheckBox.setEnabled(true);
            } else {
                disableAndDeselectButtons(tpImportantTrainersTypeDiversityCheckBox);
            }
            if (tpAddToRegularTrainersCheckBox.isSelected()) {
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
            enableButtonsIfVisible(tpAddToBossTrainersCheckBox, tpAddToImportantTrainersCheckBox,
                    tpAddToRegularTrainersCheckBox, tpBossTrainersItemsCheckBox, tpImportantTrainersItemsCheckBox,
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

        if (tpAddToBossTrainersCheckBox.isSelected()) {
            tpAddToBossTrainersSpinner.setEnabled(true);
        } else {
            tpAddToBossTrainersSpinner.setEnabled(false);
            tpAddToBossTrainersSpinner.setValue(1);
        }

        if (tpAddToImportantTrainersCheckBox.isSelected()) {
            tpAddToImportantTrainersSpinner.setEnabled(true);
        } else {
            tpAddToImportantTrainersSpinner.setEnabled(false);
            tpAddToImportantTrainersSpinner.setValue(1);
        }

        if (tpAddToRegularTrainersCheckBox.isSelected()) {
            tpAddToRegularTrainersSpinner.setEnabled(true);
        } else {
            tpAddToRegularTrainersSpinner.setEnabled(false);
            tpAddToRegularTrainersSpinner.setValue(1);
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
            enableButtons(wpBanMinorItemsCheckBox);
        } else {
            disableAndDeselectButtons(wpBanMinorItemsCheckBox);
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

        if (slmMetronomeOnlyModeRadioButton.isSelected()) {
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

            if (!(slmUnchangedRadioButton.isSelected()) || !(tmmUnchangedRadioButton.isSelected())
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
                    && (!(slmUnchangedRadioButton.isSelected()) || !(mtmUnchangedRadioButton.isSelected())
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
            enableButtons(fiBanMinorItemsCheckBox);
        } else if (fiRandomEvenDistributionRadioButton.isSelected() && fiRandomEvenDistributionRadioButton.isVisible()
                && fiRandomEvenDistributionRadioButton.isEnabled()) {
            enableButtons(fiBanMinorItemsCheckBox);
        } else {
            disableAndDeselectButtons(fiBanMinorItemsCheckBox);
        }

        if (shRandomRadioButton.isSelected() && shRandomRadioButton.isVisible() && shRandomRadioButton.isEnabled()) {
            enableButtons(shBanMinorItemsCheckBox, shBanRegularShopItemsCheckBox,
                    shBanOverpoweredShopItemsCheckBox, shGuaranteeEvolutionItemsCheckBox,
                    shGuaranteeXItemsCheckBox);
        } else {
            disableAndDeselectButtons(shBanMinorItemsCheckBox, shBanRegularShopItemsCheckBox,
                    shBanOverpoweredShopItemsCheckBox, shGuaranteeEvolutionItemsCheckBox,
                    shGuaranteeXItemsCheckBox);
        }

        if (puRandomRadioButton.isSelected() && puRandomRadioButton.isVisible() && puRandomRadioButton.isEnabled()) {
            enableButtons(puBanMinorItemsCheckBox);
        } else {
            disableAndDeselectButtons(puBanMinorItemsCheckBox);
        }

        if (teInverseRadioButton.isSelected()) {
            enableButtons(teAddRandomImmunitiesCheckBox);
        } else {
            disableAndDeselectButtons(teAddRandomImmunitiesCheckBox);
        }

        if (spalRandomRadioButton.isSelected() && spalRandomRadioButton.isVisible()
                && spalRandomRadioButton.isEnabled()) {
            enableButtons(spalFollowTypesCheckBox, spalFollowEvolutionsCheckBox,
                    spalShinyFromNormalCheckBox);
        } else {
            disableAndDeselectButtons(spalFollowTypesCheckBox, spalFollowEvolutionsCheckBox,
                    spalShinyFromNormalCheckBox);
        }

        cpgSelection.setEnabled(cpgCustomRadioButton.isSelected() && cpgCustomRadioButton.isVisible()
                && cpgCustomRadioButton.isEnabled());
        */
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
            if (firstCliArg.equals("please-use-the-launcher")) usedLauncher = true;
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
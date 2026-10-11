package com.deivarayan.kids.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deivarayan.kids.data.GameLanguage
import com.deivarayan.kids.data.GameSpeed
import com.deivarayan.kids.data.ListeningLevel
import com.deivarayan.kids.data.MemoryLevel
import com.deivarayan.kids.data.NumberSequenceLevel
import com.deivarayan.kids.data.PatternLevel
import com.deivarayan.kids.data.QuickTapLevel
import com.deivarayan.kids.data.RhymeLevel
import com.deivarayan.kids.data.WhereWasItLevel
import com.deivarayan.kids.ui.colormatch.ColorMatchGameScreen
import com.deivarayan.kids.ui.games.GAME_ID_COLOR_MATCH
import com.deivarayan.kids.ui.games.GAME_ID_LISTENING
import com.deivarayan.kids.ui.games.GAME_ID_MEMORY
import com.deivarayan.kids.ui.games.GAME_ID_NUMBER_SEQUENCE
import com.deivarayan.kids.ui.games.GAME_ID_PATTERN
import com.deivarayan.kids.ui.games.GAME_ID_PHONICS
import com.deivarayan.kids.ui.games.GAME_ID_QUICK_TAP
import com.deivarayan.kids.ui.games.GAME_ID_RHYME
import com.deivarayan.kids.ui.games.GAME_ID_SIMON_SAYS
import com.deivarayan.kids.ui.games.GAME_ID_WHERE_WAS_IT
import com.deivarayan.kids.ui.games.GamesListScreen
import com.deivarayan.kids.ui.listening.ListeningGameScreen
import com.deivarayan.kids.ui.listening.ListeningLevelSelectScreen
import com.deivarayan.kids.ui.memory.MemoryGameScreen
import com.deivarayan.kids.ui.memory.MemoryLevelSelectScreen
import com.deivarayan.kids.ui.numbers.NumberSequenceGameScreen
import com.deivarayan.kids.ui.numbers.NumberSequenceLevelSelectScreen
import com.deivarayan.kids.ui.pattern.PatternGameScreen
import com.deivarayan.kids.ui.pattern.PatternLevelSelectScreen
import com.deivarayan.kids.ui.phonics.PhonicsGameScreen
import com.deivarayan.kids.ui.quicktap.QuickTapGameScreen
import com.deivarayan.kids.ui.quicktap.QuickTapLevelSelectScreen
import com.deivarayan.kids.ui.rhyme.RhymeGameScreen
import com.deivarayan.kids.ui.rhyme.RhymeLevelSelectScreen
import com.deivarayan.kids.ui.simon.SimonSaysScreen
import com.deivarayan.kids.ui.wherewasit.WhereWasItGameScreen
import com.deivarayan.kids.ui.wherewasit.WhereWasItLevelSelectScreen
import com.deivarayan.kids.ui.spelling.CategorySelectScreen
import com.deivarayan.kids.ui.spelling.LanguageSelectScreen
import com.deivarayan.kids.ui.spelling.SpeedSelectScreen
import com.deivarayan.kids.ui.spelling.SpellingGameScreen
import java.net.URLDecoder
import java.net.URLEncoder

private const val ROUTE_GAMES_LIST = "games_list"
private const val ROUTE_LANGUAGE_SELECT = "language_select/{gameId}"
private const val ROUTE_SPEED_SELECT = "speed_select/{gameId}/{language}"
private const val ROUTE_CATEGORY_SELECT = "category_select/{language}/{speed}"
private const val ROUTE_SPELLING_GAME = "spelling_game/{language}/{speed}/{category}"
private const val ROUTE_PHONICS_GAME = "phonics_game/{language}/{speed}"
private const val ROUTE_LISTENING_LEVEL_SELECT = "listening_level_select/{language}"
private const val ROUTE_LISTENING_GAME = "listening_game/{language}/{level}"
private const val ROUTE_MEMORY_LEVEL_SELECT = "memory_level_select/{language}"
private const val ROUTE_MEMORY_GAME = "memory_game/{language}/{level}"
private const val ROUTE_SIMON_GAME = "simon_game/{language}"
private const val ROUTE_PATTERN_LEVEL_SELECT = "pattern_level_select/{language}"
private const val ROUTE_PATTERN_GAME = "pattern_game/{language}/{level}"
private const val ROUTE_RHYME_LEVEL_SELECT = "rhyme_level_select/{language}"
private const val ROUTE_RHYME_GAME = "rhyme_game/{language}/{level}"
private const val ROUTE_QUICK_TAP_LEVEL_SELECT = "quick_tap_level_select/{language}"
private const val ROUTE_QUICK_TAP_GAME = "quick_tap_game/{language}/{level}"
private const val ROUTE_COLOR_MATCH_GAME = "color_match_game/{language}"
private const val ROUTE_WHERE_WAS_IT_LEVEL_SELECT = "where_was_it_level_select/{language}"
private const val ROUTE_WHERE_WAS_IT_GAME = "where_was_it_game/{language}/{level}"
private const val ROUTE_NUMBER_SEQUENCE_LEVEL_SELECT = "number_sequence_level_select/{language}"
private const val ROUTE_NUMBER_SEQUENCE_GAME = "number_sequence_game/{language}/{level}"
private const val ARG_GAME_ID = "gameId"
private const val ARG_LANGUAGE = "language"
private const val ARG_SPEED = "speed"
private const val ARG_CATEGORY = "category"
private const val ARG_LEVEL = "level"
private const val ALL_CATEGORIES = "all"

@Composable
fun KidsNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ROUTE_GAMES_LIST) {
        composable(ROUTE_GAMES_LIST) {
            GamesListScreen(
                onGameSelected = { gameId ->
                    navController.navigate("language_select/$gameId")
                },
            )
        }
        composable(
            route = ROUTE_LANGUAGE_SELECT,
            arguments = listOf(navArgument(ARG_GAME_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString(ARG_GAME_ID) ?: GAME_ID_PHONICS
            LanguageSelectScreen(
                onLanguageSelected = { language ->
                    when (gameId) {
                        GAME_ID_LISTENING -> navController.navigate("listening_level_select/${language.name}")
                        GAME_ID_MEMORY -> navController.navigate("memory_level_select/${language.name}")
                        GAME_ID_SIMON_SAYS -> navController.navigate("simon_game/${language.name}")
                        GAME_ID_PATTERN -> navController.navigate("pattern_level_select/${language.name}")
                        GAME_ID_RHYME -> navController.navigate("rhyme_level_select/${language.name}")
                        GAME_ID_QUICK_TAP -> navController.navigate("quick_tap_level_select/${language.name}")
                        GAME_ID_COLOR_MATCH -> navController.navigate("color_match_game/${language.name}")
                        GAME_ID_WHERE_WAS_IT -> navController.navigate("where_was_it_level_select/${language.name}")
                        GAME_ID_NUMBER_SEQUENCE -> navController.navigate("number_sequence_level_select/${language.name}")
                        else -> navController.navigate("speed_select/$gameId/${language.name}")
                    }
                },
            )
        }
        composable(
            route = ROUTE_SPEED_SELECT,
            arguments = listOf(
                navArgument(ARG_GAME_ID) { type = NavType.StringType },
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString(ARG_GAME_ID) ?: GAME_ID_PHONICS
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            SpeedSelectScreen(
                onSpeedSelected = { speed ->
                    if (gameId == GAME_ID_PHONICS) {
                        navController.navigate("phonics_game/$languageName/${speed.name}")
                    } else {
                        navController.navigate("category_select/$languageName/${speed.name}")
                    }
                },
            )
        }
        composable(
            route = ROUTE_CATEGORY_SELECT,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_SPEED) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val speedName = backStackEntry.arguments?.getString(ARG_SPEED)
                ?: GameSpeed.EXPERT.name
            CategorySelectScreen(
                onCategorySelected = { category ->
                    val encoded = URLEncoder.encode(category ?: ALL_CATEGORIES, "UTF-8")
                    navController.navigate("spelling_game/$languageName/$speedName/$encoded")
                },
            )
        }
        composable(
            route = ROUTE_SPELLING_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_SPEED) { type = NavType.StringType },
                navArgument(ARG_CATEGORY) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val speedName = backStackEntry.arguments?.getString(ARG_SPEED)
                ?: GameSpeed.EXPERT.name
            val categoryArg = backStackEntry.arguments?.getString(ARG_CATEGORY) ?: ALL_CATEGORIES
            val category = URLDecoder.decode(categoryArg, "UTF-8").let { if (it == ALL_CATEGORIES) null else it }
            SpellingGameScreen(
                language = GameLanguage.valueOf(languageName),
                speed = GameSpeed.valueOf(speedName),
                category = category,
            )
        }
        composable(
            route = ROUTE_PHONICS_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_SPEED) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val speedName = backStackEntry.arguments?.getString(ARG_SPEED)
                ?: GameSpeed.EXPERT.name
            PhonicsGameScreen(
                language = GameLanguage.valueOf(languageName),
                speed = GameSpeed.valueOf(speedName),
            )
        }
        composable(
            route = ROUTE_LISTENING_LEVEL_SELECT,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            ListeningLevelSelectScreen(
                onLevelSelected = { level ->
                    navController.navigate("listening_game/$languageName/${level.name}")
                },
            )
        }
        composable(
            route = ROUTE_LISTENING_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_LEVEL) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val levelName = backStackEntry.arguments?.getString(ARG_LEVEL)
                ?: ListeningLevel.BEGINNER.name
            ListeningGameScreen(
                language = GameLanguage.valueOf(languageName),
                level = ListeningLevel.valueOf(levelName),
                onNextLevel = { nextLevel ->
                    navController.navigate("listening_game/$languageName/${nextLevel.name}")
                },
                onBackToLevels = { navController.popBackStack() },
            )
        }
        composable(
            route = ROUTE_MEMORY_LEVEL_SELECT,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            MemoryLevelSelectScreen(
                onLevelSelected = { level ->
                    navController.navigate("memory_game/$languageName/${level.name}")
                },
            )
        }
        composable(
            route = ROUTE_MEMORY_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_LEVEL) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val levelName = backStackEntry.arguments?.getString(ARG_LEVEL)
                ?: MemoryLevel.BEGINNER.name
            MemoryGameScreen(
                language = GameLanguage.valueOf(languageName),
                level = MemoryLevel.valueOf(levelName),
                onNextLevel = { nextLevel ->
                    navController.navigate("memory_game/$languageName/${nextLevel.name}")
                },
                onBackToLevels = { navController.popBackStack() },
            )
        }
        composable(
            route = ROUTE_SIMON_GAME,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            SimonSaysScreen(
                language = GameLanguage.valueOf(languageName),
                onBackToGames = { navController.popBackStack(ROUTE_GAMES_LIST, inclusive = false) },
            )
        }
        composable(
            route = ROUTE_PATTERN_LEVEL_SELECT,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            PatternLevelSelectScreen(
                onLevelSelected = { level ->
                    navController.navigate("pattern_game/$languageName/${level.name}")
                },
            )
        }
        composable(
            route = ROUTE_PATTERN_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_LEVEL) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val levelName = backStackEntry.arguments?.getString(ARG_LEVEL)
                ?: PatternLevel.BEGINNER.name
            PatternGameScreen(
                language = GameLanguage.valueOf(languageName),
                level = PatternLevel.valueOf(levelName),
                onNextLevel = { nextLevel ->
                    navController.navigate("pattern_game/$languageName/${nextLevel.name}")
                },
                onBackToLevels = { navController.popBackStack() },
            )
        }
        composable(
            route = ROUTE_RHYME_LEVEL_SELECT,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            RhymeLevelSelectScreen(
                onLevelSelected = { level ->
                    navController.navigate("rhyme_game/$languageName/${level.name}")
                },
            )
        }
        composable(
            route = ROUTE_RHYME_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_LEVEL) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val levelName = backStackEntry.arguments?.getString(ARG_LEVEL)
                ?: RhymeLevel.BEGINNER.name
            RhymeGameScreen(
                language = GameLanguage.valueOf(languageName),
                level = RhymeLevel.valueOf(levelName),
                onNextLevel = { nextLevel ->
                    navController.navigate("rhyme_game/$languageName/${nextLevel.name}")
                },
                onBackToLevels = { navController.popBackStack() },
            )
        }
        composable(
            route = ROUTE_QUICK_TAP_LEVEL_SELECT,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            QuickTapLevelSelectScreen(
                onLevelSelected = { level ->
                    navController.navigate("quick_tap_game/$languageName/${level.name}")
                },
            )
        }
        composable(
            route = ROUTE_QUICK_TAP_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_LEVEL) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val levelName = backStackEntry.arguments?.getString(ARG_LEVEL)
                ?: QuickTapLevel.BEGINNER.name
            QuickTapGameScreen(
                language = GameLanguage.valueOf(languageName),
                level = QuickTapLevel.valueOf(levelName),
                onNextLevel = { nextLevel ->
                    navController.navigate("quick_tap_game/$languageName/${nextLevel.name}")
                },
                onBackToLevels = { navController.popBackStack() },
            )
        }
        composable(
            route = ROUTE_COLOR_MATCH_GAME,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            ColorMatchGameScreen(
                language = GameLanguage.valueOf(languageName),
                onBackToGames = { navController.popBackStack(ROUTE_GAMES_LIST, inclusive = false) },
            )
        }
        composable(
            route = ROUTE_WHERE_WAS_IT_LEVEL_SELECT,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            WhereWasItLevelSelectScreen(
                onLevelSelected = { level ->
                    navController.navigate("where_was_it_game/$languageName/${level.name}")
                },
            )
        }
        composable(
            route = ROUTE_WHERE_WAS_IT_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_LEVEL) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val levelName = backStackEntry.arguments?.getString(ARG_LEVEL)
                ?: WhereWasItLevel.BEGINNER.name
            WhereWasItGameScreen(
                language = GameLanguage.valueOf(languageName),
                level = WhereWasItLevel.valueOf(levelName),
                onNextLevel = { nextLevel ->
                    navController.navigate("where_was_it_game/$languageName/${nextLevel.name}")
                },
                onBackToLevels = { navController.popBackStack() },
            )
        }
        composable(
            route = ROUTE_NUMBER_SEQUENCE_LEVEL_SELECT,
            arguments = listOf(navArgument(ARG_LANGUAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            NumberSequenceLevelSelectScreen(
                onLevelSelected = { level ->
                    navController.navigate("number_sequence_game/$languageName/${level.name}")
                },
            )
        }
        composable(
            route = ROUTE_NUMBER_SEQUENCE_GAME,
            arguments = listOf(
                navArgument(ARG_LANGUAGE) { type = NavType.StringType },
                navArgument(ARG_LEVEL) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val languageName = backStackEntry.arguments?.getString(ARG_LANGUAGE)
                ?: GameLanguage.ENGLISH.name
            val levelName = backStackEntry.arguments?.getString(ARG_LEVEL)
                ?: NumberSequenceLevel.BEGINNER.name
            NumberSequenceGameScreen(
                language = GameLanguage.valueOf(languageName),
                level = NumberSequenceLevel.valueOf(levelName),
                onNextLevel = { nextLevel ->
                    navController.navigate("number_sequence_game/$languageName/${nextLevel.name}")
                },
                onBackToLevels = { navController.popBackStack() },
            )
        }
    }
}

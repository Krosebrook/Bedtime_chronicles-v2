package com.example.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StoryDetailsScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.TutorialScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.CreateStoryScreen

import com.example.ui.screens.LibraryScreen

import com.example.ui.screens.AdventureScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = "tutorial",
        enterTransition = {
            slideInHorizontally(animationSpec = tween(400)) { it / 3 } + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(400)) { -it / 3 } + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(400)) { -it / 3 } + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(400)) { it / 3 } + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable("tutorial") {
            TutorialScreen(
                onFinishTutorial = {
                    navController.navigate("home") {
                        popUpTo("tutorial") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToStory = { storyId ->
                    navController.navigate("story_details/$storyId")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToCreate = {
                    navController.navigate("create")
                },
                onNavigateToLibrary = {
                    navController.navigate("library")
                },
                onNavigateToAdventure = {
                    navController.navigate("adventure")
                },
                onNavigateToStickerBook = {
                    navController.navigate("sticker_book")
                },
                onNavigateToHelp = {
                    navController.navigate("help")
                },
                onNavigateToCharacterCreator = {
                    navController.navigate("character_creator")
                }
            )
        }
        composable("sticker_book") {
            com.example.ui.screens.StickerBookScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("adventure") {
            AdventureScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("library") {
            LibraryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToStory = { storyId ->
                    navController.navigate("reader/$storyId") // Mock, maybe no details page for these
                }
            )
        }
        composable("character_creator") {
            com.example.ui.screens.CharacterCreatorScreen(
                onBack = { navController.popBackStack() },
                onLaunchStoryCreation = { heroName ->
                    navController.navigate("create?heroName=$heroName")
                }
            )
        }
        composable(
            route = "create?heroName={heroName}",
            arguments = listOf(
                androidx.navigation.navArgument("heroName") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialHeroName = backStackEntry.arguments?.getString("heroName")
            CreateStoryScreen(
                onBack = { navController.popBackStack() },
                onStoryGenerated = { storyId ->
                    navController.navigate("reader/$storyId")
                },
                initialHeroName = initialHeroName
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate("help") }
            )
        }
        composable("help") {
            HelpScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("story_details/{storyId}") { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
            StoryDetailsScreen(
                storyId = storyId,
                onBack = { navController.popBackStack() },
                onStartReading = { id ->
                    navController.navigate("reader/$id")
                }
            )
        }
        composable("reader/{storyId}") { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
            ReaderScreen(
                storyId = storyId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

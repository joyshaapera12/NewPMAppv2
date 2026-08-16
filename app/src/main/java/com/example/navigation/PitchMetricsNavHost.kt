package com.example.navigation

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.network.SafeApiLogger
import com.example.domain.models.LiveMatch
import com.example.domain.models.Match
import com.example.domain.models.Prediction
import com.example.feature.discover.DiscoverScreen
import com.example.feature.discover.DiscoverViewModel
import com.example.feature.home.HomeScreen
import com.example.feature.home.HomeViewModel
import com.example.feature.live.LiveScreen
import com.example.feature.live.LiveViewModel
import com.example.feature.matchcentre.MatchCentreScreen
import com.example.feature.matchcentre.MatchCentreViewModel
import com.example.feature.matchcentre.MatchRouteArgs
import com.example.feature.predictions.AiPicksScreen
import com.example.feature.predictions.PredictionsViewModel
import com.example.feature.profile.ProfileScreen
import com.example.feature.profile.ProfileViewModel
import com.example.feature.splash.SplashScreen
import com.example.shared.components.NavigationTab
import com.example.shared.components.PitchMetricsGlassNavBar

object Destinations {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LIVE = "live"
    const val AI_PICKS = "ai_picks"
    const val DISCOVER = "discover"
    const val PROFILE = "profile"
    const val MATCH_CENTRE = "match_centre/{matchId}?asId={asId}&source={source}"

    fun matchCentre(
        matchId: String,
        asId: String? = null,
        source: String? = null
    ): String {
        val cleanAsId = asId?.takeIf { it.isNotBlank() }
        val cleanSource = source?.takeIf { it.isNotBlank() }

        val queryParams = mutableListOf<String>()
        if (cleanAsId != null) queryParams.add("asId=$cleanAsId")
        if (cleanSource != null) queryParams.add("source=$cleanSource")

        val queryString = if (queryParams.isNotEmpty()) "?${queryParams.joinToString("&")}" else ""
        return "match_centre/$matchId$queryString"
    }
}

@Composable
fun PitchMetricsApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }

    // Keep tab selection in sync with route
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Destinations.HOME -> selectedTab = NavigationTab.HOME
            Destinations.LIVE -> selectedTab = NavigationTab.LIVE
            Destinations.AI_PICKS -> selectedTab = NavigationTab.AI_PICKS
            Destinations.DISCOVER -> selectedTab = NavigationTab.DISCOVER
            Destinations.PROFILE -> selectedTab = NavigationTab.PROFILE
        }
    }

    val isTopLevelRoute = currentRoute in listOf(
        Destinations.HOME,
        Destinations.LIVE,
        Destinations.AI_PICKS,
        Destinations.DISCOVER,
        Destinations.PROFILE
    )

    Scaffold(
        bottomBar = {
            if (isTopLevelRoute) {
                PitchMetricsGlassNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        val destination = when (tab) {
                            NavigationTab.HOME -> Destinations.HOME
                            NavigationTab.LIVE -> Destinations.LIVE
                            NavigationTab.AI_PICKS -> Destinations.AI_PICKS
                            NavigationTab.DISCOVER -> Destinations.DISCOVER
                            NavigationTab.PROFILE -> Destinations.PROFILE
                        }
                        navController.navigate(destination) {
                            popUpTo(Destinations.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isTopLevelRoute) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.Dp.Unspecified)
        ) {
            composable(Destinations.SPLASH) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(Destinations.SPLASH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Destinations.HOME) {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToMatch = { match: Match ->
                        val route = Destinations.matchCentre(
                            matchId = match.id,
                            asId = match.allSportsId,
                            source = if (match.pmId != null) "pitchmetrics" else "allsports"
                        )
                        SafeApiLogger.logMatchClick(
                            source = "home_today",
                            pmMatchId = match.pmId ?: match.id,
                            asFixtureId = match.allSportsId,
                            homeTeamName = match.homeTeam.name,
                            awayTeamName = match.awayTeam.name,
                            competitionName = match.leagueName,
                            navigationRoute = route
                        )
                        navController.navigate(route)
                    },
                    onNavigateToLiveMatch = { liveMatch: LiveMatch ->
                        val route = Destinations.matchCentre(
                            matchId = liveMatch.pmMatchId ?: liveMatch.eventKey,
                            asId = liveMatch.asFixtureId ?: liveMatch.eventKey,
                            source = if (liveMatch.pmMatchId != null) "merged" else "allsports"
                        )
                        SafeApiLogger.logMatchClick(
                            source = "home_live_carousel",
                            pmMatchId = liveMatch.pmMatchId,
                            asFixtureId = liveMatch.asFixtureId ?: liveMatch.eventKey,
                            homeTeamName = liveMatch.homeTeamName,
                            awayTeamName = liveMatch.awayTeamName,
                            competitionName = liveMatch.leagueName,
                            navigationRoute = route
                        )
                        navController.navigate(route)
                    },
                    onNavigateToPrediction = { prediction: Prediction ->
                        val route = Destinations.matchCentre(
                            matchId = prediction.matchId,
                            source = "pitchmetrics"
                        )
                        SafeApiLogger.logMatchClick(
                            source = "home_featured_pick",
                            pmMatchId = prediction.matchId,
                            asFixtureId = null,
                            homeTeamName = prediction.homeTeam,
                            awayTeamName = prediction.awayTeam,
                            competitionName = prediction.league,
                            navigationRoute = route
                        )
                        navController.navigate(route)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Destinations.DISCOVER)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Destinations.PROFILE)
                    }
                )
            }

            composable(Destinations.LIVE) {
                val liveViewModel: LiveViewModel = viewModel()
                LiveScreen(
                    viewModel = liveViewModel,
                    onNavigateToMatch = { liveMatch: LiveMatch ->
                        val route = Destinations.matchCentre(
                            matchId = liveMatch.pmMatchId ?: liveMatch.eventKey,
                            asId = liveMatch.asFixtureId ?: liveMatch.eventKey,
                            source = if (liveMatch.pmMatchId != null) "merged" else "allsports"
                        )
                        SafeApiLogger.logMatchClick(
                            source = "live_centre",
                            pmMatchId = liveMatch.pmMatchId,
                            asFixtureId = liveMatch.asFixtureId ?: liveMatch.eventKey,
                            homeTeamName = liveMatch.homeTeamName,
                            awayTeamName = liveMatch.awayTeamName,
                            competitionName = liveMatch.leagueName,
                            navigationRoute = route
                        )
                        navController.navigate(route)
                    }
                )
            }

            composable(Destinations.AI_PICKS) {
                val predictionsViewModel: PredictionsViewModel = viewModel()
                AiPicksScreen(
                    viewModel = predictionsViewModel,
                    onNavigateToMatch = { prediction: Prediction ->
                        val route = Destinations.matchCentre(
                            matchId = prediction.matchId,
                            source = "pitchmetrics"
                        )
                        SafeApiLogger.logMatchClick(
                            source = "ai_picks",
                            pmMatchId = prediction.matchId,
                            asFixtureId = null,
                            homeTeamName = prediction.homeTeam,
                            awayTeamName = prediction.awayTeam,
                            competitionName = prediction.league,
                            navigationRoute = route
                        )
                        navController.navigate(route)
                    }
                )
            }

            composable(Destinations.DISCOVER) {
                val discoverViewModel: DiscoverViewModel = viewModel()
                DiscoverScreen(
                    viewModel = discoverViewModel,
                    onNavigateToMatchId = { matchId: String ->
                        val route = Destinations.matchCentre(
                            matchId = matchId,
                            source = "pitchmetrics"
                        )
                        SafeApiLogger.logMatchClick(
                            source = "discover_search",
                            pmMatchId = matchId,
                            asFixtureId = null,
                            homeTeamName = "Match",
                            awayTeamName = "Match",
                            competitionName = "Search Result",
                            navigationRoute = route
                        )
                        navController.navigate(route)
                    }
                )
            }

            composable(Destinations.PROFILE) {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(viewModel = profileViewModel)
            }

            composable(
                route = Destinations.MATCH_CENTRE,
                arguments = listOf(
                    navArgument("matchId") { type = NavType.StringType },
                    navArgument("asId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("source") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId").orEmpty()
                val asId = backStackEntry.arguments?.getString("asId")
                val source = backStackEntry.arguments?.getString("source").orEmpty()

                val pmMatchId = if (source != "allsports") matchId else null
                val effectiveAsId = asId ?: if (source == "allsports") matchId else null

                val args = MatchRouteArgs(
                    matchId = matchId,
                    pmMatchId = pmMatchId,
                    allSportsFixtureId = effectiveAsId,
                    source = source
                )

                val app = LocalContext.current.applicationContext as Application
                val matchCentreViewModel = remember(matchId, asId, source) {
                    MatchCentreViewModel(app, args)
                }

                MatchCentreScreen(
                    viewModel = matchCentreViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

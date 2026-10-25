package red.line.instayar.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import red.line.instayar.data.repository.UserPreferences
import red.line.instayar.ui.editor.EditorScreen
import red.line.instayar.ui.home.HomeScreen
import red.line.instayar.ui.home.HomeViewModel
import red.line.instayar.ui.navigation.FloatingBottomNavBar
import red.line.instayar.ui.navigation.NavDestination
import red.line.instayar.ui.onboarding.OnboardingScreen
import red.line.instayar.ui.screens.BrandProfileScreen
import red.line.instayar.ui.screens.MyProjectsScreen
import red.line.instayar.ui.screens.SettingsScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import red.line.instayar.ui.screens.AboutScreen
import red.line.instayar.ui.screens.SplashScreen
import red.line.instayar.ui.screens.VipModalBottomSheet
import red.line.instayar.ui.theme.DarkSurfaceCardElevated

@Composable
fun MainApp(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPreferences = remember { UserPreferences(context) }
    val onboardingCompleted by userPreferences.onboardingCompleted.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    val brandProfile by userPreferences.brandProfile.collectAsState()

    var currentNavDestination by remember { mutableStateOf(NavDestination.HOME) }
    var editingTemplateId by remember { mutableStateOf<String?>(null) }
    var isShowingSplash by remember { mutableStateOf(true) }
    var isShowingAbout by remember { mutableStateOf(false) }

    LaunchedEffect(activity) {
        if (activity is ComponentActivity) {
            homeViewModel.billingManager.attachRegistry(activity.activityResultRegistry)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        // App Integrity Warning Dialog
        if (homeState.integrityWarning != null) {
            AlertDialog(
                onDismissRequest = { homeViewModel.dismissIntegrityWarning() },
                title = {
                    Text(
                        text = "هشدار امنیتی اصالت برنامه ⚠️",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                },
                text = {
                    Text(
                        text = homeState.integrityWarning ?: "",
                        color = Color.White
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { homeViewModel.dismissIntegrityWarning() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("متوجه شدم", color = Color.White)
                    }
                },
                containerColor = DarkSurfaceCardElevated,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (isShowingSplash) {
            SplashScreen(
                onFinished = {
                    isShowingSplash = false
                }
            )
        } else if (isShowingAbout) {
            AboutScreen(
                onNavigateBack = {
                    isShowingAbout = false
                }
            )
        } else if (!onboardingCompleted) {
            OnboardingScreen(
                onFinished = {
                    userPreferences.setOnboardingCompleted(true)
                }
            )
        } else if (editingTemplateId != null) {
            EditorScreen(
                templateId = editingTemplateId!!,
                onNavigateBack = {
                    editingTemplateId = null
                }
            )
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    FloatingBottomNavBar(
                        currentDestination = currentNavDestination,
                        onDestinationSelect = { destination ->
                            currentNavDestination = destination
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = currentNavDestination,
                        label = "tab_crossfade"
                    ) { destination ->
                        when (destination) {
                            NavDestination.HOME -> {
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    brandProfile = brandProfile,
                                    onNavigateToEditor = { templateId ->
                                        editingTemplateId = templateId
                                    },
                                    onOpenAbout = {
                                        isShowingAbout = true
                                    }
                                )
                            }
                            NavDestination.PROJECTS -> {
                                MyProjectsScreen(
                                    brandProfile = brandProfile,
                                    onEditProject = { projectId ->
                                        editingTemplateId = projectId
                                    },
                                    onNavigateHome = {
                                        currentNavDestination = NavDestination.HOME
                                    }
                                )
                            }
                            NavDestination.BRAND -> {
                                BrandProfileScreen(
                                    brandProfile = brandProfile,
                                    onSaveProfile = { updated ->
                                        userPreferences.saveBrandProfile(updated)
                                    },
                                    onNavigateHome = {
                                        currentNavDestination = NavDestination.HOME
                                    }
                                )
                            }
                            NavDestination.SETTINGS -> {
                                SettingsScreen(
                                    isVipUser = homeState.isVipUser,
                                    onOpenVipModal = {
                                        homeViewModel.openVipModal()
                                    },
                                    onNavigateHome = {
                                        currentNavDestination = NavDestination.HOME
                                    },
                                    onOpenAbout = {
                                        isShowingAbout = true
                                    }
                                )
                            }
                        }
                    }

                    // Global VIP BottomSheet if triggered
                    if (homeState.showVipModal) {
                        VipModalBottomSheet(
                            onDismiss = { homeViewModel.closeVipModal() },
                            onPurchasePlan = { sku, onResult ->
                                homeViewModel.purchasePlan(activity, sku, onResult)
                            },
                            onRestorePurchases = { onResult ->
                                homeViewModel.restorePurchases(onResult)
                            }
                        )
                    }
                }
            }
        }
    }
}

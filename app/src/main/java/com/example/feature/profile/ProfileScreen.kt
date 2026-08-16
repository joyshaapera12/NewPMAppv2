package com.example.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.User
import com.example.shared.components.PitchMetricsGlassHeader
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRegisterMode by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            PitchMetricsGlassHeader(
                title = "Account & Settings",
                subtitle = "Preferences & Tipster Leaderboard",
                showSearchButton = false,
                showNotificationButton = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("profile_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Session or Auth Card
            if (uiState.isLoggedIn) {
                item {
                    UserOverviewCard(
                        user = uiState.user,
                        onLogout = { viewModel.logout() }
                    )
                }
            } else {
                item {
                    AuthCard(
                        isRegisterMode = isRegisterMode,
                        onToggleMode = { isRegisterMode = !isRegisterMode },
                        username = usernameInput,
                        onUsernameChange = { usernameInput = it },
                        email = emailInput,
                        onEmailChange = { emailInput = it },
                        password = passwordInput,
                        onPasswordChange = { passwordInput = it },
                        isLoading = uiState.isAuthLoading,
                        error = uiState.authError,
                        onSubmit = {
                            if (isRegisterMode) {
                                viewModel.register(usernameInput, emailInput, passwordInput)
                            } else {
                                viewModel.login(usernameInput, passwordInput)
                            }
                        }
                    )
                }
            }

            // App Settings Section
            item {
                SettingsSection(
                    currentTheme = uiState.themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )
            }

            // Global Tipster Leaderboard Section
            item {
                LeaderboardSection(leaderboard = uiState.leaderboard)
            }
        }
    }
}

@Composable
fun UserOverviewCard(
    user: User?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("user_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.2f))
                            .border(1.5.dp, PitchMetricsTheme.colors.pitchGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PitchMetricsTheme.colors.pitchGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = user?.username ?: "Tactical Punter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Global Rank: #${user?.rank ?: 42}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.pitchGreen
                        )
                    }
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = PitchMetricsTheme.colors.textMuted
                    )
                }
            }

            // Stats grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PitchMetricsTheme.colors.glassBackground)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "Accuracy", value = "${user?.accuracy ?: 74.5}%", color = PitchMetricsTheme.colors.pitchGreen)
                StatColumn(label = "Streak", value = "${user?.currentStreak ?: 3} W", color = PitchMetricsTheme.colors.aiViolet)
                StatColumn(label = "Points", value = "${user?.points ?: 560}", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}

@Composable
fun AuthCard(
    isRegisterMode: Boolean,
    onToggleMode: () -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("auth_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isRegisterMode) "Join PitchMetrics AI" else "Sign In to PitchMetrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = PitchMetricsTheme.colors.liveRed
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("auth_username_input")
            )

            if (isRegisterMode) {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
            )

            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PitchMetricsTheme.colors.pitchGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = if (isRegisterMode) "Create Account" else "Sign In", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onToggleMode,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? Sign In" else "Don't have an account? Register",
                    style = MaterialTheme.typography.labelMedium,
                    color = PitchMetricsTheme.colors.pitchGreen
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = PitchMetricsTheme.colors.pitchGreen
    val inactiveBg = PitchMetricsTheme.colors.glassBackground
    val onSurface = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("settings_section_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Display & App Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Theme Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Appearance Theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = currentTheme == mode
                        val bg = if (isSelected) activeColor else inactiveBg
                        val textCol = if (isSelected) Color.Black else onSurface

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(bg)
                                .clickable { onThemeSelected(mode) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("theme_toggle_${mode.name.lowercase()}")
                        ) {
                            Text(
                                text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = textCol
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardSection(
    leaderboard: List<User>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("leaderboard_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = PitchMetricsTheme.colors.pitchGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Global Tipster Leaderboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Weekly Season",
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchMetricsTheme.colors.textMuted
                )
            }

            leaderboard.forEachIndexed { idx, user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (idx == 0) PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.08f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${user.rank}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = when (user.rank) {
                            1 -> PitchMetricsTheme.colors.pitchGreen
                            2 -> PitchMetricsTheme.colors.aiViolet
                            3 -> PitchMetricsTheme.colors.secondaryBlue
                            else -> PitchMetricsTheme.colors.textMuted
                        },
                        modifier = Modifier.width(36.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${user.correctPredictions}/${user.totalPredictions} correct • ${user.currentStreak} streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }

                    Text(
                        text = "${user.accuracy}%",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PitchMetricsTheme.colors.pitchGreen
                    )
                }
                if (idx < leaderboard.size - 1) {
                    Divider(color = PitchMetricsTheme.colors.border.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}

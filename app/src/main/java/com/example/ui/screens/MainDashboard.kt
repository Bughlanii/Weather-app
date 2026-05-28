package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalEvent
import com.example.data.local.WeatherCached
import com.example.data.repository.HubRepository
import com.example.ui.utils.CalendarUtils
import com.example.ui.viewmodel.HubViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: HubViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger Snackbar whenever viewModel state prompts
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { viewModel.selectTab("home") },
                    icon = { Icon(if (activeTab == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = activeTab == "comparison",
                    onClick = { viewModel.selectTab("comparison") },
                    icon = { Icon(if (activeTab == "comparison") Icons.Filled.CompareArrows else Icons.Outlined.CompareArrows, contentDescription = "Compare") },
                    label = { Text("Compare", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = activeTab == "events",
                    onClick = { viewModel.selectTab("events") },
                    icon = { Icon(if (activeTab == "events") Icons.Filled.Festival else Icons.Outlined.Festival, contentDescription = "Events") },
                    label = { Text("Events", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = activeTab == "settings",
                    onClick = { viewModel.selectTab("settings") },
                    icon = { Icon(if (activeTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                )
        ) {
            Crossfade(targetState = activeTab, label = "ScreenTransition") { screen ->
                when (screen) {
                    "home" -> HomeScreen(viewModel)
                    "comparison" -> ComparisonScreen(viewModel)
                    "events" -> EventsScreen(viewModel)
                    "settings" -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

// ----------------- SUB-SCREEN 1: HOME DASHBOARD -----------------

@Composable
fun HomeScreen(viewModel: HubViewModel) {
    val primaryCity by viewModel.primaryCity.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val weather by viewModel.primaryCityWeather.collectAsState(initial = null)
    val events by viewModel.primaryCityEvents.collectAsState(initial = emptyList())
    val aiInsight by viewModel.cachedAiInsight.collectAsState()
    val geminiLoading by viewModel.geminiLoading.collectAsState()

    // Date & Time states
    var liveTime by remember { mutableStateOf(LocalTime.now()) }
    val today = LocalDate.now()

    LaunchedEffect(Unit) {
        while (true) {
            liveTime = LocalTime.now()
            delay(1000)
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH)
    val ceDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)

    val hijriDate = CalendarUtils.formatHijriDate(today)
    val desiDate = CalendarUtils.getPunjabiDesiDate(today)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Triple Calendar Systems (Regional Calendar Hub)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REGIONAL CALENDAR HUB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(100.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gregorian", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(today.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hijri", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(hijriDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Punjabi", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(desiDate.description, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ticking live local time:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = liveTime.format(timeFormatter),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Selected Primary City Weather card
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT LOCATION",
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "$primaryCity, PK",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), RoundedCornerShape(100.dp)) // Live status dot
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(100.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100.dp))
                            .clickable { viewModel.refreshPrimaryCityOnly() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (weather == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                        }
                    }
                } else {
                    val weatherDesc = CalendarUtils.getWeatherDescriptionAndIcon(weather!!.weatherCode)
                    val displayedTemp = if (tempUnit == "F") {
                        (weather!!.temp * 9 / 5 + 32).toInt().toString() + "°F"
                    } else {
                        weather!!.temp.toInt().toString() + "°C"
                    }

                    val displayedFeelsLike = if (tempUnit == "F") {
                        (weather!!.feelsLike * 9 / 5 + 32).toInt().toString() + "°F"
                    } else {
                        weather!!.feelsLike.toInt().toString() + "°C"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = displayedTemp,
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.Light,
                                        letterSpacing = (-2).sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${weatherDesc.first} • Feels like $displayedFeelsLike",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Text(
                                    text = weatherDesc.second,
                                    fontSize = 52.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Glassmorphism indicators row
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("HUMIDITY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${weather!!.humidity}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(24.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("WIND", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${weather!!.windSpeed} km/h", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(24.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("UV INDEX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            if (weather!!.temp > 35) "Very High" else if (weather!!.temp > 30) "High" else "Moderate",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Suggestion Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Psychology,
                                contentDescription = "Gemini",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Predictions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Button(
                            onClick = { viewModel.requestGeminiInsight() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            enabled = !geminiLoading,
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (geminiLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Scan Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = aiInsight,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Rule-based Smart Advisor
        item {
            Column {
                Text(
                    text = "Current Health & Desi Tips",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                val suggestions = viewModel.getLocalSmartRecommendation(weather, desiDate.monthName)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (tip in suggestions) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.TipsAndUpdates,
                                    contentDescription = "Tips",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Local associated events summary
        item {
            Column {
                Text(
                    text = "Nearby Today's Events",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (events.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No big melas in $primaryCity on this precise date. Choose and explore other cities!",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (event in events) {
                            EventSmallCard(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarRow(icon: String, title: String, valStr: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )
            Text(
                text = valStr,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun WeatherIndicator(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = name, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun EventSmallCard(event: LocalEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFFFEB3B), RoundedCornerShape(100.dp)) // Bright yellow dot
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UPCOMING EVENT",
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFDE7)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${event.location} • ${event.dateString}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(100.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "➔", fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

// ----------------- SUB-SCREEN 2: 5-CITY COMPARISON SCREEN -----------------

@Composable
fun ComparisonScreen(viewModel: HubViewModel) {
    val tempUnit by viewModel.tempUnit.collectAsState()
    val weatherList by viewModel.allCachedWeather.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Punjab Live Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Comparing 5 critical locations in regional Punjab",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { viewModel.refreshAllWeatherData() }) {
                Icon(Icons.Filled.Sync, contentDescription = "Sync all cities weather", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid layout showing comparatives
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(HubRepository.CITIES) { city ->
                val matchingCached = weatherList.firstOrNull { it.cityName.equals(city.name, ignoreCase = true) }
                ComparisonCityCard(city.name, matchingCached, tempUnit) {
                    viewModel.setPrimaryCity(city.name)
                    viewModel.selectTab("home")
                }
            }
        }
    }
}

@Composable
fun ComparisonCityCard(
    cityName: String,
    weather: WeatherCached?,
    tempUnit: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val emoji = weather?.let { CalendarUtils.getWeatherDescriptionAndIcon(it.weatherCode).second } ?: "🌡️"
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cityName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (weather == null) {
                    Text(text = "Uncached", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val displayedTemp = if (tempUnit == "F") {
                        (weather.temp * 9 / 5 + 32).toInt().toString() + "°F"
                    } else {
                        weather.temp.toInt().toString() + "°C"
                    }
                    Text(
                        text = displayedTemp,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ----------------- SUB-SCREEN 3: EVENTS SCREEN -----------------

@Composable
fun EventsScreen(viewModel: HubViewModel) {
    val eventsList by viewModel.allEvents.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredEvents = remember(eventsList, searchQuery) {
        if (searchQuery.isBlank()) {
            eventsList
        } else {
            eventsList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.location.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.cityAssociation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Punjabi Mela & Sufi Urs Explorer",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Discover location-aware cultural and religious gatherings",
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by saint name, city or mela...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search icon", modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No events match your criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEvents) { event ->
                    EventDetailedRowCard(event) {
                        viewModel.setPrimaryCity(event.cityAssociation)
                        viewModel.selectTab("home")
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailedRowCard(event: LocalEvent, onAssociateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Szn", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${event.dateString} (${event.season})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                AssistChip(
                    onClick = onAssociateClick,
                    label = { Text("View ${event.cityAssociation}", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Filled.MyLocation, contentDescription = "Locate", modifier = Modifier.size(10.dp)) },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Directions, contentDescription = "Directions", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Coordinates: ${event.latitude}, ${event.longitude}",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ----------------- SUB-SCREEN 4: SETTINGS SCREEN -----------------

@Composable
fun SettingsScreen(viewModel: HubViewModel) {
    val primaryCity by viewModel.primaryCity.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val widgetInterval by viewModel.widgetInterval.collectAsState()

    var showCityDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "App Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tailor your smart calendar and local notifications layout",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Selected City Selectors
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Primary Associated Location", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text(text = "Focuses calendar/events suggestions around this city.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box {
                            Button(
                                onClick = { showCityDropdown = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = primaryCity, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSecondary)
                            }

                            DropdownMenu(
                                expanded = showCityDropdown,
                                onDismissRequest = { showCityDropdown = false }
                            ) {
                                for (city in HubRepository.CITIES) {
                                    DropdownMenuItem(
                                        text = { Text(city.name, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setPrimaryCity(city.name)
                                            showCityDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Temperature Unit preference
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Temperature System Unit", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(text = "Choose Celsius (°C) or Fahrenheit (°F).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row {
                        FilterChip(
                            selected = tempUnit == "C",
                            onClick = { viewModel.setTempUnit("C") },
                            label = { Text("°C", fontSize = 11.sp) },
                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        )
                        FilterChip(
                            selected = tempUnit == "F",
                            onClick = { viewModel.setTempUnit("F") },
                            label = { Text("°F", fontSize = 11.sp) },
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                    }
                }
            }
        }

        // Trigger Notifications Switch
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Weather-based alerts system", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(text = "Display automatic reminder triggers on extreme hot or rain states.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }
        }

        // Widget Configuration metadata details
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Comparative Widget Interval", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text(text = "Determines homescreen periodic redraw speed.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(text = widgetInterval, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💡 Practical Tip: Homescreen widgets rely on Android's internal broadcast schedule. The 5-city comparison widget runs at 30 minutes cycle to sustain extremely low battery impact.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Explanatory signature details
        item {
            Text(
                text = "Smart Weather & Local Calendar Hub • v1.0\nDesigned for Punjab & Saraiki Waseeb",
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

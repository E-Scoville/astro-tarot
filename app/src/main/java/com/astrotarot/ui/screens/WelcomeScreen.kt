package com.astrotarot.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.astrotarot.R
import com.astrotarot.ui.ReadingUiState
import com.astrotarot.ui.artNouveauBackground
import com.astrotarot.ui.theme.DimIvory
import com.astrotarot.ui.theme.Gold
import com.astrotarot.ui.theme.GoldFrameBrush
import com.astrotarot.ui.theme.GoldTextBrush
import com.astrotarot.ui.theme.IndigoCard
import com.astrotarot.ui.theme.IndigoSurface
import com.astrotarot.ui.theme.Ivory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private val DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm")

private fun Address.displayName(): String = listOfNotNull(
    locality ?: featureName,
    adminArea,
    countryName,
).joinToString(", ")

// Curated list of major timezone IDs with friendly labels
private val COMMON_ZONES = listOf(
    "Pacific/Honolulu"    to "Honolulu",
    "America/Anchorage"   to "Anchorage",
    "America/Los_Angeles" to "Los Angeles / Seattle",
    "America/Denver"      to "Denver / Salt Lake City",
    "America/Phoenix"     to "Phoenix (no DST)",
    "America/Chicago"     to "Chicago / Dallas",
    "America/New_York"    to "New York / Miami",
    "America/Halifax"     to "Halifax / Atlantic",
    "America/Sao_Paulo"   to "São Paulo / Buenos Aires",
    "Atlantic/Azores"     to "Azores",
    "UTC"                 to "UTC",
    "Europe/London"       to "London / Dublin",
    "Europe/Paris"        to "Paris / Berlin / Rome",
    "Europe/Athens"       to "Athens / Helsinki / Cairo",
    "Europe/Moscow"       to "Moscow",
    "Asia/Dubai"          to "Dubai / Muscat",
    "Asia/Karachi"        to "Karachi / Islamabad",
    "Asia/Kolkata"        to "India",
    "Asia/Dhaka"          to "Dhaka",
    "Asia/Bangkok"        to "Bangkok / Jakarta",
    "Asia/Shanghai"       to "Beijing / Singapore / Perth",
    "Asia/Tokyo"          to "Tokyo / Seoul",
    "Australia/Adelaide"  to "Adelaide",
    "Australia/Sydney"    to "Sydney / Melbourne",
    "Pacific/Auckland"    to "Auckland",
)

private fun ZoneId.friendlyLabel(): String =
    COMMON_ZONES.find { ZoneId.of(it.first) == this }?.second ?: id

private fun ZoneId.utcOffsetLabel(): String {
    val offset = ZonedDateTime.now(this).offset
    return if (offset == ZoneOffset.UTC) "UTC" else "UTC$offset"
}

@Composable
fun WelcomeScreen(
    state: ReadingUiState,
    onReadingRequested: (timestamp: Long) -> Unit,
    onManualCoordinates: (lat: Double, lon: Double, timestamp: Long) -> Unit,
    onShowInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var showLocation  by remember { mutableStateOf(false) }
    var showTime      by remember { mutableStateOf(false) }
    var showZonePicker by remember { mutableStateOf(false) }

    // Location search state
    var searchQuery     by remember { mutableStateOf("") }
    var searchResults   by remember { mutableStateOf<List<Address>>(emptyList()) }
    var selectedAddress by remember { mutableStateOf<Address?>(null) }
    var isSearching     by remember { mutableStateOf(false) }
    var searchError     by remember { mutableStateOf("") }

    // Custom time state — null means "now"
    var customTime    by remember { mutableStateOf<LocalDateTime?>(null) }
    var useCustomTime by remember { mutableStateOf(false) }
    var selectedZone  by remember { mutableStateOf(ZoneId.systemDefault()) }

    fun resolvedTimestamp(): Long =
        if (useCustomTime && customTime != null)
            customTime!!.atZone(selectedZone).toInstant().toEpochMilli()
        else
            System.currentTimeMillis()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onReadingRequested(resolvedTimestamp())
        else showLocation = true
    }

    fun onGpsButtonTapped() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) onReadingRequested(resolvedTimestamp())
        else permissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    fun onSearch() {
        if (searchQuery.isBlank()) return
        scope.launch {
            isSearching = true
            searchError = ""
            searchResults = emptyList()
            selectedAddress = null
            try {
                val geocoder = Geocoder(context)
                val results = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(searchQuery.trim(), 5) ?: emptyList()
                }
                if (results.isEmpty()) searchError = "No places found — try a different name"
                else searchResults = results
            } catch (e: Exception) {
                searchError = "Search unavailable. Check your connection."
            }
            isSearching = false
        }
    }

    fun showDateTimePicker() {
        val base = customTime ?: LocalDateTime.now(selectedZone)
        val cal = Calendar.getInstance().apply {
            set(base.year, base.monthValue - 1, base.dayOfMonth, base.hour, base.minute)
        }
        DatePickerDialog(
            context,
            R.style.AstroPickerDialog,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    R.style.AstroPickerDialog,
                    { _, hour, minute ->
                        customTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                        useCustomTime = true
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true,
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    if (showZonePicker) {
        ZonePickerDialog(
            currentZone = selectedZone,
            onSelect    = { selectedZone = it },
            onDismiss   = { showZonePicker = false },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .artNouveauBackground(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            CelestialRosette()

            Spacer(Modifier.height(36.dp))

            Text(
                text = "ASTRO TAROT",
                style = MaterialTheme.typography.displaySmall.copy(brush = GoldTextBrush),
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "The positions of the planets\nat this exact moment determine\nwhich cards appear.",
                style = MaterialTheme.typography.bodyMedium,
                color = DimIvory,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Nothing is random.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Gold.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )

            TextButton(onClick = onShowInfo) {
                Text(
                    text = "How does this work?",
                    color = Gold.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(40.dp))

            OutlinedButton(
                onClick = ::onGpsButtonTapped,
                shape  = RoundedCornerShape(4.dp),
                border = BorderStroke(1.5.dp, GoldFrameBrush),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = IndigoSurface.copy(alpha = 0.4f),
                    contentColor   = Gold,
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("✦  DRAW A READING  ✦", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            Spacer(Modifier.height(8.dp))

            // ── Location search toggle ────────────────────────────────
            TextButton(onClick = { showLocation = !showLocation }) {
                Text(
                    text = if (showLocation) "⌃ Hide location search"
                           else "⌄ Choose a different location",
                    color = DimIvory,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }

            AnimatedVisibility(visible = showLocation, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Gold,
                        unfocusedBorderColor = DimIvory,
                        focusedLabelColor    = Gold,
                        unfocusedLabelColor  = DimIvory,
                        cursorColor          = Gold,
                        focusedTextColor     = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor   = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                searchError = ""
                                if (selectedAddress != null) selectedAddress = null
                            },
                            label = { Text("City or place name") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = ::onSearch,
                            enabled = searchQuery.isNotBlank() && !isSearching,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor   = MaterialTheme.colorScheme.onSecondary,
                            ),
                        ) {
                            if (isSearching)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                )
                            else
                                Text("Search")
                        }
                    }

                    if (searchError.isNotEmpty()) {
                        Text(
                            text = searchError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    if (searchResults.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        searchResults.forEach { address ->
                            val isSelected = address == selectedAddress
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IndigoCard else IndigoCard.copy(alpha = 0.5f))
                                    .border(1.dp, if (isSelected) Gold else DimIvory.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable { selectedAddress = address }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(if (isSelected) "✦  " else "    ", color = Gold, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = address.displayName(),
                                    color = if (isSelected) Gold else DimIvory,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    if (selectedAddress != null) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                val a = selectedAddress ?: return@OutlinedButton
                                onManualCoordinates(a.latitude, a.longitude, resolvedTimestamp())
                            },
                            shape  = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, GoldFrameBrush),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = IndigoSurface.copy(alpha = 0.4f),
                                contentColor   = Gold,
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text(
                                "✦  DRAW FOR ${selectedAddress!!.displayName().uppercase()}  ✦",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // ── Historical / future time toggle ───────────────────────
            OrnamentalDivider()

            TextButton(onClick = { showTime = !showTime }) {
                Text(
                    text = if (showTime) "⌃ Hide time selection" else "⌄ Choose a different time",
                    color = DimIvory,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }

            AnimatedVisibility(visible = showTime, enter = expandVertically(), exit = shrinkVertically()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                ) {
                    // ── Use current time toggle ───────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Use current time", color = DimIvory, style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = !useCustomTime,
                            onCheckedChange = { nowSelected ->
                                useCustomTime = !nowSelected
                                if (!nowSelected && customTime == null) showDateTimePicker()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = MaterialTheme.colorScheme.primary,
                                checkedTrackColor   = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = DimIvory,
                                uncheckedTrackColor = DimIvory.copy(alpha = 0.3f),
                            ),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Date / time picker button ─────────────────────
                    val zoneOffset = selectedZone.utcOffsetLabel()
                    OutlinedButton(
                        onClick = ::showDateTimePicker,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            1.dp, if (useCustomTime) Gold else DimIvory.copy(alpha = 0.4f),
                        ),
                    ) {
                        Text(
                            text = if (useCustomTime && customTime != null)
                                "${customTime!!.format(DATE_TIME_FMT)}  $zoneOffset"
                            else
                                "Select date & time",
                            color = if (useCustomTime) Gold else DimIvory,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── Timezone selector row ─────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showZonePicker = true }
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                    ) {
                        Text("Timezone", color = DimIvory, style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedZone.friendlyLabel()}  ($zoneOffset)",
                                color = Gold.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text("  ›", color = DimIvory, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (useCustomTime && customTime != null) {
                        Spacer(Modifier.height(2.dp))
                        TextButton(onClick = { useCustomTime = false; customTime = null }) {
                            Text("✕  Clear — use current time", color = DimIvory, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // ── Error message ─────────────────────────────────────────
            if (state is ReadingUiState.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "⚠  ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Timezone picker dialog ────────────────────────────────────────────────────

@Composable
private fun ZonePickerDialog(
    currentZone: ZoneId,
    onSelect: (ZoneId) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = IndigoSurface,
        shape            = RoundedCornerShape(8.dp),
        modifier         = Modifier.border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        title = {
            Text(
                "Select Timezone",
                style = MaterialTheme.typography.titleMedium,
                color = Gold,
            )
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                items(COMMON_ZONES) { (zoneIdStr, label) ->
                    val zoneId     = ZoneId.of(zoneIdStr)
                    val isSelected = zoneId == currentZone
                    val offset     = zoneId.utcOffsetLabel()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(zoneId); onDismiss() }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text  = if (isSelected) "✦  " else "     ",
                            color = Gold,
                            fontSize = 10.sp,
                        )
                        Column {
                            Text(
                                text  = label,
                                color = if (isSelected) Gold else Ivory,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                            Text(
                                text  = offset,
                                color = DimIvory,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    if (zoneIdStr != COMMON_ZONES.last().first) {
                        HorizontalDivider(color = DimIvory.copy(alpha = 0.08f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DimIvory, style = MaterialTheme.typography.bodySmall)
            }
        },
    )
}

// ── Ornamental divider: line ◆ line ──────────────────────────────────────────

@Composable
private fun OrnamentalDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = DimIvory.copy(alpha = 0.25f),
            thickness = 1.dp,
        )
        Text(
            text     = "◆",
            color    = Gold.copy(alpha = 0.5f),
            fontSize = 8.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = DimIvory.copy(alpha = 0.25f),
            thickness = 1.dp,
        )
    }
}

// ── Celestial rosette: rotating zodiac tick ring with lunar glyphs ────────────

@Composable
private fun CelestialRosette() {
    val transition = rememberInfiniteTransition(label = "rosette")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(60_000, easing = LinearEasing), RepeatMode.Restart,
        ),
        label = "rotation",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(130.dp),
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Soft radial glow behind everything
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Gold.copy(alpha = 0.20f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )

            // Middle hairline ring (static)
            drawCircle(
                color  = Gold.copy(alpha = 0.35f),
                radius = radius * 0.72f,
                center = center,
                style  = Stroke(0.5.dp.toPx()),
            )

            // Rotating outer ring with 12 zodiac tick marks
            rotate(rotation, pivot = center) {
                drawCircle(
                    color  = Gold.copy(alpha = 0.5f),
                    radius = radius - 1.dp.toPx(),
                    center = center,
                    style  = Stroke(1.dp.toPx()),
                )
                val tickOuter = radius - 1.dp.toPx()
                val tickInner = radius - 7.dp.toPx()
                for (i in 0 until 12) {
                    val angle = Math.toRadians(i * 30.0)
                    val cosA  = kotlin.math.cos(angle).toFloat()
                    val sinA  = kotlin.math.sin(angle).toFloat()
                    drawLine(
                        color = Gold.copy(alpha = 0.7f),
                        start = Offset(center.x + cosA * tickInner, center.y + sinA * tickInner),
                        end   = Offset(center.x + cosA * tickOuter, center.y + sinA * tickOuter),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
        }
        Text("☽ ✦ ☾", fontSize = 28.sp, color = Gold)
    }
}

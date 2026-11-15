package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArabicScriptFont
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val noorColors = LocalNoorColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        noorColors.deepBackgroundTop,
                        noorColors.deepBackgroundBottom,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Subtle geometric star lattice watermark in top corner
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val patternColor = noorColors.gold.copy(alpha = 0.05f)
            val center = Offset(size.width * 0.14f, size.height * 0.08f)
            val radius = size.minDimension * 0.28f
            drawIslamicOctagram(center, radius, patternColor, strokeWidth = 1.5f)
            drawIslamicOctagram(center, radius * 0.68f, patternColor, strokeWidth = 1.2f)
        }
        content()
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIslamicOctagram(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float = 2f
) {
    for (rot in listOf(0.0, 45.0)) {
        val path = Path()
        for (i in 0 until 4) {
            val angleRad = Math.toRadians(rot + i * 90.0)
            val x = center.x + radius * cos(angleRad).toFloat()
            val y = center.y + radius * sin(angleRad).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
    }
    drawCircle(color = color, radius = radius * 0.42f, center = center, style = Stroke(width = strokeWidth))
}

@Composable
fun NoorGoldCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = LocalNoorColors.current.goldBorder,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_scale"
    )

    val cardModifier = modifier
        .scale(scale)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.material3.ripple(),
                    onClick = onClick
                )
            } else Modifier
        )

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun NoorSectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val noorColors = LocalNoorColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(CircleShape)
                        .background(noorColors.gold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = noorColors.mutedText,
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            Surface(
                onClick = onActionClick,
                shape = RoundedCornerShape(50),
                color = noorColors.gold.copy(alpha = 0.14f),
                border = BorderStroke(0.8.dp, noorColors.goldBorder)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = noorColors.gold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun NoorTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    val noorColors = LocalNoorColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("back_button")
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .border(1.dp, noorColors.goldBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = noorColors.gold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = noorColors.gold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        actions()
    }
}

@Composable
fun BasmalahOrnament(modifier: Modifier = Modifier) {
    val noorColors = LocalNoorColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, noorColors.gold.copy(alpha = 0.65f))
                    )
                )
        )
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            fontFamily = LocalScriptFontFamily.current,
            style = MaterialTheme.typography.headlineMedium,
            color = noorColors.gold,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(noorColors.gold.copy(alpha = 0.65f), Color.Transparent)
                    )
                )
        )
    }
}

data class BottomNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun NoorBottomNavigationBar(
    items: List<BottomNavDestination>,
    currentRoute: String?,
    onSelect: (BottomNavDestination) -> Unit
) {
    val noorColors = LocalNoorColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        color = noorColors.glassSurface,
        border = BorderStroke(1.dp, noorColors.goldBorder),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val tint by animateColorAsState(
                    targetValue = if (selected) noorColors.gold else noorColors.mutedText,
                    label = "nav_tint"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (selected) noorColors.gold.copy(alpha = 0.16f) else Color.Transparent,
                    label = "nav_bg"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.06f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "nav_scale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag(item.testTag)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgColor)
                        .clickable { onSelect(item) }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ArabicFontLivePreviewCard(
    selectedFontId: String,
    previewFontSizeSp: Float = 22f,
    sampleVerseText: String = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾",
    sampleAzkarText: String = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
    modifier: Modifier = Modifier
) {
    val noorColors = LocalNoorColors.current
    val activeFont = ArabicScriptFont.fromId(selectedFontId)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(noorColors.emerald.copy(alpha = 0.14f))
            .border(1.dp, noorColors.goldBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "معاينة حية — ${activeFont.nameArabic}",
                style = MaterialTheme.typography.labelMedium,
                color = noorColors.gold,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sampleVerseText,
                fontFamily = activeFont.fontFamily,
                fontSize = previewFontSizeSp.sp,
                lineHeight = (previewFontSizeSp * 1.65f).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("font_live_preview_verse")
            )
            Text(
                text = sampleAzkarText,
                fontFamily = activeFont.fontFamily,
                style = MaterialTheme.typography.titleMedium,
                color = noorColors.mutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ArabicFontOptionsList(
    selectedFontId: String,
    onSelectFont: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val noorColors = LocalNoorColors.current
    val activeFont = ArabicScriptFont.fromId(selectedFontId)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ArabicScriptFont.entries.forEach { font ->
            val isSelected = font.id == activeFont.id
            Surface(
                onClick = { onSelectFont(font.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("font_option_${font.id}"),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) {
                    noorColors.emerald.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                },
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) noorColors.gold else noorColors.goldBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = font.nameArabic,
                                fontFamily = font.fontFamily,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isSelected) noorColors.gold else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${font.nameEnglish}",
                                style = MaterialTheme.typography.labelMedium,
                                color = noorColors.mutedText
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = font.descriptionArabic,
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.mutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                            fontFamily = font.fontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "الخط المختار",
                            tint = noorColors.gold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArabicFontSelectorSection(
    selectedFontId: String,
    previewFontSizeSp: Float = 22f,
    sampleVerseText: String = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾",
    sampleAzkarText: String = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
    onSelectFont: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ArabicFontLivePreviewCard(
            selectedFontId = selectedFontId,
            previewFontSizeSp = previewFontSizeSp,
            sampleVerseText = sampleVerseText,
            sampleAzkarText = sampleAzkarText
        )
        ArabicFontOptionsList(
            selectedFontId = selectedFontId,
            onSelectFont = onSelectFont
        )
    }
}



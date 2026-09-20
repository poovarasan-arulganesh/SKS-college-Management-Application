package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Apple-inspired Card with clean subtle border, generous 20dp corners,
 * and high-contrast light surface.
 */
@Composable
fun AppleCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppleCardSurface,
    borderColor: Color = SlateBorder,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier.shadow(
            elevation = elevation,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = CharcoalPrimary.copy(alpha = 0.04f),
            spotColor = CharcoalPrimary.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Modern Apple-style primary action button with 16dp rounded corners
 * and 50dp comfortable touch target.
 */
@Composable
fun AppleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = NursingTealDark,
    contentColor: Color = PureWhite,
    testTag: String = "apple_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag(testTag),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = SlateBorder,
            disabledContentColor = SlateMuted
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                )
            }
        }
    }
}

/**
 * Secondary subtle button with soft background container.
 */
@Composable
fun AppleSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = NursingTealSurface,
    contentColor: Color = NursingTealDark,
    testTag: String = "apple_secondary_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag(testTag),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, NursingTealSubtle),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = Typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Apple-inspired Segmented Control (Tabs) with soft pill indicator.
 */
@Composable
fun <T> AppleSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() },
    itemIcon: ((T) -> ImageVector?)? = null,
    testTagPrefix: String = "segment"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = AppleCardElevated,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SlateBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                val targetBg = if (isSelected) PureWhite else Color.Transparent
                val targetTextColor = if (isSelected) CharcoalPrimary else SlateSecondary
                val targetElevation = if (isSelected) 2.dp else 0.dp

                val animatedBg by animateColorAsState(
                    targetValue = targetBg,
                    animationSpec = tween(180),
                    label = "segmentBg"
                )
                val animatedTextColor by animateColorAsState(
                    targetValue = targetTextColor,
                    animationSpec = tween(180),
                    label = "segmentText"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(item) }
                        .testTag("${testTagPrefix}_${itemLabel(item).lowercase().replace(" ", "_")}"),
                    shape = RoundedCornerShape(12.dp),
                    color = animatedBg,
                    shadowElevation = targetElevation
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val icon = itemIcon?.invoke(item)
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) NursingTealDark else SlateMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = itemLabel(item),
                            style = Typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = animatedTextColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern input field with soft background container, clear icon,
 * rounded corners (14dp), and clear label.
 */
@Composable
fun AppleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = "apple_text_field"
) {
    var passwordVisible by remember { mutableStateOf(!isPassword) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = Typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) CrimsonError else CharcoalPrimary,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            placeholder = {
                Text(
                    text = placeholder,
                    style = Typography.bodyMedium,
                    color = SlateMuted
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) CrimsonError else SlateSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = SlateSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = AppleCardElevated,
                disabledContainerColor = AppleCardElevated,
                focusedBorderColor = NursingTealDark,
                unfocusedBorderColor = SlateBorder,
                errorBorderColor = CrimsonError,
                focusedTextColor = CharcoalPrimary,
                unfocusedTextColor = CharcoalPrimary
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = Typography.bodySmall,
                color = CrimsonError,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Status and role pill badge.
 */
@Composable
fun AppleBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = NursingTealSubtle,
    textColor: Color = NursingTealDark,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = Typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.2.sp
        )
    }
}

/**
 * Stat Metric Card for Dashboards.
 */
@Composable
fun AppleStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = NursingTealDark,
    badgeText: String? = null,
    badgeColor: Color = NursingTealSubtle,
    badgeTextColor: Color = NursingTealDark
) {
    AppleCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = Typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = SlateSecondary
            )
            if (badgeText != null) {
                AppleBadge(
                    text = badgeText,
                    containerColor = badgeColor,
                    textColor = badgeTextColor
                )
            } else if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = value,
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CharcoalPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = Typography.bodySmall,
            color = SlateSecondary
        )
    }
}

/**
 * Rounded linear progress indicator with percentage readout.
 */
@Composable
fun AppleProgressBar(
    progress: Float,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier,
    progressColor: Color = NursingTealDark,
    trackColor: Color = SlateBorderSubtle
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = Typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = CharcoalPrimary
            )
            Text(
                text = valueText,
                style = Typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = progressColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = progressColor,
            trackColor = trackColor
        )
    }
}

/**
 * Clean Section Header with title and optional action.
 */
@Composable
fun AppleSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CharcoalPrimary
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = Typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NursingTealDark,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

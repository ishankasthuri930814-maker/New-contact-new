package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.util.Base64

data class AvatarOption(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

object AvatarHelper {
    val avatarOptions = listOf(
        AvatarOption(0, "පොලිස් සහකාර (Police Support)", Icons.Default.LocalPolice, listOf(Color(0xFF0D47A1), Color(0xFF1976D2))),
        AvatarOption(1, "හදිසි ප්‍රතිචාර (Emergency 119)", Icons.Default.Security, listOf(Color(0xFFB71C1C), Color(0xFFE53935))),
        AvatarOption(2, "රථවාහන ආරක්ෂක (Traffic Unit)", Icons.Default.Traffic, listOf(Color(0xFFE65100), Color(0xFFFB8C00))),
        AvatarOption(3, "පුරවැසි සාමාජික (Citizen)", Icons.Default.Person, listOf(Color(0xFF004D40), Color(0xFF00897B))),
        AvatarOption(4, "ප්‍රජා ආරක්ෂක (Community Shield)", Icons.Default.Shield, listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))),
        AvatarOption(5, "වෛද්‍ය සහය (Medical/First Aid)", Icons.Default.MedicalServices, listOf(Color(0xFF880E4F), Color(0xFFC2185B))),
        AvatarOption(6, "තොරතුරු මධ්‍යස්ථානය (Help Desk)", Icons.Default.SupportAgent, listOf(Color(0xFF1B5E20), Color(0xFF43A047))),
        AvatarOption(7, "සාමාන්‍ය ගිණුම (User Profile)", Icons.Default.AccountCircle, listOf(Color(0xFF263238), Color(0xFF546E7A)))
    )

    fun getAvatar(index: Int): AvatarOption {
        return avatarOptions.getOrNull(index.coerceIn(0, avatarOptions.size - 1)) ?: avatarOptions[0]
    }
}

@Composable
fun UserAvatarView(
    avatarIndex: Int,
    displayName: String = "",
    photoUrl: String = "",
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
    borderColor: Color = Color.White
) {
    val customBitmap = remember(photoUrl) {
        if (photoUrl.isNotBlank()) {
            try {
                val clean = if (photoUrl.contains(",")) photoUrl.substringAfter(",") else photoUrl
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap,
            contentDescription = displayName.ifBlank { "Avatar" },
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .then(if (showBorder) Modifier.border(2.dp, borderColor, CircleShape) else Modifier)
        )
    } else {
        val avatar = AvatarHelper.getAvatar(avatarIndex)
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(avatar.gradientColors))
                .then(
                    if (showBorder) Modifier.border(2.dp, borderColor, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (displayName.isNotBlank() && avatarIndex == 3) {
                val initial = displayName.trim().take(1).uppercase()
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.45f).sp
                )
            } else {
                Icon(
                    imageVector = avatar.icon,
                    contentDescription = avatar.name,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
        }
    }
}

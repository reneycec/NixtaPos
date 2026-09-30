package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary

@Composable
fun NixtaNumericPad(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    KeyButton(
                        key = key,
                        onClick = {
                            handleKeyPress(key, value, onValueChange)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDel = key == "DEL"
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDel) Color(0xFFFDECEA) else Color(0xFFF5EFE6))
            .clickable { onClick() }
    ) {
        if (isDel) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Borrar",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = key,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (key == ".") NixtaTerracottaPrimary else NixtaTextPrimary
            )
        }
    }
}

private fun handleKeyPress(key: String, current: String, onValueChange: (String) -> Unit) {
    when (key) {
        "DEL" -> {
            if (current.isNotEmpty()) {
                onValueChange(current.dropLast(1))
            }
        }
        "." -> {
            if (!current.contains(".")) {
                if (current.isEmpty()) onValueChange("0.") else onValueChange(current + ".")
            }
        }
        else -> {
            // Limitar a 2 decimales
            if (current.contains(".")) {
                val parts = current.split(".")
                if (parts[1].length < 2) {
                    onValueChange(current + key)
                }
            } else {
                // Evitar múltiples ceros al inicio
                if (current == "0") {
                    onValueChange(key)
                } else {
                    onValueChange(current + key)
                }
            }
        }
    }
}

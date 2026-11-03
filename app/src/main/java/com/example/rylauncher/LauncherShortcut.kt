package com.example.rylauncher

data class LauncherShortcut(
    val title: String,
    val onClick: () -> Unit,
    val iconRes: Int
)
package com.example.rylauncher

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import java.util.Calendar
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.core.graphics.toColorInt

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apps = listOf(
            LauncherApp(
                title = getString(R.string.moonlight),
                packageName = getString(R.string.moonlight_app),
                coverRes = R.drawable.moonlight_logo,
                backgroundRes = R.drawable.tile_moonlight_background
            ),
            LauncherApp(
                title = getString(R.string.netflix),
                packageName = getString(R.string.netflix_app),
                coverRes = R.drawable.netflix_logo,
                backgroundRes = R.drawable.tile_netflix_background
            ),
            LauncherApp(
                title = getString(R.string.prime_video),
                packageName = getString(R.string.prime_video_app),
                coverRes = R.drawable.prime_video_logo,
                backgroundRes = R.drawable.tile_prime_background
            )
        )

        val shortcuts = listOf(
            LauncherShortcut(
                title = getString(R.string.youtube),
                onClick = {
                    val intent = packageManager.getLeanbackLaunchIntentForPackage(
                        getString(R.string.youtube_app)
                    ) ?: packageManager.getLaunchIntentForPackage(
                        getString(R.string.youtube_app)
                    )

                    launchSafely(intent, getString(R.string.app_not_found))
                },
                iconRes = R.drawable.play_circle_outline,
            ),
            LauncherShortcut(
                title = getString(R.string.TV),
                onClick = {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        setClassName(
                            "org.droidtv.zapster",
                            "org.droidtv.zapster.playtv.activity.PlayTvActivity"
                        )
                    }

                    launchSafely(intent, getString(R.string.tv_not_found))
                },
                iconRes = R.drawable.live_tv
            ),
            LauncherShortcut(
                title = getString(R.string.settings_title),
                onClick = {
                    val intent =
                        Intent(android.provider.Settings.ACTION_SETTINGS)

                    launchSafely(intent, getString(R.string.settings_not_found))
                },
                iconRes = R.drawable.settings
            ),
        )

        val appsRow = findViewById<LinearLayout>(R.id.apps_row)
        populateRow(appsRow, apps, R.layout.item_app_card)


        val shortcutsRow = findViewById<LinearLayout>(R.id.secondary_apps_row)
        populateShortcutRow(shortcutsRow, shortcuts, R.layout.item_shortcut_card)
    }

    private fun populateRow(
        row: LinearLayout,
        apps: List<LauncherApp>,
        cardLayoutRes: Int
    ) {
        apps.forEach { app ->
            val card = layoutInflater.inflate(
                cardLayoutRes,
                row,
                false
            )

            val params = card.layoutParams as LinearLayout.LayoutParams
            params.width = 0
            params.weight = 1f
            card.layoutParams = params

            val cardImage =
                card.findViewById<android.widget.ImageView>(R.id.card_image)

            val cardTitle =
                card.findViewById<TextView>(R.id.card_title)

            val surface =
                card.findViewById<android.view.View>(R.id.card_surface)

            surface.setBackgroundResource(app.backgroundRes)

            val focusOverlay =
                card.findViewById<android.view.View>(R.id.card_focus_overlay)

            cardImage.setImageResource(app.coverRes)
            cardImage.imageTintList =
                if (app.coverRes == R.drawable.prime_video_logo) {
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.WHITE
                    )
                } else {
                    null
                }

            cardTitle.text = app.title

            card.setOnClickListener {
                val launchIntent =
                    packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
                        ?: packageManager.getLaunchIntentForPackage(app.packageName)

                launchSafely(launchIntent, getString(R.string.app_not_found))
            }

            card.setOnFocusChangeListener { _, hasFocus ->
                val scale = if (hasFocus) 1.04f else 1f
                val density = resources.displayMetrics.density
                val lift = if (hasFocus) -4f * density else 0f

                focusOverlay.isSelected = hasFocus

                cardTitle.setTextColor(
                    (if (hasFocus) "#FFFFFF" else "#A7B3C7").toColorInt()
                )

                card.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationY(lift)
                    .setDuration(180L)
                    .start()

                surface.animate()
                    .translationZ(if (hasFocus) 10f * density else 0f)
                    .setDuration(180L)
                    .start()
            }

            row.addView(card)
        }
    }

    private fun populateShortcutRow(
        row: LinearLayout,
        shortcuts: List<LauncherShortcut>,
        cardLayoutRes: Int
    ) {
        shortcuts.forEach { shortcut ->
            val card = layoutInflater.inflate(
                cardLayoutRes,
                row,
                false
            )

            val params = card.layoutParams as LinearLayout.LayoutParams
            params.width = 0
            params.weight = 1f
            card.layoutParams = params

            val cardTitle =
                card.findViewById<TextView>(R.id.shortcut_card_title)

            val cardIcon =
                card.findViewById<android.widget.ImageView>(
                    R.id.shortcut_card_icon
                )

            cardTitle.text = shortcut.title
            cardIcon.setImageResource(shortcut.iconRes)

            card.clipToOutline = true

            card.setOnClickListener {
                shortcut.onClick()
            }

            card.setOnFocusChangeListener { view, hasFocus ->
                val density = resources.displayMetrics.density
                val scale = if (hasFocus) 1.03f else 1f
                val lift = if (hasFocus) -3f * density else 0f

                val color =
                    (if (hasFocus) "#FFFFFF" else "#A7B3C7").toColorInt()

                cardTitle.setTextColor(color)

                cardIcon.imageTintList =
                    android.content.res.ColorStateList.valueOf(color)

                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationY(lift)
                    .translationZ(if (hasFocus) 6f * density else 0f)
                    .setDuration(180L)
                    .start()
            }

            row.addView(card)
        }
    }

    private fun launchSafely(intent: Intent?, errorMessage: String) {
        if (intent == null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private var currentGreetingRes: Int? = null

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val greetingRes = when (hour) {
            in 6..11 -> R.string.morning_greeting
            in 12..17 -> R.string.afternoon_greeting
            in 18..22 -> R.string.evening_greeting
            else -> R.string.night_greeting
        }

        if (greetingRes != currentGreetingRes) {
            findViewById<TextView>(R.id.greeting_text).setText(greetingRes)
            currentGreetingRes = greetingRes
        }
    }

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateGreeting()
        }
    }

    override fun onStart() {
        super.onStart()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        registerReceiver(timeReceiver, filter)
        updateGreeting()
    }

    override fun onStop() {
        unregisterReceiver(timeReceiver)
        super.onStop()
    }

}
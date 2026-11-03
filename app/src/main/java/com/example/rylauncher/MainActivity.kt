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
import android.media.tv.TvContract
import android.widget.Toast

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apps = listOf(
            LauncherApp(
                title = getString(R.string.moonlight),
                packageName = getString(R.string.moonlight_app),
                coverRes = R.drawable.moonlight_cover
            ),
            LauncherApp(
                title = getString(R.string.netflix),
                packageName = getString(R.string.netflix_app),
                coverRes = R.drawable.netflix_cover
            ),
            LauncherApp(
                title = getString(R.string.prime_video),
                packageName = getString(R.string.prime_video_app),
                coverRes = R.drawable.prime_video_cover
            ),
        )

        val shortcuts = listOf(
            LauncherShortcut(
                title = "▶️ ${getString(R.string.youtube)}",
                onClick = {
                    val intent = packageManager.getLeanbackLaunchIntentForPackage(
                        getString(R.string.youtube_app)
                    ) ?: packageManager.getLaunchIntentForPackage(
                        getString(R.string.youtube_app)
                    )

                    if (intent != null) {
                        startActivity(intent)
                    }
                },
            ),
            LauncherShortcut(
                title = "📺 ${getString(R.string.TV)}",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        TvContract.Channels.CONTENT_URI
                    )

                    try {
                        startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Nessuna app disponibile per aprire i canali TV",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
            ),
            LauncherShortcut(
                title = "⚙️ ${getString(R.string.settings_title)}",
                onClick = {
                    val intent =
                        Intent(android.provider.Settings.ACTION_SETTINGS)

                    try {
                        startActivity(intent)
                    } catch (_: android.content.ActivityNotFoundException) {
                    }
                },
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

            val cardImage = card.findViewById<android.widget.ImageView>(R.id.card_image)
            val cardTitle = card.findViewById<TextView>(R.id.card_title)

            cardImage.setImageResource(app.coverRes)
            cardTitle.text = app.title

            card.clipToOutline = true

            card.setOnClickListener {
                val launchIntent =
                    packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
                        ?: packageManager.getLaunchIntentForPackage(app.packageName)

                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    cardTitle.setText(R.string.not_found)
                }
            }

            card.setOnFocusChangeListener { view, hasFocus ->
                val scale = if (hasFocus) 1.06f else 1f

                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(150L)
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

            val cardTitle = card.findViewById<TextView>(R.id.shortcut_card_title)
            cardTitle.text = shortcut.title

            card.clipToOutline = true
            card.setOnClickListener {
                shortcut.onClick()
            }

            card.setOnFocusChangeListener { view, hasFocus ->
                val scale = if (hasFocus) 1.06f else 1f

                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(150L)
                    .start()
            }
            row.addView(card)
        }
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val greetingRes = when (hour) {
            in 6..12 -> R.string.morning_greeting
            in 12..17 -> R.string.afternoon_greeting
            in 18..22 -> R.string.evening_greeting
            else -> R.string.night_greeting
        }

        val greetingTextView = findViewById<TextView>(R.id.greeting_text)
        greetingTextView.setText(greetingRes)
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
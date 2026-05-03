package com.example.tusevinge

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcomeHeader: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnDeposit: Button
    private lateinit var btnWithdraw: Button
    private lateinit var btnHistory: Button
    private lateinit var btnLogout: Button
    private lateinit var balanceTextView: TextView
    private lateinit var goalProgressBar: ProgressBar

    private var balance: Double = 0.0
    private val savingsGoal: Double = 100000.0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleReminders()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before super.onCreate
        val sharedPref = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val themeId = sharedPref.getInt("SelectedTheme", R.style.Theme_Tusevinge)
        setTheme(themeId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val username = intent.getStringExtra("USERNAME") ?: "User"

        // UI references
        tvWelcomeHeader = findViewById(R.id.tvWelcomeHeader)
        imgProfile = findViewById(R.id.imgProfile)
        btnDeposit = findViewById(R.id.btnDeposit)
        btnWithdraw = findViewById(R.id.btnWithdraw)
        btnHistory = findViewById(R.id.btnHistory)
        btnLogout = findViewById(R.id.btnLogout)
        balanceTextView = findViewById(R.id.tvBalance)
        goalProgressBar = findViewById(R.id.goalProgressBar)

        tvWelcomeHeader.text = "WELCOME ${username.uppercase()}"

        loadUserProfile(username)
        checkNotificationPermission()

        btnDeposit.setOnClickListener {
            startActivity(Intent(this, DepositActivity::class.java))
        }

        btnWithdraw.setOnClickListener {
            startActivity(Intent(this, WithdrawActivity::class.java))
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Theme switching logic
        findViewById<Button>(R.id.btnThemeGreen).setOnClickListener { saveTheme(R.style.Theme_Tusevinge) }
        findViewById<Button>(R.id.btnThemeBlue).setOnClickListener { saveTheme(R.style.Theme_Tusevinge_Investment) }
        findViewById<Button>(R.id.btnThemePurple).setOnClickListener { saveTheme(R.style.Theme_Tusevinge_Royal) }
    }

    private fun saveTheme(themeId: Int) {
        val sharedPref = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("SelectedTheme", themeId)
            apply()
        }
        recreate() // Restart activity to apply theme
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleReminders()
            }
        } else {
            scheduleReminders()
        }
    }

    private fun scheduleReminders() {
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "savings_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    private fun loadUserProfile(username: String) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val user = db.userDao().getUserByName(username)
            user?.profileImageUri?.let { uriString ->
                val imgFile = File(uriString)
                if (imgFile.exists()) {
                    imgProfile.setImageURI(Uri.fromFile(imgFile))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        calculateBalance()
    }

    private fun calculateBalance() {
        val db = AppDatabase.getDatabase(this)
        val transactionDao = db.transactionDao()

        lifecycleScope.launch {
            balance = transactionDao.getBalance()
            updateDashboard()
        }
    }

    private fun updateDashboard() {
        balanceTextView.text = "Balance: UGX $balance"
        val progress = if (savingsGoal > 0) ((balance / savingsGoal) * 100).toInt() else 0
        goalProgressBar.progress = progress.coerceIn(0, 100)
        
        if (balance >= savingsGoal && savingsGoal > 0) {
            Toast.makeText(this, "🎉 Savings Goal Achieved!", Toast.LENGTH_SHORT).show()
        }
    }
}

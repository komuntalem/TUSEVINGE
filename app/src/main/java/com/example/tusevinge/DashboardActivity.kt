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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

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
    private lateinit var username: String

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleReminders()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        username = intent.getStringExtra("USERNAME") ?: "User"

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
            val intent = Intent(this, DepositActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        btnWithdraw.setOnClickListener {
            val intent = Intent(this, WithdrawActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
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
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(username)
        dbRef.child("profileImageUri").get().addOnSuccessListener { snapshot ->
            val uriString = snapshot.value?.toString()
            if (!uriString.isNullOrEmpty()) {
                // Using Picasso to load the Firebase Storage URL
                Picasso.get().load(uriString).placeholder(R.drawable.tusevinge).into(imgProfile)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchFirebaseBalance()
    }

    private fun fetchFirebaseBalance() {
        val dbRef = FirebaseDatabase.getInstance().getReference("transactions").child(username)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var currentBalance = 0.0
                for (child in snapshot.children) {
                    val type = child.child("type").value.toString()
                    val amt = child.child("amount").value.toString().toDoubleOrNull() ?: 0.0
                    if (type.lowercase() == "deposit") currentBalance += amt
                    else currentBalance -= amt
                }
                balance = currentBalance
                updateDashboard()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

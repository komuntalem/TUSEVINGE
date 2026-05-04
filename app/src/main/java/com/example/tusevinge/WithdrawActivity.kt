package com.example.tusevinge

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class WithdrawActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        val username = intent.getStringExtra("USERNAME") ?: "User"
        val edtAmount = findViewById<EditText>(R.id.edtWithdrawAmount)
        val edtPhone = findViewById<EditText>(R.id.edtPhoneNumber)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmWithdraw)

        btnConfirm.setOnClickListener {
            val amountStr = edtAmount.text.toString().trim()
            val phone = edtPhone.text.toString().trim()

            if (validateInput(amountStr, phone)) {
                val amount = amountStr.toDouble()
                confirmWithdrawal(username, amount, phone)
            }
        }
    }

    private fun confirmWithdrawal(username: String, amount: Double, phone: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Withdrawal")
            .setMessage("Are you sure you want to withdraw UGX $amount to $phone?")
            .setPositiveButton("Withdraw") { _, _ ->
                checkBalanceAndWithdraw(username, amount, phone)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkBalanceAndWithdraw(username: String, amount: Double, phone: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("transactions").child(username)

        dbRef.get().addOnSuccessListener { snapshot ->
            var currentBalance = 0.0
            for (child in snapshot.children) {
                val type = child.child("type").value.toString()
                val amt = child.child("amount").value.toString().toDoubleOrNull() ?: 0.0
                if (type.lowercase() == "deposit") currentBalance += amt
                else currentBalance -= amt
            }

            if (currentBalance >= amount) {
                processWithdrawal(username, amount, phone)
            } else {
                Toast.makeText(this, "Insufficient balance! Current: UGX $currentBalance", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to check balance: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processWithdrawal(username: String, amount: Double, phone: String) {
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Processing")
            .setMessage("Transferring UGX $amount to $phone...")
            .setCancelable(false)
            .show()

        // Simulate transaction processing
        Handler(Looper.getMainLooper()).postDelayed({
            performFirebaseWithdraw(username, amount, phone)
            progressDialog.dismiss()
        }, 3000)
    }

    private fun performFirebaseWithdraw(username: String, amount: Double, phone: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("transactions").child(username)
        val transactionId = UUID.randomUUID().toString()
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val transaction = mapOf(
            "id" to transactionId,
            "type" to "Withdraw",
            "amount" to amount,
            "date" to date,
            "phone" to phone,
            "status" to "Completed"
        )

        dbRef.child(transactionId).setValue(transaction).addOnSuccessListener {
            showSuccessDialog(amount, phone)
        }.addOnFailureListener {
            Toast.makeText(this, "Withdrawal failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessDialog(amount: Double, phone: String) {
        AlertDialog.Builder(this)
            .setTitle("Withdrawal Successful")
            .setMessage("UGX $amount has been sent to $phone. Please check your mobile money wallet.")
            .setPositiveButton("Done") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun validateInput(amountStr: String, phone: String): Boolean {
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return false
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.isEmpty() || phone.length < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

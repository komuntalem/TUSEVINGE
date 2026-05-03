package com.example.tusevinge

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class WithdrawActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        val edtAmount = findViewById<EditText>(R.id.edtWithdrawAmount)
        val edtPhone = findViewById<EditText>(R.id.edtPhoneNumber)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmWithdraw)

        val db = AppDatabase.getDatabase(this)
        val transactionDao = db.transactionDao()

        btnConfirm.setOnClickListener {
            val amountStr = edtAmount.text.toString().trim()
            val phone = edtPhone.text.toString().trim()

            if (validateInput(amountStr, phone)) {
                val amount = amountStr.toDouble()
                
                lifecycleScope.launch {
                    // Check balance before allowing withdrawal
                    val transactions = transactionDao.getAllTransactions()
                    var currentBalance = 0.0
                    for (t in transactions) {
                        if (t.type.lowercase() == "deposit") currentBalance += t.amount
                        else currentBalance -= t.amount
                    }

                    if (currentBalance >= amount) {
                        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        val transaction = Transaction(type = "Withdraw", amount = amount, date = date)
                        transactionDao.insertTransaction(transaction)
                        
                        Toast.makeText(this@WithdrawActivity, "Withdrew UGX $amount to $phone", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@WithdrawActivity, "Insufficient balance! Current: UGX $currentBalance", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
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
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.length < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

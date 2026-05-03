package com.example.tusevinge

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class DepositActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)

        val edtAmount = findViewById<EditText>(R.id.edtAmount)
        val spnMethod = findViewById<Spinner>(R.id.spnMethod)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmDeposit)

        val db = AppDatabase.getDatabase(this)
        val transactionDao = db.transactionDao()

        // Example payment methods
        val methods = arrayOf("MTN MoMo", "Airtel Money")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, methods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnMethod.adapter = adapter

        btnConfirm.setOnClickListener {
            val amountStr = edtAmount.text.toString().trim()
            val method = spnMethod.selectedItem.toString()

            if (validateInput(amountStr)) {
                val amount = amountStr.toDouble()
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val transaction = Transaction(type = "Deposit", amount = amount, date = date)

                lifecycleScope.launch {
                    transactionDao.insertTransaction(transaction)
                    Toast.makeText(this@DepositActivity, "Successfully deposited UGX $amount via $method", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun validateInput(amountStr: String): Boolean {
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return false
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

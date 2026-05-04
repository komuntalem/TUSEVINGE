package com.example.tusevinge

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class DepositActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)

        val username = intent.getStringExtra("USERNAME") ?: "User"
        val edtAmount = findViewById<EditText>(R.id.edtAmount)
        val edtPhoneNumber = findViewById<EditText>(R.id.edtPhoneNumber)
        val spnMethod = findViewById<Spinner>(R.id.spnMethod)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmDeposit)

        val methods = arrayOf("MTN MoMo", "Airtel Money")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, methods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnMethod.adapter = adapter

        btnConfirm.setOnClickListener {
            val amountStr = edtAmount.text.toString().trim()
            val phone = edtPhoneNumber.text.toString().trim()
            val method = spnMethod.selectedItem.toString()

            if (validateInput(amountStr, phone)) {
                val amount = amountStr.toDouble()
                showSimulatedPinPrompt(username, amount, phone, method)
            }
        }
    }

    private fun showSimulatedPinPrompt(username: String, amount: Double, phone: String, method: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("$method Payment")
        builder.setMessage("You are depositing UGX $amount from $phone. \n\nPlease enter your Mobile Money PIN on the prompt sent to your phone (Simulated).")

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "Enter PIN"
        
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 0)
        container.addView(input, params)
        builder.setView(container)

        builder.setPositiveButton("Authorize") { dialog, _ ->
            val pin = input.text.toString()
            if (pin.isNotEmpty()) {
                processSimulatedTransaction(username, amount)
            } else {
                Toast.makeText(this, "PIN is required", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun processSimulatedTransaction(username: String, amount: Double) {
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Processing")
            .setMessage("Verifying with service provider...")
            .setCancelable(false)
            .show()

        Handler(Looper.getMainLooper()).postDelayed({
            performFirebaseDeposit(username, amount)
            progressDialog.dismiss()
        }, 2500)
    }

    private fun performFirebaseDeposit(username: String, amount: Double) {
        val dbRef = FirebaseDatabase.getInstance().getReference("transactions").child(username)
        val transactionId = UUID.randomUUID().toString()
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        
        val transaction = mapOf(
            "id" to transactionId,
            "type" to "Deposit",
            "amount" to amount,
            "date" to date,
            "status" to "Completed"
        )

        dbRef.child(transactionId).setValue(transaction).addOnSuccessListener {
            AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("UGX $amount successfully deposited.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error updating balance", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(amountStr: String, phone: String): Boolean {
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return false
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.length < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

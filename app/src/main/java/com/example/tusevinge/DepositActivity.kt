package com.example.tusevinge

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.text.SimpleDateFormat
import java.util.*

class DepositActivity : AppCompatActivity() {

    private val DB_URL = "https://savingsapp-e1241-default-rtdb.firebaseio.com/"

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

            if (validateInput(amountStr, phone)) {
                val amount = amountStr.toDouble()
                showSimulatedPinPrompt(username, amount, phone, spnMethod.selectedItem.toString())
            }
        }
    }

    private fun showSimulatedPinPrompt(username: String, amount: Double, phone: String, method: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("$method Payment")
        builder.setMessage("Depositing UGX $amount from $phone. \n\nEnter PIN to authorize.")

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 20, 50, 0)
        container.addView(input, params)
        builder.setView(container)

        builder.setPositiveButton("Authorize") { _, _ ->
            if (input.text.isNotEmpty()) processSimulatedTransaction(username, amount, phone)
            else Toast.makeText(this, "PIN is required", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun processSimulatedTransaction(username: String, amount: Double, phone: String) {
        val progress = AlertDialog.Builder(this).setMessage("Processing...").setCancelable(false).show()
        Handler(Looper.getMainLooper()).postDelayed({
            performFirebaseDeposit(username, amount, phone)
            progress.dismiss()
        }, 2000)
    }

    private fun performFirebaseDeposit(username: String, amount: Double, phone: String) {
        val rootRef = FirebaseDatabase.getInstance(DB_URL).reference
        val transactionId = UUID.randomUUID().toString()
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        
        val transaction = mapOf(
            "id" to transactionId,
            "username" to username,
            "type" to "deposit",
            "amount" to amount,
            "date" to date,
            "timestamp" to System.currentTimeMillis(),
            "status" to "Completed",
            "phone" to phone
        )

        rootRef.child("transactions").child(transactionId).setValue(transaction).addOnSuccessListener {
            rootRef.child("users").child(username).child("balance").setValue(ServerValue.increment(amount))
            AlertDialog.Builder(this).setTitle("Success").setMessage("Deposit of UGX $amount successful!")
                .setPositiveButton("OK") { _, _ -> finish() }.show()
        }.addOnFailureListener {
            Toast.makeText(this, "Deposit failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateInput(amountStr: String, phone: String): Boolean {
        if (amountStr.isEmpty() || (amountStr.toDoubleOrNull() ?: 0.0) <= 0) {
            Toast.makeText(this, "Valid amount required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.length < 10) {
            Toast.makeText(this, "Valid phone number required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

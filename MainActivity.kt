package com.example.callblocker

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val roleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { updateStatus() }

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) requestScreeningRole() else updateStatus()
        }

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        status = TextView(this).apply {
            textSize = 18f
            setPadding(48, 96, 48, 48)
        }
        setContentView(status)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            requestScreeningRole()
        } else {
            permLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun requestScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                !rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            ) {
                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
            }
        }
    }

    private fun updateStatus() {
        val hasContacts = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val hasRole = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_CALL_SCREENING)

        status.text = if (hasContacts && hasRole) {
            "✅ চালু আছে\n\nকন্টাক্টে নেই এমন নাম্বার থেকে কল এলে ব্লক হবে এবং কল লগ ও নোটিফিকেশনে দেখাবে না।"
        } else {
            "⚠️ সেটআপ বাকি\n\n" +
                (if (hasContacts) "" else "• কন্টাক্ট পারমিশন দিন\n") +
                (if (hasRole) "" else "• এই অ্যাপকে Call screening অ্যাপ হিসেবে সেট করুন\n") +
                "\nঅ্যাপটি আবার খুলুন।"
        }
    }
}

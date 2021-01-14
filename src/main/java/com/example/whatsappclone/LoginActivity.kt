package com.example.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var phnNumber:String
    private lateinit var countryCode:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // TODO: Add Hint Request for Phone Number.

        phoneNumber.addTextChangedListener {
            nextBtn.isEnabled =!(it.isNullOrEmpty() || it.length < 10)
        }
        nextBtn.setOnClickListener {
            checkNumber()
        }
    }

    // For Creating the whole number with it's country code.
    private fun checkNumber() {
        countryCode = ccp.selectedCountryCodeWithPlus
        phnNumber = countryCode + phoneNumber.text.toString()

        notifyUser()
    }

    // Alert Dialog for User. In order to Edit Number or continue.
    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(" We will be verifying the phone number:$phnNumber\n" + "Is this Ok, or would you like to edit the number?")
            setPositiveButton("Ok"){ _,_ ->
                showOptActivity()
            }
            setNegativeButton("Edit"){ dialog , which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }


    // Sending the phone number while accepting the Alert Dialog to the next Activity.
    private fun showOptActivity() {
        startActivity(Intent(this , OtpActivity::class.java).putExtra(PHONE_NUMBER , phnNumber))
        finish()
    }


}
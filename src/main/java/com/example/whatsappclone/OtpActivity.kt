package com.example.whatsappclone

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Message
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phnNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber:String? = null
    var mVerificationId: String? = null
    var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var progressDialog: ProgressDialog
    private var mCounterDown: CountDownTimer?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        initViews()
        startVerify()

    }

    private fun startVerify() {
        showTimer(60000)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber!!, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks
        // Creating Dialog
        progressDialog = createProgressDialog("Sending a verification code", false)
        progressDialog.show()
    }

    private fun showTimer(milliSecInFuture: Long) {
        resendBTN.isEnabled = false
        mCounterDown = object : CountDownTimer(milliSecInFuture , 1000) {
            override fun onTick(p0: Long) {
                counterTv.isVisible = true
                counterTv.text = getString(R.string.second_remaining , p0/1000)
            }

            override fun onFinish() {
                resendBTN.isEnabled = true
                counterTv.isVisible = false
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCounterDown != null) {
            mCounterDown!!.cancel()
        }
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifying.text = getString(R.string.verfy_num , phoneNumber)
        setSpannableString()

        verificationBTN.setOnClickListener(this)
        resendBTN.setOnClickListener(this)

        // Callback for Authentication Through OTP.(Firebase)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                val smsCode: String? = p0.smsCode
                // THis auto fill the OTP
                if(!smsCode.isNullOrBlank())
                    sendcodeET.setText(smsCode)
                 signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                if(p0 is FirebaseAuthInvalidCredentialsException) {

                } else if (p0 is FirebaseTooManyRequestsException) {

                }
                Log.e("ERROR_FIREBASE" , p0.localizedMessage)
                notifyUserAndRetry("Your Phone Number might be wrong or connection error. Retry again!")

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                counterTv.isVisible = false
                mVerificationId = verificationId
                mResendToken = token


            }
        }


    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this , SingUpActivity::class.java)
                    )
                    finish()
                } else {
                    notifyUserAndRetry("Your Phone Number verfication is failed. Try Again!")
                }
            }

    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok"){ _,_ ->
                showLoginActivity()
            }
            setNegativeButton("Edit"){ dialog , which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text, phoneNumber ))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                // Send back
                showLoginActivity()
            }

            // Used to set the text color and underlining or not.
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor

            }
        }
        span.setSpan(clickableSpan , span.length - 14 , span.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this , LoginActivity::class.java ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    // Simple hack to disable the back pressed on the activity.
    override fun onBackPressed() {

    }

    override fun onClick(p0: View?) {
        when(p0) {
            verificationBTN -> {

                val code: String = sendcodeET.text.toString()
                if (code.isNotEmpty() && !mVerificationId.isNullOrBlank()) {
                    progressDialog = createProgressDialog("Please Wait....." , true)
                    progressDialog.show()
                    val credential: PhoneAuthCredential =
                        PhoneAuthProvider.getCredential(mVerificationId!! , code)
                        signInWithPhoneAuthCredential(credential)
                }
            }
            resendBTN -> {

                val code: String = sendcodeET.text.toString()
                if (mResendToken != null) {
                    showTimer(60000)
                    progressDialog = createProgressDialog("Please Wait....." , true)
                    progressDialog.show()
                   PhoneAuthProvider.getInstance().verifyPhoneNumber(
                       phoneNumber!!,
                       60,
                       TimeUnit.SECONDS,
                       this,
                       callbacks,
                       mResendToken
                   )
                }
            }
        }
    }
}

 // Creating an Extension function , We can use it anywhere. This is a Dialog which we can add anywhere.
fun Context.createProgressDialog(message: String , isCancelable: Boolean): ProgressDialog {
     return ProgressDialog(this).apply {
         setCancelable(false)
         setMessage(message)
         setCanceledOnTouchOutside(false)
     }
 }
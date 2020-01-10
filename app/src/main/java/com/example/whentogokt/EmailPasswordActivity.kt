package com.example.whentogokt

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import android.text.TextUtils
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_emailpassword.detail
import kotlinx.android.synthetic.main.activity_emailpassword.emailCreateAccountButton
import kotlinx.android.synthetic.main.activity_emailpassword.emailPasswordButtons
import kotlinx.android.synthetic.main.activity_emailpassword.emailPasswordFields
import kotlinx.android.synthetic.main.activity_emailpassword.emailSignInButton
import kotlinx.android.synthetic.main.activity_emailpassword.fieldEmail
import kotlinx.android.synthetic.main.activity_emailpassword.fieldPassword
import kotlinx.android.synthetic.main.activity_emailpassword.signOutButton
import kotlinx.android.synthetic.main.activity_emailpassword.signedInButtons
import kotlinx.android.synthetic.main.activity_emailpassword.status
import kotlinx.android.synthetic.main.activity_emailpassword.verifyEmailButton

class EmailPasswordActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emailpassword)
        setProgressBar(R.id.progressBar)

        // Buttons
        emailSignInButton.setOnClickListener(this)
        emailCreateAccountButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)
        verifyEmailButton.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()

    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser

        updateUI(currentUser)

        // 이미 로그인 되어있다면
        if ((currentUser?.isEmailVerified == true) && currentUser != null){
            val intent = Intent(this, Main2Activity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun createAccount(email: String, password: String) {
        if (!validateForm()) {return}

        showProgressBar()

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){ task ->
            if (task.isSuccessful){
                // 가입이 성공하면 업데이트 된 유저 접속
                val user = mAuth.currentUser
                updateUI(user)
            } else {
                // 가입이 실패하면 가입 실패 메세지를 보여줌
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }

            hideProgressBar()

        }
    }

    private fun signIn(email: String, password: String){
        if (!validateForm()) {return}

        showProgressBar()

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인이 성공하면 다음 Activity로 이동
                    Log.d(TAG, "signInWithEmail:success")
                    val user = mAuth.currentUser

                    updateUI(user)

                    val intent = Intent(this, Main2Activity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // 로그인 실패
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // 인증 실패
                if (!task.isSuccessful) {
                    status.setText(R.string.auth_failed)
                }
                hideProgressBar()
            }
    }

    private fun signOut() {
        // 로그아웃
        mAuth.signOut()
        updateUI(null)
    }

    private fun sendEmailVerification() {
        // Disable button
        verifyEmailButton.isEnabled = false

        // Send verification email
        // [START send_email_verification]
        val user = mAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
                // [START_EXCLUDE]
                // Re-enable button
                verifyEmailButton.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(baseContext,
                        "인증 메일이 발송 되었습니다.${user.email}",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext,
                        "인증이 실패하였습니다.",
                        Toast.LENGTH_SHORT).show()
                }
                // [END_EXCLUDE]
            }
        // [END send_email_verification]
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = fieldEmail.text.toString()

        if (TextUtils.isEmpty(email)) {
            fieldEmail.error = "이메일을 작성해 주세요."
            valid = false
        } else {
            fieldEmail.error = null
        }

        val password = fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fieldPassword.error = "비밀번호를 작성해 주세요."
            valid = false
        } else {
            fieldPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            status.text = getString(R.string.emailpassword_status_fmt, user.email, user.isEmailVerified)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)

            emailPasswordButtons.visibility = View.GONE
            emailPasswordFields.visibility = View.GONE
            signedInButtons.visibility = View.VISIBLE

            verifyEmailButton.isEnabled = !user.isEmailVerified
        } else {
            status.setText(R.string.signed_out)
            detail.text = null

            emailPasswordButtons.visibility = View.VISIBLE
            emailPasswordFields.visibility = View.VISIBLE
            signedInButtons.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.emailCreateAccountButton -> createAccount(fieldEmail.text.toString(), fieldPassword.text.toString())
            R.id.emailSignInButton -> signIn(fieldEmail.text.toString(), fieldPassword.text.toString())
            R.id.signOutButton -> signOut()
            R.id.verifyEmailButton -> sendEmailVerification()
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }


}
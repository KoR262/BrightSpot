package com.example.brightspot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.btn_register
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_register.setOnClickListener{
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        btn_login.setOnClickListener{
            when {
                TextUtils.isEmpty(tiet_login_email.text.toString().trim{ it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Пожалуйста введите email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(tiet_login_password.text.toString().trim{ it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Пожалуйста введите пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val email: String = tiet_login_email.text.toString().trim() { it <= ' ' }
                    val password: String = tiet_login_password.text.toString().trim() { it <= ' ' }

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Авторизация выполнена успешно",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val firebaseUser: FirebaseUser = task.result!!.user!!

                                val intent =
                                    Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
//                              intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }
}
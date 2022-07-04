package com.example.brightspot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var uDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register.setOnClickListener{
            when {
                TextUtils.isEmpty(tiet_register_email.text.toString().trim{ it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Пожалуйста введите email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(tiet_register_nickname.text.toString().trim{ it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Пожалуйста введите nickname",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(tiet_register_password.text.toString().trim{ it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Пожалуйста введите пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val email: String = tiet_register_email.text.toString().trim(){ it <= ' ' }
                    val password: String = tiet_register_password.text.toString().trim(){ it <= ' ' }
                    val nickname: String = tiet_register_nickname.text.toString().trim(){ it <= ' ' }

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (task.isSuccessful){
                                    val firebaseUser: FirebaseUser = task.result!!.user!!
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Регистрация выполнена успешно",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    uDatabase = FirebaseDatabase.getInstance().getReference("Users")
                                    val User = User(nickname, null, null)
//                                    Log.d("RegisterActivity", "Класс юзер: $User")
//                                    Log.d("RegisterActivity", "User id: ${firebaseUser.uid}")
//                                    Log.d("RegisterActivity", "Nickname: $nickname")
                                    uDatabase.child(firebaseUser.uid).setValue(User).addOnSuccessListener {
                                        Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener{
                                        Toast.makeText(this, "Не добавлена", Toast.LENGTH_SHORT).show()
                                    }

                                    val intent =
                                        Intent(this@RegisterActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                    intent.putExtra("user_id", firebaseUser.uid)
//                                    intent.putExtra("email", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                }
            }
        }
    }
}
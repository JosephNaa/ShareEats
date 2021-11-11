package com.sharewanted.shareeats.src.main.home.participate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sharewanted.shareeats.R
import com.sharewanted.shareeats.config.ApplicationClass
import com.sharewanted.shareeats.database.creditcard.CreditCard
import com.sharewanted.shareeats.database.creditcard.CreditCardRepository
import com.sharewanted.shareeats.databinding.ActivityParticipateBinding
import com.sharewanted.shareeats.src.main.MainActivity
import com.sharewanted.shareeats.src.main.home.HomeFragment
import com.sharewanted.shareeats.src.main.home.order.orderDto.Post
import com.sharewanted.shareeats.src.main.home.order.orderDto.StoreMenu
import com.sharewanted.shareeats.src.main.userlogin.dto.UserDto
import com.sharewanted.shareeats.util.SharedPreferencesUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

private const val TAG = "ParticipateActivity_싸피"
class ParticipateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParticipateBinding
    private lateinit var adapter: ParticipateAdapter
    var selectedCard: CreditCard?=null
    lateinit var post: Post
    private var menuList: MutableList<Menu> = arrayListOf()
    private var mDatabase = Firebase.database.reference
    private var dataInputFlag = -1
    lateinit var user: UserDto

    private var cardPassword: String?=null

    lateinit var creditCardRepository: CreditCardRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParticipateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        creditCardRepository = CreditCardRepository.get()

        val cardList = resources.getStringArray(R.array.credit_card)

        user = ApplicationClass.sharedPreferencesUtil.getUser()

        val toolbar = binding.activityParticipateToolbar
        setSupportActionBar(toolbar)

        user = ApplicationClass.sharedPreferencesUtil.getUser()

        post = intent.getSerializableExtra("post") as Post
        menuList = intent.getParcelableArrayListExtra<Menu>("menu") as ArrayList<Menu>
        dataInputFlag = intent.getIntExtra("flag", -1)

        Log.d(TAG, "onCreate: $post")
        Log.d(TAG, "onCreate: $menuList")
        
        var totalPrice = 0
        for (i in menuList.indices) {
            totalPrice += menuList[i].price * menuList[i].quantity
        }

        binding.activityParticipateTvOrderDate.text = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        binding.activityParticipateTvTotalPrice.text = "${totalPrice} 원"
        
        initView()


        Log.d(TAG, "onCreate: ")
        binding.activityParticipateBtnCancel.setOnClickListener {
            Toast.makeText(this, "뒤로가기", Toast.LENGTH_SHORT).show()
//            finish()
        }

        binding.activityParticipateBtnPayment.setOnClickListener {
            val company = binding.activityParticipateSpinner.selectedItem.toString()
            val number = binding.activityParticipateCardNumber.text.toString()
            val expiry = binding.activityParticipateExpiry.text.toString()
            val cvc = binding.activityParticipateCvc.text.toString()
            val password = binding.activityParticipatePassword.text.toString()

            if (cardPassword != "") {
                if (cardPassword.equals(password)) {
                    Toast.makeText(this, "결제하기", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: $company $number $expiry $cvc $password")
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
                    mDatabase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (dataInputFlag == 0) {
                                if (snapshot.hasChildren()) {
                                    post.postId = snapshot.child("Post").children.last().key!!.toInt() + 1
                                }

                                mDatabase.child("Post").child(post.postId.toString()).setValue(post)

                                for (i in menuList.indices) {
                                    mDatabase.child("Post").child(post.postId.toString())
                                        .child("participant").child(user.id)
                                        .child("menu").child(menuList[i].name).setValue(menuList[i])
                                }

                                val map = mapOf("postId" to post.postId)
                                mDatabase.child("User").child(user.id).child("postList").child(post.postId.toString()).setValue(map)

                                dataInputFlag = 1

                                Toast.makeText(this@ParticipateActivity, "글 작성완료", Toast.LENGTH_SHORT).show()


                            } else if (dataInputFlag == 1) {
                                for (i in menuList.indices) {
                                    mDatabase.child("Post").child(post.postId.toString())
                                        .child("participant").child(user.id)
                                        .child("menu").child(menuList[i].name).setValue(menuList[i])
                                }

                                val map = mapOf("postId" to post.postId)
                                mDatabase.child("User").child(user.id).child("postList").child(post.postId.toString()).setValue(map)

                                Toast.makeText(this@ParticipateActivity, "참여완료", Toast.LENGTH_SHORT).show()

                            }

                            val intent = Intent(this@ParticipateActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent)
                            finish()

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ParticipateActivity, error.toString(), Toast.LENGTH_SHORT).show()
                        }

                    })
                } else {
                    Toast.makeText(this, "비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (company.isNotEmpty() && number.isNotEmpty() && expiry.isNotEmpty() && cvc.isNotEmpty() && password.isNotEmpty()) {
                    val creditCard = CreditCard(user.id, company, number, expiry, cvc, password)
                    CoroutineScope(Dispatchers.IO).launch {
                        creditCardRepository.insert(creditCard)
                    }
                    cardPassword = password
                }
            }
        }


        val cardAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cardList)

        binding.activityParticipateSpinner.adapter = cardAdapter

        binding.activityParticipateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Toast.makeText(this@ParticipateActivity, "${cardList[position]}", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.Main).launch {
                    CoroutineScope(Dispatchers.IO).async {
                        selectedCard = creditCardRepository.getCreditCard(user.id, cardList[position])
                    }.await()

                    updateView()
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

    }

    private fun initView() {
        adapter = ParticipateAdapter(menuList)
        binding.activityParticipateRvMenu.adapter = adapter
        binding.activityParticipateRvMenu.layoutManager = LinearLayoutManager(this)

    }

    fun updateView() {
        if (selectedCard != null) {
            binding.activityParticipateCardNumber.setText(selectedCard!!.number)
            binding.activityParticipateExpiry.setText(selectedCard!!.expiry)
            binding.activityParticipateCvc.setText(selectedCard!!.cvc)
            cardPassword = selectedCard!!.password
        } else {
            binding.activityParticipateCardNumber.setText("")
            binding.activityParticipateExpiry.setText("")
            binding.activityParticipateCvc.setText("")
            binding.activityParticipatePassword.setText("")
            cardPassword = ""
        }
    }
}
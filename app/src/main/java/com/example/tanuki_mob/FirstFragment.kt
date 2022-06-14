package com.example.tanuki_mob

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tanuki_mob.databinding.FragmentFirstBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val db = Firebase.firestore
    private var usersRef = db.collection("/users")
    private var hiraganaRef = db.collection("/hiragana")
    private var katakanaRef = db.collection("/katakana")
    private var kanjiRef = db.collection("/kanji")

    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet
    private var isFront = true

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        val cardBack = binding.cardBack
        val cardFront = binding.cardFront

        // read json from assets and assign to a String value
        val hiraganaJsonFileString = getJsonDataFromAsset(requireContext(), "hiragana.json")
        val katakanaJsonFileString = getJsonDataFromAsset(requireContext(), "katakana.json")
        val kanjiJsonFileString = getJsonDataFromAsset(requireContext(), "kanji.json")

        // log the contents of the String
//        if (kanjiJsonFileString != null) {
//            Log.i("data", kanjiJsonFileString)
//        }

        // use Gson to deserialize the specified Json into an object type List<Kanji>
        val gson = Gson()
        val listHiraganaType = object : TypeToken<List<Kana>>() {}.type
        val listKatakanaType = object : TypeToken<List<Kana>>() {}.type
        val listKanjiType = object : TypeToken<List<Kanji>>() {}.type
        val hiraganaList: List<Kana> = gson.fromJson(hiraganaJsonFileString, listHiraganaType)
        val katakanaList: List<Kana> = gson.fromJson(katakanaJsonFileString, listKatakanaType)
        val kanjiList: List<Kanji> = gson.fromJson(kanjiJsonFileString, listKanjiType)

        // log the contents of the list
//        kanjiList.forEachIndexed { idx, kanji -> Log.i("data", "> Item $idx:\n$kanji") }

//        // button to add Hiragana from json to Firestore Database
//        binding.buttonAddHiragana.setOnClickListener {
//            hiraganaList.forEach {
//                addHiragana(it)
//                    .addOnSuccessListener { documentReference ->
//                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w(TAG, "Error adding document", e)
//                    }
//            }
//        }
//
//        // button to add Katakana from json to Firestore Database
//        binding.buttonAddKatakana.setOnClickListener {
//            katakanaList.forEach {
//                addKatakana(it)
//                    .addOnSuccessListener { documentReference ->
//                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w(TAG, "Error adding document", e)
//                    }
//            }
//        }
//
//        // button to add Kanji from json to Firestore Database
//        binding.buttonAddKanji.setOnClickListener {
//            kanjiList.forEach {
//                addKanji(it)
//                    .addOnSuccessListener { documentReference ->
//                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w(TAG, "Error adding document", e)
//                    }
//            }
//        }

        getSingleUserByName("Ada")
            .get()
            .addOnSuccessListener { documentReference ->
                for (document in documentReference) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        getSingleKanjiById(1)
            .get()
            .addOnSuccessListener { documentReference ->
                for (document in documentReference) {
                    val meanArray = document.data["meaning"] as ArrayList<*>
                    cardBack.text = meanArray[0] as CharSequence?
                    cardFront.text = document.data["sign"] as CharSequence?
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        frontAnimation = AnimatorInflater.loadAnimator(context, R.animator.front_card_animator) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(context, R.animator.back_card_animator) as AnimatorSet

        binding.buttonFlip.setOnClickListener {
            flipCard(requireContext(), cardFront, cardBack)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun getSingleKanjiById(id: Int): Query {
        return kanjiRef.whereEqualTo("id", id)
    }

    private fun addKanji(kanji: Kanji): Task<DocumentReference> {
        return kanjiRef.add(kanji)
    }

    private fun addHiragana(hiragana: Kana): Task<DocumentReference> {
        return hiraganaRef.add(hiragana)
    }

    private fun addKatakana(katakana: Kana): Task<DocumentReference> {
        return katakanaRef.add(katakana)
    }

    private fun getSingleUserByName(name: String): Query {
        return usersRef.whereEqualTo("first", name)
    }

    private fun flipCard(context: Context, frontView: View, backView: View) {
        try {
            val scale = context.resources.displayMetrics.density
            frontView.cameraDistance = 8000 * scale
            backView.cameraDistance = 8000 * scale

            if(isFront) {
                frontAnimation.setTarget(frontView)
                backAnimation.setTarget(backView)
                frontAnimation.start()
                backAnimation.start()
                isFront = false
            } else {
                frontAnimation.setTarget(backView)
                backAnimation.setTarget(frontView)
                backAnimation.start()
                frontAnimation.start()
                isFront = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Flip failed: ", e)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
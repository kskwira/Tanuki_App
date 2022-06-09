package com.example.tanuki_mob

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
    private var kanjiRef = db.collection("/kanji")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        // read json from assets and assign to a String value
        val jsonFileString = getJsonDataFromAsset(requireContext(), "kanji.json")

        // log the contents of the String
//        if (jsonFileString != null) {
//            Log.i("data", jsonFileString)
//        }

        // use Gson to deserialize the specified Json into an object type List<Kanji>
        val gson = Gson()
        val listKanjiType = object : TypeToken<List<Kanji>>() {}.type
        val kanjiList: List<Kanji> = gson.fromJson(jsonFileString, listKanjiType)

        // log the contents of the list
//        kanjiList.forEachIndexed { idx, kanji -> Log.i("data", "> Item $idx:\n$kanji") }

        // button to add Kanji from json to Firestore Database
        binding.buttonSecond.setOnClickListener {
            kanjiList.forEach {
                addKanji(it)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }
        }

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
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
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

    private fun getSingleUserByName(name: String): Query {
        return usersRef.whereEqualTo("first", name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.tanuki_mob

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tanuki_mob.databinding.FragmentMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // keys for reading data from SharedPreferences
    private var CHOICES = "pref_numberOfChoices"
    private var SIGNS = "pref_signsToInclude"

    // Firestore collections
    private val db = Firebase.firestore
    private var hiraganaRef = db.collection("/hiragana")
    private var katakanaRef = db.collection("/katakana")
    private var kanjiRef = db.collection("/kanji")

    // animations
    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet
    private var isFront = true

    // signs and correct answer sets
    private lateinit var kanjiArrayList: ArrayList<Kanji>
    private lateinit var hiraganaArrayList: ArrayList<Kana>
    private lateinit var katakanaArrayList: ArrayList<Kana>
    private lateinit var quizListKanji: ArrayList<Kanji>
    private lateinit var quizListHiragana: ArrayList<Kana>
    private lateinit var quizListKatakana: ArrayList<Kana>
    private lateinit var quizListKanjiMeaning: ArrayList<List<String>>
    private lateinit var quizListHiraganaReading: ArrayList<String>
    private lateinit var quizListKatakanaReading: ArrayList<String>
    private lateinit var correctAnswerKanji: List<String>
    private lateinit var correctAnswerKana: String

    private val signsInQuiz = 15 // number of signs in one quiz
    private var totalGuesses = 0 // number of guesses made
    private var correctAnswers = 0 // number of correct guesses
    private var guessRows = 0 // number of rows displaying guess Buttons
    private var signsSet = "" // set of signs used in the quiz
    private var answer = "" // answer chosen during the quiz
    private var random : SecureRandom? = null // used to randomize the quiz
    private var handler : Handler? = null // used to delay loading next sign

    private lateinit var questionNumberTextView : TextView // shows current question #
    private lateinit var guessLinearLayouts : Array<LinearLayout?>  // rows of answer Buttons
    private var answerTextView : TextView? = null // displays correct answer
    private var cardFront : TextView? = null // displays card front
    private var cardBack : TextView? = null // displays card back

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainBinding.inflate(inflater, container, false)

        // initializing and binding
        random = SecureRandom()
        handler = Handler()
        kanjiArrayList = ArrayList()
        hiraganaArrayList = ArrayList()
        katakanaArrayList = ArrayList()
        quizListKanji = ArrayList()
        quizListHiragana = ArrayList()
        quizListKatakana = ArrayList()
        quizListKanjiMeaning = ArrayList()
        quizListHiraganaReading = ArrayList()
        quizListKatakanaReading = ArrayList()
        guessLinearLayouts = arrayOfNulls(4)
        guessLinearLayouts[0] = binding.row1LinearLayout
        guessLinearLayouts[1] = binding.row2LinearLayout
        guessLinearLayouts[2] = binding.row3LinearLayout
        guessLinearLayouts[3] = binding.row4LinearLayout
        answerTextView = binding.answerTextView
        questionNumberTextView = binding.questionNumberTextView
        cardBack = binding.cardBack
        cardFront = binding.cardFront
        frontAnimation = AnimatorInflater.loadAnimator(context, R.animator.front_card_animator) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(context, R.animator.back_card_animator) as AnimatorSet

        // get all kanji from Firestore
        kanjiRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val kanji = document.toObject<Kanji>()
                    kanjiArrayList.add(kanji)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting kanji from Firestore: ", exception)
            }

        // get all hiragana from Firestore
        hiraganaRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val hiragana = document.toObject<Kana>()
                    hiraganaArrayList.add(hiragana)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting kanji from Firestore: ", exception)
            }

        // get all katakana from Firestore
        katakanaRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val katakana = document.toObject<Kana>()
                    katakanaArrayList.add(katakana)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting kanji from Firestore: ", exception)
            }

        // configure listeners for the guess Buttons
        for (row in guessLinearLayouts) {
            for (column in 0 until row!!.childCount) {
                val button = row.getChildAt(column) as Button
                button.setOnClickListener(guessButtonListener)
            }
        }

        return binding.root
    }

    // update guessRows based on value in SharedPreferences
    fun updateGuessRows(sharedPreferences: SharedPreferences) {

        // get the number of guess buttons that should be displayed
        val choices = sharedPreferences.getString(CHOICES, null)
        guessRows = choices!!.toInt() / 2

        // hide all guess button LinearLayouts
        for (layout in guessLinearLayouts) layout?.visibility = View.GONE

        // display appropriate guess button LinearLayouts
        for (row in 0 until guessRows) guessLinearLayouts[row]?.visibility = View.VISIBLE
    }

    // update signsSet based on value in SharedPreferences
    fun updateSigns(sharedPreferences: SharedPreferences) {
        signsSet = sharedPreferences.getString(SIGNS, null).toString()
    }

    // reset the Quiz
    fun resetQuiz() {
        correctAnswers = 0 // reset the number of correct answers made
        totalGuesses = 0 // reset the total number of guesses the user made
        quizListKanji.clear() // clear prior list of quiz kanji
        quizListHiragana.clear() // clear prior list of quiz hiragana
        quizListKatakana.clear() // clear prior list of quiz katakana
        quizListKanjiMeaning.clear() // clear prior list of quiz kanji meanings
        quizListHiraganaReading.clear() // clear prior list of quiz hiragana meanings
        quizListKatakanaReading.clear() // clear prior list of quiz katakana meanings

        // 2-second delay to update the sing quiz lists and load the next sign
        // required due to asynchronous Firebase API
        handler!!.postDelayed(
            {

                // switch depending on the signs set from SharedPreferences
                when(signsSet) {

                    "Hiragana" -> {
                        // add all hiragana readings to quizListHiraganaReading
                        for (hiragana in hiraganaArrayList) {
                            quizListHiraganaReading.add(hiragana.reading)
                        }

                        val numberOfAllHiragana = hiraganaArrayList.size
                        var counter = 1


                        // add signsInQuiz number of random signs to the quizListKanji
                        while (counter <= signsInQuiz) {

                            // get a random sign
                            val randomIndex = random!!.nextInt(numberOfAllHiragana)
                            val sign = hiraganaArrayList[randomIndex]

                            // add the sign to the list if it hasn't already been chosen
                            if (!quizListHiragana.contains(sign)) {
                                quizListHiragana.add(sign)
                                ++counter
                            }

                        }
                    }

                    "Katakana" -> {
                        // add all katakana readings to quizListKatakanaReading
                        for (katakana in katakanaArrayList) {
                            quizListKatakanaReading.add(katakana.reading)
                        }

                        val numberOfAllKatakana = katakanaArrayList.size
                        var counter = 1


                        // add signsInQuiz number of random signs to the quizListKanji
                        while (counter <= signsInQuiz) {

                            // get a random sign
                            val randomIndex = random!!.nextInt(numberOfAllKatakana)
                            val sign = katakanaArrayList[randomIndex]

                            // add the sign to the list if it hasn't already been chosen
                            if (!quizListKatakana.contains(sign)) {
                                quizListKatakana.add(sign)
                                ++counter
                            }
                        }
                    }

                    "Kanji" -> {
                        // add all kanji meanings to quizListKanjiMeaning
                        for (kanji in kanjiArrayList) {
                            quizListKanjiMeaning.add(kanji.meaning)
                        }

                        val numberOfAllKanji = kanjiArrayList.size
                        var counter = 1

                        // add signsInQuiz number of random signs to the quizListKanji
                        while (counter <= signsInQuiz) {

                            // get a random sign
                            val randomIndex = random!!.nextInt(numberOfAllKanji)
                            val sign = kanjiArrayList[randomIndex]

                            // add the sign to the list if it hasn't already been chosen
                            if (!quizListKanji.contains(sign)) {
                                quizListKanji.add(sign)
                                ++counter
                            }
                        }
                    }

                    else -> {
                        Log.w(TAG, "signs choice error")
                    }
                }

                loadNextSign()
            }, 2000
        )

    }

    // load the next sign after user chooses an answer
    private fun loadNextSign() {

        answerTextView!!.text = "" // clear answerTextView

        // display current question number
        questionNumberTextView.text = context?.getString(
            R.string.question,
            totalGuesses + 1,
            signsInQuiz
        )

        // switch depending on the signs set from SharedPreferences
        when(signsSet) {

            "Hiragana" -> {
                // get the next sign and remove it from the quiz list
                val nextHiragana: Kana = quizListHiragana.removeAt(0)
                correctAnswerKana = nextHiragana.reading // update the correct answer

                // display the next sign on the flip card
                cardFront?.text = nextHiragana.sign
                cardBack?.text = nextHiragana.reading

                // shuffle sign meanings
                quizListHiraganaReading.shuffle()

                // put the correct meaning at the end of quizListKanjiMeaning
                val correctMeaning = quizListHiraganaReading.indexOf(correctAnswerKana)
                quizListHiraganaReading.add(quizListHiraganaReading.removeAt(correctMeaning))

                // add 2, 4, 6 or 8 guess Buttons based on the value of guessRows
                for (row in 0 until guessRows) {

                    // place Buttons in currentTableRow
                    for (column in 0 until guessLinearLayouts[row]!!.childCount) {

                        // get reference to Button to configure
                        val newGuessButton = guessLinearLayouts[row]?.getChildAt(column) as Button
                        newGuessButton.isEnabled = true

                        // get sign meaning and set it as newGuessButton's text
                        val meaning = quizListHiraganaReading[row * 2 + column]
                        newGuessButton.text = meaning
                    }
                }

                // randomly replace one Button with the correct answer
                val row = random!!.nextInt(guessRows) // pick random row
                val column = random!!.nextInt(2) // pick random column
                val randomRow = guessLinearLayouts[row] // get the row
                (randomRow?.getChildAt(column) as Button).text = correctAnswerKana
            }

            "Katakana" -> {
                // get the next sign and remove it from the quiz list
                val nextKatakana: Kana = quizListKatakana.removeAt(0)
                correctAnswerKana = nextKatakana.reading // update the correct answer

                // display the next sign on the flip card
                cardFront?.text = nextKatakana.sign
                cardBack?.text = nextKatakana.reading

                // shuffle sign meanings
                quizListKatakanaReading.shuffle()

                // put the correct meaning at the end of quizListKanjiMeaning
                val correctMeaning = quizListKatakanaReading.indexOf(correctAnswerKana)
                quizListKatakanaReading.add(quizListKatakanaReading.removeAt(correctMeaning))

                // add 2, 4, 6 or 8 guess Buttons based on the value of guessRows
                for (row in 0 until guessRows) {

                    // place Buttons in currentTableRow
                    for (column in 0 until guessLinearLayouts[row]!!.childCount) {

                        // get reference to Button to configure
                        val newGuessButton = guessLinearLayouts[row]?.getChildAt(column) as Button
                        newGuessButton.isEnabled = true

                        // get sign meaning and set it as newGuessButton's text
                        val meaning = quizListKatakanaReading[row * 2 + column]
                        newGuessButton.text = meaning
                    }
                }

                // randomly replace one Button with the correct answer
                val row = random!!.nextInt(guessRows) // pick random row
                val column = random!!.nextInt(2) // pick random column
                val randomRow = guessLinearLayouts[row] // get the row
                (randomRow?.getChildAt(column) as Button).text = correctAnswerKana
            }

            "Kanji" -> {

                // get the next sign and remove it from the quiz list
                val nextKanji: Kanji = quizListKanji.removeAt(0)
                correctAnswerKanji = nextKanji.meaning // update the correct answer

                // display the next sign on the flip card
                cardFront?.text = nextKanji.sign
                cardBack?.text = nextKanji.meaning[0]

                // shuffle sign meanings
                quizListKanjiMeaning.shuffle()

                // put the correct meaning at the end of quizListKanjiMeaning
                val correctMeaning = quizListKanjiMeaning.indexOf(correctAnswerKanji)
                quizListKanjiMeaning.add(quizListKanjiMeaning.removeAt(correctMeaning))

                // add 2, 4, 6 or 8 guess Buttons based on the value of guessRows
                for (row in 0 until guessRows) {

                    // place Buttons in currentTableRow
                    for (column in 0 until guessLinearLayouts[row]!!.childCount) {

                        // get reference to Button to configure
                        val newGuessButton = guessLinearLayouts[row]?.getChildAt(column) as Button
                        newGuessButton.isEnabled = true

                        // get sign meaning and set it as newGuessButton's text
                        val meaning = quizListKanjiMeaning[row * 2 + column]
                        newGuessButton.text = meaning[0]
                    }
                }

                // randomly replace one Button with the correct answer
                val row = random!!.nextInt(guessRows) // pick random row
                val column = random!!.nextInt(2) // pick random column
                val randomRow = guessLinearLayouts[row] // get the row
                (randomRow?.getChildAt(column) as Button).text = correctAnswerKanji[0]
            }

            else -> Log.w(TAG, "signs choice error")
        }

    }

    // listener called when a guess Button is touched
    private val guessButtonListener =
        View.OnClickListener { v ->

            // assign the button value chosen by the user
            val guessButton = v as Button
            val guess = guessButton.text.toString()

            // assign correct answer
            // switch depending on the signs set from SharedPreferences
            when(signsSet) {
                "Hiragana" -> answer = correctAnswerKana
                "Katakana" -> answer = correctAnswerKana
                "Kanji" -> answer = correctAnswerKanji[0]
                else -> Log.w(TAG, "signs choice error")
            }

            flipCard(requireContext(), cardFront!!, cardBack!!) //flip the card
            ++totalGuesses // increment number of guesses the user has made

            // answer was correct
            if (guess == answer) {
                // increment the number of correct answers
                ++correctAnswers

                // display "Correct!" in green text
                answerTextView!!.setText(R.string.correct_answer)
                answerTextView!!.setTextColor(
                    resources.getColor(
                        R.color.correct_answer,
                        requireContext().theme
                    )
                )

                disableButtons() // disable all guess Buttons

                // if the user has answered signsInQuiz number of times - quiz over
                if (totalGuesses == signsInQuiz) {

                    // alertDialog to display quiz stats and reset button to start new quiz
                    val alertDialogBuilder = AlertDialog.Builder(activity)
                    alertDialogBuilder.setMessage(
                        getString(
                            R.string.results,
                            correctAnswers,
                            (correctAnswers.toDouble() / signsInQuiz) * 100
                        )
                    )
                        .setPositiveButton(R.string.reset_quiz
                        ) { _, _ -> resetQuiz() }
                        .setTitle(R.string.quiz_results)

                    val alertDialog : AlertDialog = alertDialogBuilder.create()
                    alertDialog.show()

                    // flip the card after a 2-second delay
                    handler!!.postDelayed(
                        {
                            flipCard(requireContext(), cardFront!!, cardBack!!)
                        }, 2000
                    )


                // quiz not over
                } else {

                    // flip the card and load the next sign
                    handler!!.postDelayed(
                        {
                            // flip the card after a 2-second delay
                            flipCard(requireContext(), cardFront!!, cardBack!!)

                            // load the next sign after a 1-second delay
                            handler!!.postDelayed(
                                {
                                    loadNextSign()
                                }, 1000
                            )

                        }, 2000
                    )


                }

            // answer was incorrect
            } else {

                // display "Incorrect!" in red text
                answerTextView!!.setText(R.string.incorrect_answer)
                answerTextView!!.setTextColor(
                    resources.getColor(
                        R.color.incorrect_answer, requireContext().theme
                    )
                )

                disableButtons() // disable all guess Buttons

                // if the user has answered signsInQuiz number of times - quiz over
                if (totalGuesses == signsInQuiz) {

                    // alertDialog to display quiz stats and reset button to start new quiz
                    val alertDialogBuilder = AlertDialog.Builder(activity)
                    alertDialogBuilder.setMessage(
                        getString(
                            R.string.results,
                            correctAnswers,
                            (correctAnswers.toDouble() / signsInQuiz) * 100
                        )
                    )
                        .setPositiveButton(R.string.reset_quiz
                        ) { _, _ -> resetQuiz() }
                        .setTitle(R.string.quiz_results)

                    val alertDialog : AlertDialog = alertDialogBuilder.create()
                    alertDialog.show()

                    // flip the card after a 2-second delay
                    handler!!.postDelayed(
                        {
                            flipCard(requireContext(), cardFront!!, cardBack!!)
                        }, 2000
                    )

                // quiz not over
                } else {

                    // flip the card and load the next sign
                    handler!!.postDelayed(
                        {
                            // flip the card after a 2-second delay
                            flipCard(requireContext(), cardFront!!, cardBack!!)

                            // load the next sign after a 1-second delay
                            handler!!.postDelayed(
                                {
                                    loadNextSign()
                                }, 1000
                            )

                        }, 2000
                    )
                }
            }
        }


    // function for flip card animation
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

    // utility method that disables all answer Buttons
    private fun disableButtons() {
        for (row in 0 until guessRows) {
            val guessRow = guessLinearLayouts[row]
            for (i in 0 until guessRow!!.childCount) guessRow.getChildAt(i).isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

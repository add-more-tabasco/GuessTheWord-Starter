package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel : ViewModel() {

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    // The current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // The current score
    private var _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish

    // Countdown time
    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime

    // Buzzers
    private val _buzz = MutableLiveData<BuzzType>()
    val buzz: LiveData<BuzzType>
        get() = _buzz

    // The String version of the current time
    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    // The Hint for the current word
    val wordHint = Transformations.map(word) { word ->
        val randomPosition = (1..word.length).random()
        "Current word has " + word.length + " letters" +
                "\nThe letter at position " + randomPosition + " is " +
                word.get(randomPosition - 1).toUpperCase()
    }

    // use LiveData to trigger event for buzzes
    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    private val timer: CountDownTimer

    // important times for game timer and buzzer
    companion object {
        // Time when the game is over
        private const val DONE = 0L
        // Countdown time interval
        private const val ONE_SECOND = 1000L
        // Total time for the game
        private const val COUNTDOWN_TIME = 60000L
        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 10L
    }

    //order matters! run init after initializing variables:
    init {

        _word.value = ""
        _score.value = 0

        resetList()
        nextWord()

        // Creates a timer which triggers the end of the game when it finishes
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished/ONE_SECOND
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _buzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }
            override fun onFinish() {
                _currentTime.value = DONE
                _buzz.value = BuzzType.GAME_OVER
                onGameFinish()
            }
        }
        timer.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        /** v1 - keep playing until words are all used */
//        if (wordList.isEmpty()) {
//            onGameFinish()
//        }
//        else {
//            //Select and remove a word from the list
//            _word.value = wordList.removeAt(0)
//        }

        //* v2 - keep playing until timer is up */
        // Shuffle the word list, if the list is empty
        if (wordList.isEmpty()) {
            resetList()
        } else {
            // Remove a word from the list
            _word.value = wordList.removeAt(0)
        }
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = (score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = (score.value)?.plus(1)
        _buzz.value = BuzzType.CORRECT
        nextWord()
    }

    // tell the viewModel when the buzz has completed
    fun onBuzzComplete() {
        _buzz.value = BuzzType.NO_BUZZ
    }

    /** Method for the game completed event **/
    fun onGameFinish() {
        _eventGameFinish.value = true
    }

    // reset the gamefinish value once its completed
    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

}
package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class GameViewModel: ViewModel() {
private val _uiState = MutableStateFlow(GameUiState())
//GameViewModel 내에서만 _uiState에 액세스하고 수정할 수 있다. UI는 읽기 전용 속성 uiState를 사용하여 값을 읽을 수 있다
// 이 변경 가능 상태 흐름을 읽기 전용 상태 흐름으로 만든다.
val uiState: StateFlow<GameUiState> =  _uiState.asStateFlow()
//글자가 섞인 현재 단어를 저장
private lateinit var currentWord: String
//사용된 단어를 저장하는 변경 가능 집합
private var usedWords: MutableSet<String> = mutableSetOf()
//사용자 추측값
var userGuess by mutableStateOf("")
        private set

private fun pickRandomWordAndShuffle(): String {
    // allWords에서 단어 랜덤으로 가져오기
    currentWord = allWords.random()

    if (usedWords.contains(currentWord)) {
        //useWords에 있으면 다시 랜덤돌리기
        return pickRandomWordAndShuffle()
    } else {
        usedWords.add(currentWord)
        //저장하고 단어 조합 셔플시키기
        return shuffleCurrentWord(currentWord)
    }
}

private fun shuffleCurrentWord(word: String): String {
    val tempWord = word.toCharArray()
    // Scramble the word, shuffle() : 랜덤하게 셔플시키는 함수.
    tempWord.shuffle()
    //혹시 랜덤하게 섞은 단어가 원래 단어와 같을 수도 있어서?
    while (String(tempWord).equals(word)) {
        tempWord.shuffle()
    }
    return String(tempWord)
}

    fun resetGame() {
        usedWords.clear()
        //초기화 후 새로운 단어 다시 선택
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }
    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }
    //답이 맞는지
    fun checkUserGuess() {

        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                //사용자의 추측이 틀렸으면 isGuessedWordWrong를 true로
                //copy() 함수를 사용하여 객체를 복사한다. 이렇게 하면 일부 속성만 변경하고 나머지는 그대로 유지할 수 있다.
                currentState.copy(isGuessedWordWrong = true)
            }

        }
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS){
            //Last round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else{
            // Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = currentState.currentWordCount.inc(),
                    score = updatedScore
                )
            }
        }
    }
    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }

    init {
        resetGame()
    }
}
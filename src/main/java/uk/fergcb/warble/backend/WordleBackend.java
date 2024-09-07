package uk.fergcb.warble.backend;

import uk.fergcb.warble.WordEvaluation;

public interface WordleBackend {
    void submitGuess(String word);
    WordEvaluation evaluateWord(int guess);
    boolean isFinished();
    void close();
}

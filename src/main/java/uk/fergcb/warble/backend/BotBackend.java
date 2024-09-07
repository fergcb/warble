package uk.fergcb.warble.backend;

import uk.fergcb.warble.WordEvaluation;
import uk.fergcb.warble.scoring.LetterEvaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BotBackend implements WordleBackend {

    private final String target;
    private final List<String> guesses;
    private WordEvaluation lastEvaluation = null;

    public BotBackend(String target) {
        this.target = target;
        this.guesses = new ArrayList<>();
    }

    @Override
    public void submitGuess(String word) {
        guesses.add(word);
    }

    @Override
    public WordEvaluation evaluateWord(int guessNum) {
        final var guess = guesses.get(guessNum);
        final var targetCounts = new HashMap<Character, Integer>();
        target.chars().forEach(c -> targetCounts.merge((char)c, 1, Integer::sum));

        final var seenCounts = new HashMap<Character, Integer>();

        final var letterEvals = new LetterEvaluation[guess.length()];

        for (int i = 0; i < guess.length(); i++) {
            final var gc = guess.charAt(i);
            if (gc == target.charAt(i)) {
                seenCounts.merge(gc, 1, Integer::sum);
                letterEvals[i] = LetterEvaluation.correct(gc, i);
            } else {
                letterEvals[i] = LetterEvaluation.absent(gc, i);
            }
        }

        for (int i = 0; i < guess.length(); i++) {
            final var gc = guess.charAt(i);
            if (gc != target.charAt(i)) {
                if (targetCounts.containsKey(gc) && seenCounts.getOrDefault(gc, 0) < targetCounts.get(gc))
                    letterEvals[i] = LetterEvaluation.present(gc, i);
                seenCounts.merge(gc, 1, Integer::sum);
            }
        }

        System.out.println(guess + " -> " + Arrays.stream(letterEvals).map(eval -> switch (eval.state()) {
            case CORRECT -> "\uD83D\uDFE9";
            case PRESENT -> "\uD83D\uDFE8";
            case ABSENT -> "⬛";
        }).collect(Collectors.joining()));

        return lastEvaluation = new WordEvaluation(List.of(letterEvals));
    }

    @Override
    public boolean isFinished() {
        return guesses.getLast().equals(target);
    }

    @Override
    public void close() {}
}

package uk.fergcb.warble.scoring;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class LetterFrequencyScoringStrategy implements ScoringStrategy {

    public String getName() {
        return "LetterFrequencyScoringStrategy";
    }

    public List<WordScore> scoreWords(Set<String> possibleWords) {
        // Count the frequencies of each letter in each position
        final var frequencies = new HashMap<CharPos, Integer>();
        for (var word : possibleWords) {
            for (int i = 0; i < word.length(); i++) {
                frequencies.merge(CharPos.of(word.charAt(i), i), 1, Integer::sum);
            }
        }

        return possibleWords.stream()
                .map(word -> new WordScore(
                        word,
                        IntStream.range(0, word.length())
                                .map(i -> frequencies.get(CharPos.of(word.charAt(i), i)))
                                .sum()))
                .sorted(Comparator.comparingInt(WordScore::score).reversed())
                .toList();
    }
}

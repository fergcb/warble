package uk.fergcb.warble.scoring;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NGramScoringStrategy implements ScoringStrategy {

    private final int n;
    private final Set<String> dictionary;

    public NGramScoringStrategy(int n) {
        this(n, null);
    }

    public NGramScoringStrategy(int n, Set<String> dictionary) {
        this.n = n;
        this.dictionary = dictionary;
    }

    public String getName() {
        return "NGramScoringStrategy(" + n + ")";
    }

    public List<WordScore> scoreWords(Set<String> possibleWords) {
        // Count the frequencies of each letter in each position
        final var frequencies = new HashMap<NGram, Integer>();
        for (var word : (dictionary == null ? possibleWords : dictionary)) {
            for (var ngram : ngrams(word)) {
                frequencies.merge(ngram, 1, Integer::sum);
            }
        }

//        printTopNGrams(frequencies);

        return possibleWords.stream()
                .map(word -> new WordScore(
                        word,
                        ngrams(word).stream().mapToInt(frequencies::get).sum()))
                .sorted(Comparator.comparingInt(WordScore::score).reversed())
                .toList();
    }

    public record NGram(String chars, int pos) {}

    private List<NGram> ngrams(String word) {
        return IntStream.range(-n+1, word.length())
                .mapToObj(i -> new NGram(word.substring(Math.max(0, i), Math.min(word.length(), i + n)), i))
                .toList();
    }

    public void printTopNGrams(Map<NGram, Integer> frequencies) {
        final var topForPos = new ArrayList<List<WordScore>>();
        for (int i = -n+1; i < 5; i++) {
            final var finali = i;
            topForPos.add(frequencies.entrySet().stream()
                    .filter(e -> e.getKey().pos() == finali)
                    .map(e -> {
                        final var chars = e.getKey().chars();
                        final var filler = "_".repeat(n - chars.length());
                        final var word = finali < n/2
                                ? filler + chars
                                : chars + filler;
                        return new WordScore(word, e.getValue());
                    })
                    .sorted(Comparator.comparingInt(WordScore::score).reversed())
                    .toList());
        }

        System.out.println("n = " + n);
        System.out.println(IntStream.range(-n+1, 5).mapToObj(i -> String.format("pos = %d", i)).collect(Collectors.joining("\t")));
        for (int i = 0; i < 10; i++) {
            final var finali = i;
            System.out.println(topForPos.stream()
                    .flatMap(ngrams -> ngrams.size() > finali ? Stream.of(ngrams.get(finali)) : Stream.of())
                    .map(ws -> String.format("%s (%d)", ws.word(), ws.score()))
                    .collect(Collectors.joining("\t")));
        }
    }
}

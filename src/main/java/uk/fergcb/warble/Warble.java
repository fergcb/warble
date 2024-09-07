package uk.fergcb.warble;

import uk.fergcb.warble.backend.BotBackend;
import uk.fergcb.warble.backend.WordleBackend;
import uk.fergcb.warble.scoring.NGramScoringStrategy;
import uk.fergcb.warble.scoring.ScoringStrategy;
import uk.fergcb.warble.scoring.WordScore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Warble {

    record TestCase(String desc, ScoringStrategy scorer, Set<String> words) {}

    public static void main(String[] args) {

        final var dictionary = getDictionary();

        final var minusPrev = new HashSet<>(dictionary);
        final var used = getUsedWords();
        minusPrev.removeAll(used);

//        computeFrequencyTables(possibleWords);

        final var cases = List.of(
                new TestCase("n=1, recompute frequencies, include prev", new NGramScoringStrategy(1), new HashSet<>(dictionary)),
                new TestCase("n=2, recompute frequencies, include prev", new NGramScoringStrategy(2), new HashSet<>(dictionary)),
                new TestCase("n=3, recompute frequencies, include prev", new NGramScoringStrategy(3), new HashSet<>(dictionary)),
                new TestCase("n=1, recompute frequencies, exclude prev", new NGramScoringStrategy(1), new HashSet<>(minusPrev)),
                new TestCase("n=2, recompute frequencies, exclude prev", new NGramScoringStrategy(2), new HashSet<>(minusPrev)),
                new TestCase("n=3, recompute frequencies, exclude prev", new NGramScoringStrategy(3), new HashSet<>(minusPrev)),
                new TestCase("n=1, do not recompute, include prev", new NGramScoringStrategy(1, dictionary), new HashSet<>(dictionary)),
                new TestCase("n=2, do not recompute, include prev", new NGramScoringStrategy(2, dictionary), new HashSet<>(dictionary)),
                new TestCase("n=3, do not recompute, include prev", new NGramScoringStrategy(3, dictionary), new HashSet<>(dictionary)),
                new TestCase("n=1, do not recompute, exclude prev", new NGramScoringStrategy(1, minusPrev), new HashSet<>(minusPrev)),
                new TestCase("n=2, do not recompute, exclude prev", new NGramScoringStrategy(2, minusPrev), new HashSet<>(minusPrev)),
                new TestCase("n=3, do not recompute, exclude prev", new NGramScoringStrategy(3, minusPrev), new HashSet<>(minusPrev))
        );

        playGame(
                new BotBackend("WIDEN"),
                new NGramScoringStrategy(2, minusPrev),
                new HashSet<>(minusPrev)
        );

        for (var testCase : cases) {
//            System.out.println("# " + testCase.desc());
//            System.out.println("Using " + testCase.scorer().getName());

//            playGame(
//                    new BotBackend("BATCH"),
//                    testCase.scorer(),
//                    new HashSet<>(testCase.words())
//            );

//            var totalGuesses = 0;
//            final var failures = new ArrayList<String>();
//            for (var word : minusPrev) {
//                final var guesses = playGame(
//                        new BotBackend(word),
//                        testCase.scorer(),
//                        new HashSet<>(testCase.words)
//                );
//                if (guesses > 6) failures.add(word);
//                totalGuesses += guesses;
//            }
//            final var avgGuesses = totalGuesses / (double)minusPrev.size();
//            final var failureRate = failures.size() / (double)minusPrev.size() * 100;
//
//            System.out.printf("%s\t%s\t%.2f\t%.2f%%\t%s\t%n", testCase.desc(), testCase.scorer().getName(), avgGuesses, failureRate, String.join(", ", failures));
        }
    }

    private static int playGame(WordleBackend wordle, ScoringStrategy scorer, Set<String> possibleWords) {
        var i = 0;
        try {
            while (!possibleWords.isEmpty()) {
                final var word =  scorer.scoreWords(possibleWords).getFirst().word();
//                System.out.println(word);
                wordle.submitGuess(word);
                final var eval = wordle.evaluateWord(i);
                i += 1;
                if (wordle.isFinished()) {  break; }
                eval.eliminateWords(possibleWords);
            }
        } finally {
            wordle.close();
        }

        return i;
    }

    private static void computeFrequencyTables(Set<String> possibleWords) {
//        final var previousWords = getPreviousWords(driver);
//        possibleWords.removeAll(previousWords);

        final var scores = new ArrayList<List<WordScore>>();

        for (int n = 1; n <= 3; n++) {
            final var scorer = new NGramScoringStrategy(n, possibleWords);
            scores.add(scorer.scoreWords(possibleWords));
        }

        System.out.println("n = 1\tn = 2\tn = 3");
        for (int i = 0; i < 10; i++) {
            final var finalI = i;
            System.out.println(scores.stream()
                    .map(words -> words.get(finalI))
                    .map(ws -> String.format("%s (%d)", ws.word(), ws.score()))
                    .collect(Collectors.joining("\t")));
        }
    }

    private static Set<String> getDictionary() {
        try (
                final var in = Warble.class.getClassLoader().getResourceAsStream("dictionary.txt");
                final var br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in)))
        ) {
            return br.lines().collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> getUsedWords() {
        try (
                final var in = Warble.class.getClassLoader().getResourceAsStream("used.txt");
                final var br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in)))
        ) {
            return br.lines().collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private static Set<String> getPreviousWords(WebDriver driver) {
//        driver.get("https://www.techradar.com/news/past-wordle-answers");
//
//        sleep(2500);
//
//        driver.switchTo().frame(3);
//        driver.findElement(By.cssSelector("button[title='Agree']")).click();
//        driver.switchTo().parentFrame();
//
//        final var p = driver.findElement(By.cssSelector("#section-past-wordle-answers-alphabetical-list + p"));
//
//        return Set.of(p.getText().split(" \\| "));
//    }
}

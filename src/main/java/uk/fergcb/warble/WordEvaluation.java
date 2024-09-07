package uk.fergcb.warble;

import uk.fergcb.warble.scoring.LetterEvaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record WordEvaluation(List<LetterEvaluation> letters) {
    public void eliminateWords(Set<String> possibleWords) {
        final var requiredCount = countChars();

//        System.out.println(requiredCount);

        possibleWords.removeIf(word -> {
            final var actualCount = countChars(word);

            for (var requiredLetter : requiredCount.keySet()) {
                if (requiredCount.get(requiredLetter) > actualCount.getOrDefault(requiredLetter, 0))
                    return true;
                if (requiredCount.get(requiredLetter) == 0 && actualCount.containsKey(requiredLetter))
                    return true;
            }

            return letters.stream()
                    .anyMatch(eval -> {
                        final var letter = word.charAt(eval.pos());
                        if (eval.state() == LetterEvaluation.State.CORRECT) {
//                            if (letter != eval.letter())
//                                System.out.println("Removing " + word + " because it doesn't have " + eval.letter() + " in slot " + eval.pos());
                            return letter != eval.letter();
                        }
                        else if (eval.state() == LetterEvaluation.State.PRESENT) {
//                            if (letter == eval.letter())
//                                System.out.println("Removing " + word + " because it has " + eval.letter() + " in slot " + eval.pos());
//                            if (!word.contains(eval.letter()+""))
//                                System.out.println("Removing " + word + " because it doesn't have " + eval.letter());
                            return letter == eval.letter() || !word.contains(eval.letter()+"");
                        }
//                        if (letter == eval.letter())
//                            System.out.println("Removing " + word + " because it has " + eval.letter() + " in slot " + eval.pos());
                        return letter == eval.letter();
                    });
        });
    }

    private Map<Character, Integer> countChars() {
        final var occurrences = new HashMap<Character, Integer>();
        letters.forEach(le -> {
            if (le.state() != LetterEvaluation.State.ABSENT)
                occurrences.merge(le.letter(), 1, Integer::sum);
            else
                occurrences.putIfAbsent(le.letter(), 0);
        });
        return occurrences;
    }

    private Map<Character, Integer> countChars(String word) {
        final var occurrences = new HashMap<Character, Integer>();
        word.chars().forEach(c -> occurrences.merge((char)c, 1, Integer::sum));
        return occurrences;
    }

    public boolean isCorrect() {
        return letters.stream().allMatch(eval -> eval.state() == LetterEvaluation.State.CORRECT);
    }
}

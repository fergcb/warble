package uk.fergcb.warble.scoring;

public record LetterEvaluation(char letter, int pos, State state) {
    public enum State { CORRECT, PRESENT, ABSENT }

    public static LetterEvaluation correct(char letter, int pos) {
        return new LetterEvaluation(letter, pos, State.CORRECT);
    }

    public static LetterEvaluation present(char letter, int pos) {
        return new LetterEvaluation(letter, pos, State.PRESENT);
    }

    public static LetterEvaluation absent(char letter, int pos) {
        return new LetterEvaluation(letter, pos, State.ABSENT);
    }
}

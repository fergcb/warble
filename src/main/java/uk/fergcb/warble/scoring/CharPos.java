package uk.fergcb.warble.scoring;

public record CharPos(char c, int i) {
    public static CharPos of(char c, int i) {
        return new CharPos(c, i);
    }
}

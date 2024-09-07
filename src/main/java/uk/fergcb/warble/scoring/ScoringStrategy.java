package uk.fergcb.warble.scoring;

import java.util.List;
import java.util.Set;

public interface ScoringStrategy {
    String getName();
    List<WordScore> scoreWords(Set<String> words);
}

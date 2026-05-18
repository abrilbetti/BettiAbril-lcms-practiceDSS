package scoredAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Algorithmic Stage 6 scorer that integrates partial rule-based scores.
 *
 * <p>This stage does not introduce new Drools domain rules. It reads the
 * evidence already stored in each {@link ScoredAnnotation} and computes a final
 * confidence score as a weighted average of the adduct-pattern score and the
 * retention-time score.</p>
 *
 * <p>The weights are dynamic:</p>
 * <ul>
 *     <li>adduct weight is the number of adduct evidence rules that contributed
 *     to the annotation</li>
 *     <li>RT weight is the number of distinct other features compared by the RT
 *     stage</li>
 * </ul>
 *
 * <p>If only one partial score has evidence, that score receives the full
 * weight. If neither partial score has evidence, the final score is
 * {@code -1.0} to indicate that final scoring was not applicable.</p>
 */
public class FinalScoringService {

    /**
     * Scores the provided annotations in place and returns them as a final
     * ranking.
     *
     * <p>The returned list is sorted by descending final score. The input
     * objects are the same mutable {@link ScoredAnnotation} instances that were
     * provided to the method.</p>
     *
     * @param scoredAnnotations annotations produced by Stage 5
     * @return annotations sorted from highest to lowest final score
     */
    public List<ScoredAnnotation> rankByFinalScore(Collection<ScoredAnnotation> scoredAnnotations) {
        scoreFinalInPlace(scoredAnnotations);
        List<ScoredAnnotation> ranking = new ArrayList<>(scoredAnnotations);
        ranking.sort(Comparator.comparingDouble(this::rankingScore).reversed());
        return ranking;
    }

    /**
     * Computes and stores the final score for each annotation.
     *
     * @param scoredAnnotations annotations produced by Stage 5
     */
    public void scoreFinalInPlace(Collection<ScoredAnnotation> scoredAnnotations) {
        for (ScoredAnnotation scoredAnnotation : scoredAnnotations) {
            scoredAnnotation.setScoreFinal(calculateFinalScore(scoredAnnotation));
        }
    }

    /**
     * Computes the weighted final score for one annotation.
     *
     * <p>The formula is
     * {@code (wAdduct * scoreAdduct + wRT * scoreRT) / (wAdduct + wRT)}.
     * Weights with no supporting evidence are ignored, so sentinel values such
     * as {@code -1.0} for "not applied" are not averaged into the final score.</p>
     *
     * @param scoredAnnotation annotation to score
     * @return weighted final score, or {@code -1.0} when no evidence is available
     */
    public double calculateFinalScore(ScoredAnnotation scoredAnnotation) {
        // @TODO does a dynamic formula that says the final score will be alpha times adduct score plus beta times RTscore
        // alpha and beta are the number of rules that have been applied to each
        // if rules were not applied, alpha or beta is 0 and the Adduct score and the RT score are -1
        double weightedScore = 0.0;
        int totalWeight = 0;

        if (hasUsableAdductScore(scoredAnnotation)) {
            int weight = scoredAnnotation.getAdductEvidenceCount();
            weightedScore += weight * scoredAnnotation.getScoreAdduct();
            totalWeight += weight;
        }

        if (hasUsableRtScore(scoredAnnotation)) {
            int weight = scoredAnnotation.getRtEvidenceFeatureCount();
            weightedScore += weight * scoredAnnotation.getScoreRT();
            totalWeight += weight;
        }

        // This is to avoid the situation of dividing by 0
        // Weight of 0 means lack of evidence and -1 also
        if (totalWeight == 0) {
            return -1.0;
        }
        return weightedScore / totalWeight;
    }

    private boolean hasUsableAdductScore(ScoredAnnotation scoredAnnotation) {
        return scoredAnnotation.getScoreAdduct() != null
                && scoredAnnotation.getScoreAdduct() >= 0.0
                && scoredAnnotation.getAdductEvidenceCount() > 0;
    }

    private boolean hasUsableRtScore(ScoredAnnotation scoredAnnotation) {
        return scoredAnnotation.getScoreRT() != null
                && scoredAnnotation.getScoreRT() >= 0.0
                && scoredAnnotation.getRtEvidenceFeatureCount() > 0;
    }

    private double rankingScore(ScoredAnnotation scoredAnnotation) {
        if (scoredAnnotation.getScoreFinal() == null) {
            return Double.NEGATIVE_INFINITY;
        }
        return scoredAnnotation.getScoreFinal();
    }
}
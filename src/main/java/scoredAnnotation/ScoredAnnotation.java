package scoredAnnotation;

import annotation.Annotation;
import feature.LabelledAdductFeature;
import peak.LabelledPeak;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Stage 4 fact that enriches a putative compound annotation with rule-based
 * confidence scores.
 *
 * <p>A {@link Annotation} represents the result of Stage 3: one
 * candidate compound for one labelled feature. Stage 4 does not replace that
 * annotation. Instead, it wraps it in this mutable object so Drools rules can
 * attach evidence scores to it.</p>
 *
 * <p>The three score fields correspond to the later stages of the practice:</p>
 * <ul>
 *     <li>{@code scoreAdduct}: compatibility with expected adduct ratios,
 *     filled in Stage 4.</li>
 *     <li>{@code scoreRT}: retention-time/logP evidence, reserved for Stage 5.</li>
 *     <li>{@code scoreFinal}: integrated final confidence, reserved for Stage 6.</li>
 * </ul>
 */
public class ScoredAnnotation {

    private final Annotation annotation;
    private Double scoreAdduct;
    private Double scoreRT;
    private Double scoreFinal;
    private int adductEvidenceCount;
    private int positiveRtEvidenceCount;
    private int negativeRtEvidenceCount;
    private final Set<String> adductEvidenceKeys = new HashSet<>();
    private final Set<ScoredAnnotation> rtComparedAnnotations =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<LabelledAdductFeature> rtComparedFeatures =
            Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Creates an unscored annotation wrapper.
     *
     * <p>The score fields intentionally start as {@code null}. This lets the
     * rules distinguish "not evaluated yet" from a real score such as
     * {@code 0.0}.</p>
     *
     * @param annotation putative compound annotation generated in Stage 3
     * @throws NullPointerException if annotation is null
     */
    public ScoredAnnotation(Annotation annotation) {
        this.annotation = Objects.requireNonNull(annotation, "annotation");

    }

    /**
     * Returns the original putative annotation that is being scored.
     *
     * @return wrapped compound annotation
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Returns the adduct-pattern score assigned by the Stage 4 Drools rules.
     *
     * @return score between 0 and 1 when evidence was evaluated, {@code -1.0}
     *         when no adduct evidence was collected, or {@code null} before the
     *         rule unit has processed this fact
     */
    public Double getScoreAdduct() {
        return scoreAdduct;
    }

    /**
     * Updates the adduct-pattern score.
     *
     * <p>This setter is used by Drools consequences in the DRL file.</p>
     *
     * @param scoreAdduct new adduct-pattern score
     */
    public void setScoreAdduct(Double scoreAdduct) {
        this.scoreAdduct = scoreAdduct;
    }

    /**
     * Records the score produced by one adduct-pattern rule.
     *
     * <p>Stage 6 uses the number of applied adduct rules as the dynamic weight
     * for the adduct score. Fallback rules that mean "no adduct evidence" should
     * call {@link #setScoreAdduct(Double)} directly instead of this method.</p>
     *
     * @param scoreAdduct adduct-pattern score produced by an evidence rule
     */
    public void addAdductEvidenceScore(Double scoreAdduct) {
        this.scoreAdduct = scoreAdduct;
        adductEvidenceCount++;
    }

    /**
     * Checks whether a named adduct evidence rule has already contributed.
     *
     * @param evidenceKey stable name for one adduct evidence rule
     * @return true when this evidence was already counted
     */
    public boolean hasCollectedAdductEvidence(String evidenceKey) {
        return adductEvidenceKeys.contains(evidenceKey);
    }

    /**
     * Records a named adduct evidence score once.
     *
     * <p>When several relative-intensity rules fire for the same annotation, the
     * stored adduct score is the running average of their individual scores.</p>
     *
     * @param evidenceKey stable name for one adduct evidence rule
     * @param evidenceScore score produced by that rule
     * @return true when this call added new evidence
     */
    public boolean addAdductEvidenceScore(String evidenceKey, Double evidenceScore) {
        if (!adductEvidenceKeys.add(evidenceKey)) {
            return false;
        }
        if (scoreAdduct == null) {
            scoreAdduct = evidenceScore;
        } else {
            scoreAdduct = (scoreAdduct * adductEvidenceCount + evidenceScore) / (adductEvidenceCount + 1);
        }
        adductEvidenceCount++;
        return true;
    }

    /**
     * Returns how many adduct-pattern evidence rules contributed to this
     * annotation.
     *
     * @return number of applied adduct evidence rules
     */
    public int getAdductEvidenceCount() {
        return adductEvidenceCount;
    }

    /**
     * Returns the retention-time score assigned by the Stage 5 Drools rules.
     *
     * <p>The RT stage compares annotations from different features. A compound
     * with a higher logP is expected to elute later than a compound with a lower
     * logP. Every concordant pair contributes one positive evidence count and
     * every contradictory pair contributes one negative evidence count.</p>
     *
     * <p>The score is computed as
     * {@code positiveEvidence / (positiveEvidence + negativeEvidence)}. A
     * compound with nine concordant comparisons and one contradictory comparison
     * therefore receives {@code 0.9}.</p>
     *
     * @return score between {@code 0.0} and {@code 1.0}, {@code 0.0} when no
     *         comparable RT evidence was found, or {@code null} before Stage 5
     *         has run
     */
    public Double getScoreRT() {
        return scoreRT;
    }

    /**
     * Updates the retention-time score.
     *
     * <p>This setter is used by Drools consequences in the RT scoring DRL
     * file.</p>
     *
     * @param scoreRT new retention-time score
     */
    public void setScoreRT(Double scoreRT) {
        this.scoreRT = scoreRT;
    }

    /**
     * Returns the final integrated score reserved for Stage 6.
     *
     * @return final score, or {@code null} while Stage 6 has not run
     */
    public Double getScoreFinal() {
        return scoreFinal;
    }

    /**
     * Updates the final integrated score reserved for Stage 6.
     *
     * @param scoreFinal new final score
     */
    public void setScoreFinal(Double scoreFinal) {
        this.scoreFinal = scoreFinal;
    }

    /**
     * Checks whether the annotated compound name contains a given text.
     *
     * <p>The match is case-insensitive and intentionally uses {@code contains}
     * so a rule can match values such as "L-Alanine" or "alanine standard" with
     * the same condition.</p>
     *
     * @param compoundName text that should appear in the compound name
     * @return true when the wrapped compound name contains the given text
     */
    public boolean hasCompoundName(String compoundName) {
        return annotation.getCompound().getName() != null
                && annotation.getCompound().getName().toLowerCase(Locale.ROOT)
                .contains(compoundName.toLowerCase(Locale.ROOT));
    }

    /**
     * Checks whether the feature contains at least one labelled peak for an adduct.
     *
     * @param adductName adduct pattern, for example {@code [M+H]+}
     * @return true when the summed intensity for this adduct is greater than zero
     */
    public boolean hasAdduct(String adductName) {
        return getObservedAdductIntensity(adductName) > 0;
    }

    /**
     * Calculates the observed intensity ratio between two adducts.
     *
     * <p>For example, the alanine rule calls this method as
     * {@code getAdductIntensityRatio("[M+H]+", "[M+Na]+")} to compare the
     * observed M+H/M+Na signal ratio against the expected ratio.</p>
     *
     * @param numeratorAdduct adduct used in the numerator
     * @param denominatorAdduct adduct used in the denominator
     * @return numerator intensity divided by denominator intensity, or
     *         {@link Double#NaN} when the denominator adduct is absent
     */
    public double getAdductIntensityRatio(String numeratorAdduct, String denominatorAdduct) {
        // if we have observed a ratio, we need to compare it with the one that is in the pattern
        // if the expected is 1 and the obtained is 0.7, so if the num and denom adducts, goes to the observed and calculates the intensity ratio
        double denominatorIntensity = getObservedAdductIntensity(denominatorAdduct);
        if (denominatorIntensity == 0.0) {
            return Double.NaN;
        }
        return getObservedAdductIntensity(numeratorAdduct) / denominatorIntensity;
    }

    /**
     * Calculates an adduct's relative intensity inside the labelled feature.
     *
     * <p>The most intense observed adduct receives {@code 1.0}. Every other
     * adduct receives {@code adductIntensity / maxAdductIntensity}.</p>
     *
     * @param adductName adduct pattern to search for
     * @return relative intensity between {@code 0.0} and {@code 1.0}
     */
    public double getRelativeAdductIntensity(String adductName) {
        double maxIntensity = getMaxObservedAdductIntensity();
        if (maxIntensity == 0.0) {
            return 0.0;
        }
        return getObservedAdductIntensity(adductName) / maxIntensity;
    }

    /**
     * Finds the largest observed adduct intensity in the labelled feature.
     *
     * @return maximum summed adduct intensity, or {@code 0.0} when no adducts exist
     */
    public double getMaxObservedAdductIntensity() {
        double maxIntensity = 0.0;
        for (LabelledPeak labelledPeak : annotation.getAdductLabelledFeature().getLabelledPeaks()) {
            maxIntensity = Math.max(maxIntensity, getObservedAdductIntensity(labelledPeak.getAdduct().getAdductName()));
        }
        return maxIntensity;
    }

    /**
     * Sums the observed intensities of all labelled peaks with a given adduct.
     *
     * <p>Most examples contain one peak per adduct, but summing makes the method
     * robust if several peaks receive the same adduct label.</p>
     *
     * @param adductName adduct pattern to search for
     * @return total observed intensity for that adduct, or {@code 0.0} if absent
     */
    public double getObservedAdductIntensity(String adductName) {
        double intensity = 0.0;
        for (LabelledPeak labelledPeak : annotation.getAdductLabelledFeature().getLabelledPeaks()) {
            if (labelledPeak.getAdduct().getAdductName().equals(adductName)) {
                intensity += labelledPeak.getPeak().getIntensity();
            }
        }
        return intensity;
    }

    /**
     * Returns the representative retention time of the annotated feature.
     *
     * <p>Labelled features contain one or more labelled peaks. The RT scoring
     * rules use the average RT of those peaks as the feature RT but it does not calculate it since the feature
     * already contains a representative RT value.</p>
     *
     * @return average retention time of the labelled peaks in minutes
     */
    public double getRepresentativeRt() {
        double rt = 0.0;
        for (LabelledPeak labelledPeak : annotation.getAdductLabelledFeature().getLabelledPeaks()) {
            rt += labelledPeak.getPeak().getRt();
        }
        return rt / annotation.getAdductLabelledFeature().getLabelledPeakCount();
    }

    /**
     * Checks whether this annotation belongs to a different feature than another
     * scored annotation.
     *
     * @param other annotation to compare with this one
     * @return true when both annotations wrap different feature objects
     */
    public boolean isFromDifferentFeatureThan(ScoredAnnotation other) {
        return annotation.getAdductLabelledFeature() != other.annotation.getAdductLabelledFeature();
    }

    /**
     * Checks whether this annotation's compound has a higher logP value than
     * another annotation's compound.
     *
     * @param other annotation to compare with this one
     * @return true when this compound's logP is greater than the other's logP
     */
    public boolean hasHigherLogPThan(ScoredAnnotation other) {
        return annotation.getCompound().getLogp() > other.annotation.getCompound().getLogp();
    }

    /**
     * Checks whether this annotation's feature elutes later than another
     * annotation's feature.
     *
     * @param other annotation to compare with this one
     * @return true when this feature has a greater representative RT
     */
    public boolean hasHigherRtThan(ScoredAnnotation other) {
        return getRepresentativeRt() > other.getRepresentativeRt();
    }

    /**
     * Checks whether this annotation's feature elutes earlier than another
     * annotation's feature.
     *
     * @param other annotation to compare with this one
     * @return true when this feature has a lower representative RT
     */
    public boolean hasLowerRtThan(ScoredAnnotation other) {
        return getRepresentativeRt() < other.getRepresentativeRt();
    }

    /**
     * Records one positive RT comparison against another annotation.
     *
     * <p>The method ignores comparisons against the same feature and ignores a
     * pair that has already been counted for this annotation. Drools rules call
     * it from their consequences so repeated activations do not inflate the
     * evidence counts.</p>
     *
     * @param other annotation that was compared with this annotation
     * @return true when this call added a new positive evidence count
     */
    public boolean addPositiveRtEvidenceFrom(ScoredAnnotation other) {
        if (!canAddRtEvidenceFrom(other)) {
            return false;
        }
        positiveRtEvidenceCount++;
        updateScoreRTFromEvidenceCounts();
        return true;
    }

    /**
     * Records one negative RT comparison against another annotation.
     *
     * <p>The method ignores comparisons against the same feature and ignores a
     * pair that has already been counted for this annotation. Drools rules call
     * it from their consequences so repeated activations do not inflate the
     * evidence counts.</p>
     *
     * @param other annotation that was compared with this annotation
     * @return true when this call added a new negative evidence count
     */
    public boolean addNegativeRtEvidenceFrom(ScoredAnnotation other) {
        if (!canAddRtEvidenceFrom(other)) {
            return false;
        }
        negativeRtEvidenceCount++;
        updateScoreRTFromEvidenceCounts();
        return true;
    }

    /**
     * Checks whether this annotation has already collected RT evidence.
     *
     * @return true when at least one positive or negative RT comparison was
     *         counted for this annotation
     */
    public boolean hasRtEvidence() {
        return positiveRtEvidenceCount + negativeRtEvidenceCount > 0;
    }

    /**
     * Returns the number of distinct features used as RT evidence for this
     * annotation.
     *
     * <p>This count is intentionally feature-based, not rule-based. If several
     * annotations belong to the same other feature, they can affect the RT score
     * as annotation-level comparisons, but they contribute only one unit of RT
     * weight in the final aggregation.</p>
     *
     * @return number of distinct other features compared by the RT stage
     */
    public int getRtEvidenceFeatureCount() {
        return rtComparedFeatures.size();
    }

    private boolean canAddRtEvidenceFrom(ScoredAnnotation other) {
        if (!isFromDifferentFeatureThan(other) || !rtComparedAnnotations.add(other)) {
            return false;
        }
        rtComparedFeatures.add(other.annotation.getAdductLabelledFeature());
        return true;
    }

    private void updateScoreRTFromEvidenceCounts() {
        int totalEvidenceCount = positiveRtEvidenceCount + negativeRtEvidenceCount;
        if (totalEvidenceCount == 0) {
            scoreRT = 0.0;
            return;
        }
        scoreRT = (double) positiveRtEvidenceCount / totalEvidenceCount;
    }

    @Override
    public String toString() {
        return "ScoredAnnotation{" +
                "annotation=" + annotation +
                ", scoreAdduct=" + scoreAdduct +
                ", adductEvidenceCount=" + adductEvidenceCount +
                ", scoreRT=" + scoreRT +
                ", positiveRtEvidenceCount=" + positiveRtEvidenceCount +
                ", negativeRtEvidenceCount=" + negativeRtEvidenceCount +
                ", rtEvidenceFeatureCount=" + getRtEvidenceFeatureCount() +
                ", scoreFinal=" + scoreFinal +
                '}';
    }
}
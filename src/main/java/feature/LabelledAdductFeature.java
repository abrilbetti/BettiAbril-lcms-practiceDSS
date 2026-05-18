package feature;

import adduct.IonizationMode;
import peak.LabelledPeak;

import java.util.List;
import java.util.Objects;

public class LabelledAdductFeature {
    private final Feature feature;
   // private final Adduct adduct;
  //  private final double experimentalMonoisotopicMass;
    private final List<LabelledPeak> labelledPeaks;
    private final IonizationMode ionizationMode;

    /**
     * Constructor for the class Labelled Adduct Feature that receives the 3 variables as parameters and uses them to
     * initialise its attributes.
     * @param feature Feature object.
     * @param labelledPeaks list that contains LabelledPeak objects.
     * @param ionizationMode object of IonizationMode.
     */
    public LabelledAdductFeature(Feature feature, List<LabelledPeak> labelledPeaks, IonizationMode ionizationMode) {
        this.feature = feature;
        this.labelledPeaks = labelledPeaks;
        this.ionizationMode = ionizationMode;
    }

    /**
     * Getter method for the Feature object.
     * @return the Feature object.
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * Getter method for the list of LabelledPeak objects.
     * @return the list of objects.
     */
    public List<LabelledPeak> getLabelledPeaks() {
        return labelledPeaks;
    }

    /**
     * Returns the number of labelled peaks in this feature.
     *
     * @return the count of labelled peaks
     */
    public int getLabelledPeakCount() {
        return labelledPeaks.size();
    }

    /**
     * Getter method for the IonizationMode object.
     * @return the IonizationMode object.
     */
    public IonizationMode getIonizationMode() {
        return ionizationMode;
    }

    /**
     * Calculates the consensus neutral mass by averaging all labelled peak neutral masses.
     *
     * @return the average neutral mass of all labelled peaks
     */
    public double getConsensusNeutralMass() {
        if (labelledPeaks.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (LabelledPeak labelledPeak : labelledPeaks) {
            sum += labelledPeak.getNeutralMass();
        }
        return sum / labelledPeaks.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelledAdductFeature)) {
            return false;
        }
        LabelledAdductFeature other = (LabelledAdductFeature) o;
        return Objects.equals(feature, other.feature) && Objects.equals(labelledPeaks, other.labelledPeaks) && ionizationMode == other.ionizationMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature, labelledPeaks, ionizationMode);
    }

    @Override
    public String toString() {
        return "LabelledAdductFeature{" +
                "feature=" + feature +
                ", labelledPeaks=" + labelledPeaks +
                ", ionizationMode=" + ionizationMode +
                '}';
    }
}

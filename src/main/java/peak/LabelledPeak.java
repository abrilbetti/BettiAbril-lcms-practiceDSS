package peak;

import adduct.Adduct;

import java.util.Objects;

/**
 * This class represents the relationship between Peak and AdductLabelledFeature.
 */
public class LabelledPeak {
    private final Peak peak;
    private final Adduct adduct;
    private final double neutralMass;

    /**
     * Constructor of LabelledPeak class that initialises both the peak and the adduct objects.
     * @param peak object of the Peak class.
     * @param adduct object of the Adduct class.
     * @param neutralMass variable of double type that represents the neutral mass computed.
     */
    public LabelledPeak(Peak peak, Adduct adduct, double neutralMass) {
        this.peak = peak;
        this.adduct = adduct;
        this.neutralMass = neutralMass;
    }

    /**
     * Getter method for the peak attribute.
     * @return the Peak object.
     */
    public Peak getPeak() {
        return peak;
    }

    /**
     * Getter method for the adduct attribute.
     * @return the Adduct object.
     */
    public Adduct getAdduct() {
        return adduct;
    }

    /**
     * Getter method for the neutral mass attribute.
     * @return the neutral mass.
     */
    public double getNeutralMass() {
        return neutralMass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelledPeak)) {
            return false;
        }
        LabelledPeak other = (LabelledPeak) o;
        return Objects.equals(peak, other.peak) && adduct == other.adduct;
    }

    @Override
    public int hashCode() {
        return Objects.hash(peak, adduct);
    }

    @Override
    public String toString() {
        return "LabelledPeak{" +
                "peak=" + peak +
                ", adduct=" + adduct +
                ", neutralMass=" + neutralMass +
                '}';
    }
}

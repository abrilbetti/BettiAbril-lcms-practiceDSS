package annotation;

import compound.Compound;
import feature.LabelledAdductFeature;

import java.util.Objects;

/**
 * Class that represents the annotations over a lipid, which are the results we want to obtain.
 * This class has 1 attribute: an object of the class feature.
 */
public class Annotation {

    private final LabelledAdductFeature labelledAdductFeature;
    private final Compound compound;
    private final double massDifference;
    private final double ppmDifference;

    /**
     * Public constructor for the Annotation class.
     * @param labelledAdductFeature object of the LabelledAdductFeature class
     * @param compound object of the Compound class
     * @param massDifference double variable that represents the mass difference
     * @param ppmDifference double variable that represents the ppm difference
     */
    public Annotation(LabelledAdductFeature labelledAdductFeature, Compound compound, double massDifference, double ppmDifference) {
        this.labelledAdductFeature = labelledAdductFeature;
        this.compound = compound;
        this.massDifference = massDifference;
        this.ppmDifference = ppmDifference;
    }

    public LabelledAdductFeature getAdductLabelledFeature() {
        return labelledAdductFeature;
    }

    public Compound getCompound() {
        return compound;
    }

    public double getMassDifference() {
        return massDifference;
    }

    public double getPpmDifference() {return ppmDifference;}

    @Override
    public String toString() {
        return "Annotation{" +
                "adductLabelledFeature=" + labelledAdductFeature +
                ", compound=" + compound +
                ", massDifference=" + massDifference +
                ", ppmDifference=" + ppmDifference +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) {
            return false;
        }
        Annotation other = (Annotation) o;
        return Double.compare(massDifference, other.massDifference) == 0 && Double.compare(ppmDifference, other.ppmDifference) == 0 && Objects.equals(labelledAdductFeature, other.labelledAdductFeature) && Objects.equals(compound, other.compound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labelledAdductFeature, compound, massDifference, ppmDifference);
    }
}

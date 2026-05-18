package feature;

import adduct.IonizationMode;
import peak.Peak;

import java.util.Collection;

/**
 * This class represents the features and has 2 attributes: RT, a collection of peaks and the monoisotopic mass.
 */
public class Feature {
    double rt;
    Collection<Peak> peakCollection;
    private final IonizationMode ionizationMode;
   // double monoisotopicMass; WE DELETED THIS ATTRIBUTE FROM THIS CLASS

    /**
     * Empty constructor of the feature class.
     */
    public Feature(IonizationMode ionizationMode){
        super();
        this.ionizationMode = ionizationMode;
    }

    /**
     * Constructor that initialises all the attributes of the class feature.
     * @param rt value for the RT attribute
     * @param peakCollection collection to be initialised as attribute
     */
    public Feature(double rt, Collection<Peak> peakCollection, IonizationMode ionizationMode) {
        super();
        this.rt = rt;
        this.peakCollection = peakCollection;
        this.ionizationMode = ionizationMode;
    }

    /**
     * Getter for the RT attribute.
     * @return the RT attribute.
     */
    public double getRt() {
        return rt;
    }

    /**
     * Getter for the collection of peaks.
     * @return the collection of peaks.
     */
    public Collection<Peak> getPeakCollection() {
        return peakCollection;
    }

    /**
     * Setter for the RT attribute.
     * @param rt value used to initialise the RT attribute.
     */
    public void setRt(float rt) {
        this.rt = rt;
    }

    /**
     * Setter for the collection of peaks.
     * @param peakCollection collection used to set the attribute.
     */
    public void setPeakCollection(Collection<Peak> peakCollection) {
        this.peakCollection = peakCollection;
    }

    /**
     * Getter for the ionization mode of the Feature objects
     * @return the ionization mode of the feature.
     */
    public IonizationMode getIonizationMode() {
        return ionizationMode;
    }

    // TODO GENERATE HASH CODE, EQUALS AND TO STRING

    @Override
    public String toString() {
        return "Feature{" +
                "rt=" + rt +
                ", peakCollection=" + peakCollection +
                '}';
    }
}

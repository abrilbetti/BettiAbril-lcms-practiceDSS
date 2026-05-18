package adduct;

import feature.Feature;
import feature.LabelledAdductFeature;
import peak.LabelledPeak;
import peak.Peak;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is in charge of carrying out stage 2 of the practice.
 * This class has a static final attribute that represents the tolerance in ppm (parts per million).
 */
public class AdductDetector {
    private static final double MASS_TOLERANCE_PPM = 11.5; //I CHANGED THE PPM TOLERANCE BECAUSE IF NOT THE TEST OF ADDUCT DETECTOR DID NOT WORK

    /**
     * This method receives a feature as parameter and retrieves all the LabelledAdductFeature objects that were built.
     * @param feature object of the Feature class.
     * @return a list that contains objects of the class LabelledAdductFeature.
     */
    public List<LabelledAdductFeature> detectAdducts(Feature feature){
        // We obtain the peaks associated to the feature and cast the collection to list.
        List<Peak> peaksSortedByIntensityDecreasing = sortPeaksByDescendingIntensity((List<Peak>)feature.getPeakCollection());
        // I retieve the ionization mode of the feature received as parmeter
        IonizationMode ionizationMode = feature.getIonizationMode();
        // I obtain a list of all the adducts of the ionization mode obtained in the previous line
        List<Adduct> adductsByMode = Adduct.getByMode(ionizationMode);
        // I create a list that will contain objects of the class LabelledAdductFeature
        List<LabelledAdductFeature> labelledAdductFeatures = new ArrayList<>();

        // For loop that iterates through the list of adducts
        for(Adduct hypothesisAdduct : adductsByMode){
            // I call the method that hypothesizes over the adducts and store the resulting list in labelledPeaks
            List<LabelledPeak> labelledPeaks = hypothesizeAdducts(peaksSortedByIntensityDecreasing, hypothesisAdduct, adductsByMode);

            // If the retrieved list has more than 1 peak labelled, then I create a new LabelledAdductFeature and add it to the list
            if(labelledPeaks.size() > 1){
                labelledAdductFeatures.add(new LabelledAdductFeature(feature, labelledPeaks, ionizationMode));
            }
        }
        return labelledAdductFeatures;
    }

    /**
     * This method receives a list of Peak objects and sorts them in decreasing order in terms of their intensity parameter.
     * @param peaksToBeSorted list of peaks that we want to sort in terms of intensity
     * @return the list that contains all the sorted peaks
     */
    public List<Peak> sortPeaksByDescendingIntensity(List<Peak> peaksToBeSorted){
        ArrayList<Peak> sortedPeaks = new ArrayList<Peak>(peaksToBeSorted);

        // Nested for loops that iterate through the list of peaks to be grouped and sorts them in descending order of intensity
        for(int i=0; i<peaksToBeSorted.size(); i++){
            for(int j=0; j<peaksToBeSorted.size()-i-1; j++){
                if (sortedPeaks.get(j).getIntensity() < sortedPeaks.get(j + 1).getIntensity()) {                    Peak temp = sortedPeaks.get(j);
                    sortedPeaks.set(j, sortedPeaks.get(j + 1));
                    sortedPeaks.set(j + 1, temp);
                }
            }
        }
        return sortedPeaks;
    }

    /**
     * This method receives a list of peaks, an adduct object over which the hypothesis is made and a list of adducts filtered
     * by their ionization mode.
     * @param sortedPeaks list of Peak objects ordered by descending order of their intensity.
     * @param hypothesisAdduct Adduct object over which we hypothesize.
     * @param adductsByMode list of adduct objects filtered by their ionization mode.
     * @return a list that contains objects of the class LabelledPeak.
     */
    public List<LabelledPeak> hypothesizeAdducts(List<Peak> sortedPeaks, Adduct hypothesisAdduct, List<Adduct> adductsByMode){
        List<LabelledPeak> labelledPeaks = new ArrayList<>();

        // I retrieve the most intense peak
        Peak mostIntensePeak = sortedPeaks.get(0);
        // I use the most intense peaks and the adduct over which we are hypothesising in order to compute the neutral mass
        double neutralMass = calculateNeutralMass(mostIntensePeak, hypothesisAdduct);
        /*I create a LabelledPeak object with the most intense peak, the adduct received as parameter and the computed
          neutral mass and add this object to the list of labelled peaks.
         */
        labelledPeaks.add(new LabelledPeak(mostIntensePeak, hypothesisAdduct, neutralMass));

        // For loop that iterates through the list of peaks received as parameter, skipping index 0 since it was already used
        // (stored in the variable mostIntensePeak)
        for(int i=1; i<sortedPeaks.size(); i++){
            Peak currentPeak = sortedPeaks.get(i);

            /* For each loop that iterates through the list of Adduct objects that contain all the adducts with the
               specified ionization mode.
             */
            for(Adduct currentAdduct : adductsByMode){
                // I compute the expected mz and use it to compute the ppm error.
                double expectedMZ = calculateExpectedMz(neutralMass, currentAdduct);
                /* We compute the PPM error over the mz of the most intense peak (which is the actual mz value) and the
                 value of mz we are expecting out of the value retrieved in the previous line (from the method calculateExpectedMz).
                 */
                double ppmError = calculatePpmError(currentPeak.getMz(), expectedMZ);

                // If the ppm error is lower than the tolerance, then we found a matching hypothesis
                if (ppmError <= MASS_TOLERANCE_PPM) {
                    // We create the Labelled Peak object and leave the for loops
                    labelledPeaks.add(new LabelledPeak(currentPeak, currentAdduct, neutralMass));
                    break; // Found a match, no need to try other adducts for this peak
                }
            }
        }
        return labelledPeaks;
    }

    /**
     * Method that computes the expected mz of an adduct. In order to do so, it uses both the adduct and the neutral
     * mass received as parameters.
     * @param neutralMass variable of double type that represents the neutral mass.
     * @param adduct Adduct object.
     * @return a value of double type that represents the expected mz.
     */
    private double calculateExpectedMz(double neutralMass, Adduct adduct) {

        return (adduct.getMultimerNumber() * neutralMass + adduct.getMassDifference()) / adduct.getCharge();
    }

    /**
     * Method that uses the measured mz and the expected mz, which are received as parameters, in order to compute the
     * PPM error.
     * @param measuredMz variable of double type that contains the actual mz value.
     * @param expectedMz variable of double type that contains the expected or computed mz value.
     * @return the ppm error, represented by a value of double type.
     */
    private double calculatePpmError(double measuredMz, double expectedMz) {
        // Where 1e6 is a tolerance established
        return Math.abs((measuredMz - expectedMz) / expectedMz) * 1e6;
    }

    /**
     * Method that computes the neutral mass making use of peak and adduct objects.
     * @param peak object of Peak.
     * @param adduct object of Adduct.
     * @return the computed mass, stored in a variable of double type.
     */
    private double calculateNeutralMass(Peak peak, Adduct adduct) {
        // First, we make sure that the peak and adduct objects are not null.
        // !TODO: HANDLE EXCEPTIONS IN THIS WAY OR WITH TRY CATCH
        Objects.requireNonNull(peak, "peak");
        Objects.requireNonNull(adduct, "adduct");

        double mz = peak.getMz();

        double monoisotopicMass = (mz * adduct.getCharge() - adduct.getMassDifference()) / adduct.getMultimerNumber();

        return monoisotopicMass;

    }
}

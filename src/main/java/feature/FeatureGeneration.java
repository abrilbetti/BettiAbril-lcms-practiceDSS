package feature;

import adduct.IonizationMode;
import peak.Peak;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class is the one in charge of grouping the peaks depending on their RTs and the stated tolerance.
 * The class is provided with a collection or set of peaks and, depending on the RT value of each peak, they are grouped with their "equals".
 */
public class FeatureGeneration {

    //I declare the tolerance as a variable of double type and final since I will not modify this value
    private final double tolerance = 0.3;
    private static final double epsilon = 1e-6;

    /**
     * Method that is in charge of grouping the peaks it receives as parameter in terms of their RT.
     * @param peaks list that contains the Peak objects that we want to group.
     * @return a list of lists that contains all the grouped peaks.
     */
    public List<List<Peak>> groupPeaksByRTWindow(List<Peak> peaks){
        List<Peak> peaksToBeGrouped = sortPeaksByIncreasingRT(peaks);
        List<List<Peak>> listOfGroupedPeaks = new ArrayList<>(); // In this list of lists I will store all the lists of the grouped peaks.

        // I create a while loop that will run as long as there are peaks to be grouped into features
        while(!peaksToBeGrouped.isEmpty()){
            Peak leaderPeak = peaksToBeGrouped.get(0); // I manually obtain the first peak and create a group with it.
            peaksToBeGrouped.remove(leaderPeak); // I remove the first peak from the list of peaks to be grouped.

            // I create a list where I will store all the peaks that share the range of RT with this first peak.
            List<Peak> currentGroup = new ArrayList<>();
            currentGroup.add(leaderPeak); // I add the first peak to the current group.
            double lastRt = leaderPeak.getRt(); // Variable to help keep track of the PREVIOUS peak

            for (int i = 0; i < peaksToBeGrouped.size(); i++) {
                //I obtain the peak of interest and create a Peak object where I store it
                Peak currentPeak = peaksToBeGrouped.get(i);

                /*If the RT of the peak I am trying to group is within the range that, depending on the tolerance, epsilon
                and on the RT of the lastly grouped peak, characterises peaks of a same feature, we group them together.
                 */
                if (currentPeak.getRt() - lastRt <= tolerance + epsilon) {
                    currentGroup.add(currentPeak);
                } else {
                    listOfGroupedPeaks.add(new ArrayList<>(currentGroup));
                    currentGroup.clear(); // @TODO SHOULD I REMOVE THIS LINE?
                    currentGroup.add(currentPeak); // I add the peak to its belonging group
                }
                lastRt = currentPeak.getRt(); // Update the reference RT
            }
            listOfGroupedPeaks.add(currentGroup);
            return listOfGroupedPeaks;
        }
        // Up to now, I built several lists that contain peaks that should be grouped into one.
        // I should create another function that creates the features from the list of peaks it receives as parameters.
        return listOfGroupedPeaks;
    }

    /**
     * Sorts a list of peaks in increasing order of their Retention Time (RT).
     * @param peaks the list of peaks to be sorted.
     * @return a new list containing the sorted peaks.
     */
    private List<Peak> sortPeaksByIncreasingRT(List<Peak> peaks) {
        List<Peak> sortedPeaks = new ArrayList<>(peaks);
        // Sorts from lowest RT to highest RT
        sortedPeaks.sort(Comparator.comparingDouble(Peak::getRt));
        return sortedPeaks;
    }

    /**
     * Method that receives an array list as parameter that contains all the peaks that belong to the same
     * group and that, therefore, should be used in order to create a feature. This method makes use of the
     * method that computes the average RT and creates a Feature object with the obtained average RT.
     * @param groupedPeaks array list that contains all the peaks with RT according to the established tolerance
     * @return the object of the feature class that was created using the grouped peaks
     */
    public Feature generateFeatureFromGrouping(List<Peak> groupedPeaks, IonizationMode mode){
        // I call the method in charge of computing the average value for the group of peaks
        double avgRT = computeFeatureAverageRT(groupedPeaks);
        return new Feature(avgRT, groupedPeaks, mode);
    }

    /**
     * Method that receives a list of Peak objects as parameter and computes the average value for the RT which will be used
     * to create the new feature. This method is declared as private since it is only used internally within this class.
     * @param groupedPeaks list that contains grouped Peak objects
     * @return a double variable where the average RT is stored
     */
    private double computeFeatureAverageRT(List<Peak> groupedPeaks){
        double sumOfRTs = 0; // This variable will store the sum of all the RTs of the Peak objects within the list received as parameter
        // For loop that iterates the whole list of peaks
        for(int i=0; i<groupedPeaks.size(); i++){
            // We add the RT of the current element of the list to the sum of RTs
            sumOfRTs = sumOfRTs + groupedPeaks.get(i).getRt();
        }
        // We declare a double variable that represents the average of the RTs
        double averageRT = sumOfRTs/groupedPeaks.size();
        return averageRT;
    }
}

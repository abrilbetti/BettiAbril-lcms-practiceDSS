package lipid;

import adduct.IonizationMode;
import feature.Feature;
import feature.FeatureGeneration;
import org.junit.Test;
import peak.Peak;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;


public class FeatureGenerationTest {

    /**
     * This method reads the CSV file where the data of the peaks is included and creates a list that contains
     * the Peak objects that were created out of reading the CSV file.
     * @param fileName name of the CSV file
     * @return the list of peaks
     */
    private List<Peak> loadPeaksFromCSV(String fileName) {
        // I create a list where I will store the peak objects I create from reading the CSV file
        List<Peak> peaks = new ArrayList<>();

        // Use getResourceAsStream to read from src/test/resources
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName))))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // We are skipping the first line since it contains the headings
                    isFirstLine = false;
                    continue;
                }
                // I declare how the text is split
                String[] values = line.split(";");

                // We store the values present in the columns of the read row in the corresponding attributes
                double mz = Double.parseDouble(values[1]);
                double rt = Double.parseDouble(values[3]);

                // Create the Peak object and add it to the list
                Peak peak = new Peak();
                peak.setMz(mz);
                peak.setRt(rt);
                peaks.add(peak);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return peaks;
    }

    @Test
    public void featureGenerationTestAmountOfGroups(){
        // Create an instance of the class we want to test
        FeatureGeneration featureGeneration = new FeatureGeneration();
        // Call the method that loads the data from the CSV and creates the list of Peak objects
        List<Peak> peaks = loadPeaksFromCSV("peakData.csv");
        /* In the line above I am only introducing the name of the file instead of the whole data path
        because I am making use of the "getResourceAsStream" in the method in charge of reading from the file.
         */

        // Call the method we want to test
        List<List<Peak>> groupedPeaks = featureGeneration.groupPeaksByRTWindow(peaks);

        // Now, I check if the amount of groups that were created is the one that I am expecting
        assertEquals("The number of groups is incorrect", 12, groupedPeaks.size());
    }

    @Test
    public void generateFeatureFromGroupingTest(){
        // Create an instance of the class we want to test
        FeatureGeneration featureGeneration = new FeatureGeneration();
        // Call the method that loads the data from the CSV and creates the list of Peak objects
        List<Peak> peaks = loadPeaksFromCSV("peakData.csv");
        /* In the line above I am only introducing the name of the file instead of the whole data path
        because I am making use of the "getResourceAsStream" in the method in charge of reading from the file.
         */

        // Call the method we want to test
        List<List<Peak>> groupedPeaks = featureGeneration.groupPeaksByRTWindow(peaks);
        List<Feature> listOfFeatures = new ArrayList<>();

        for(int i=0; i<groupedPeaks.size(); i++){
            Feature feature = featureGeneration.generateFeatureFromGrouping(groupedPeaks.get(i), IonizationMode.POSITIVE);
            System.out.println(feature);
            listOfFeatures.add(feature);
        }
        assertEquals("The number of features generated is incorrect", 12, listOfFeatures.size());
    }
}

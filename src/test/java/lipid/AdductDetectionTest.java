package lipid;

import adduct.Adduct;
import adduct.AdductDetector;
import adduct.IonizationMode;
import feature.Feature;
import feature.FeatureGeneration;
import feature.LabelledAdductFeature;
import org.junit.Before;
import org.junit.Test;
import peak.LabelledPeak;
import peak.Peak;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdductDetectionTest {
    // !!TODO For the adduct detection both regular algorithms or drools can be used as far the tests are passed.
    private AdductDetector adductDetector;
    private List<Peak> peaks;
    private List<Peak> sortedPeaks;
    private List<Adduct> adductsByIonizationMode;

    @Before
    public void setup() {
        adductDetector = new AdductDetector();
        // I create a list where I will store the peak objects I create from reading the CSV file
        peaks = new ArrayList<>();
        sortedPeaks = new ArrayList<>();
        adductsByIonizationMode = new ArrayList<>();
    }

    /**
     * This method reads the CSV file where the data of the peaks is included and creates a list that contains
     * the Peak objects that were created out of reading the CSV file.
     * @param fileName name of the CSV file
     * @return the list of peaks
     */
    private List<Peak> loadPeaksFromCSV(String fileName) {
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
                double intensity = Double.parseDouble(values[2]);
                double rt = Double.parseDouble(values[3]);

                // Create the Peak object and add it to the list
                Peak peak = new Peak();
                peak.setMz(mz);
                peak.setIntensity(intensity);
                peak.setRt(rt);
                peaks.add(peak);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return peaks;
    }
/*
    @Test
    public void shouldDetectAdductBasedOnMzDifference() {

        // Given two peaks with ~21.98 Da difference (e.g., [M+H]+ and [M+Na]+)
        Peak mH = new Peak(700.500, 100000.0); // [M+H]+
        Peak mNa = new Peak(722.482, 80000.0);  // [M+Na]+
        Lipid lipid = new Lipid(1, "PC 34:1", "C42H82NO8P", "PC", 34, 1);

        double annotationMZ = 700.49999d;
        double annotationIntensity = 80000.0;
        double annotationRT = 6.5d;
        Annotation annotation = new Annotation(lipid, annotationMZ, annotationIntensity, annotationRT, Set.of(mH, mNa));


        // Then we should call the algorithmic/knowledge system rules fired to detect the adduct and Set it!
        //
        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }


    @Test
    public void shouldDetectLossOfWaterAdduct() {
        Peak mh = new Peak(700.500, 90000.0);        // [M+H]+
        Peak mhH2O = new Peak(682.4894, 70000.0);     // [M+H–H₂O]+, ~18.0106 Da less

        Lipid lipid = new Lipid(1, "PE 36:2", "C41H78NO8P", "PE", 36, 2);
        Annotation annotation = new Annotation(lipid, mh.getMz(), mh.getIntensity(), 7.5d, Set.of(mh, mhH2O));



        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());

        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectDoublyChargedAdduct() {
        // Assume real M = (700.500 - 1.0073) = 699.4927
        // So [M+2H]2+ = (M + 2.0146) / 2 = 350.7536
        Peak singlyCharged = new Peak(700.500, 100000.0);  // [M+H]+
        Peak doublyCharged = new Peak(350.754, 85000.0);   // [M+2H]2+

        Lipid lipid = new Lipid(3, "TG 54:3", "C57H104O6", "TG", 54, 3);
        Annotation annotation = new Annotation(lipid, singlyCharged.getMz(), singlyCharged.getIntensity(), 10d, Set.of(singlyCharged, doublyCharged));

        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());

        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }
*/
    @Test
    public void sortPeaksByDescendingIntensityTest(){
        peaks = loadPeaksFromCSV("peakData.csv");
        sortedPeaks = adductDetector.sortPeaksByDescendingIntensity(peaks);
        Peak mostIntensePeak = new Peak(411.507,600000,9.05);
        Peak middlePeak = new Peak(328.190,	140000,	6.80);
        Peak leastIntensePeak = new Peak(512.789,45000,7.40);

        assertEquals(mostIntensePeak, sortedPeaks.get(0));
        assertEquals(middlePeak, sortedPeaks.get(14));
        assertEquals(leastIntensePeak, sortedPeaks.get(29));
    }

    @Test
    public void hypothesizeAdducts(){
        peaks = loadPeaksFromCSV("peakData.csv");

        // First, I sort the peaks in descending intensity
        sortedPeaks = adductDetector.sortPeaksByDescendingIntensity(peaks);
        adductsByIonizationMode = Adduct.getByMode(IonizationMode.POSITIVE);
        Adduct hypothesisAdduct = adductsByIonizationMode.get(0); //Reteieved adduct: M_PLUS_H

        List<LabelledPeak> labelledPeaks = adductDetector.hypothesizeAdducts(sortedPeaks, hypothesisAdduct, adductsByIonizationMode);
        // The first peak of the list of labelled peaks should be the most intense one. Therefore, I compare the mz of the most intense
        // peak and that of the peak of the first object stored in the list of LabelledPeaks.
        // I declared a tolerance equal to 0.001 in order to avoid the test from failing due to tiny rounding errors.
        assertEquals("The first peak should be the most intense", 411.507, labelledPeaks.get(0).getPeak().getMz(), 0.001);
        assertEquals("The first adduct should be M+H", Adduct.M_PLUS_H, labelledPeaks.get(0).getAdduct());
    }

    @Test
    public void detectAdductsTest(){
        // I read the peaks and store them in a list called peaks
        peaks = loadPeaksFromCSV("peakData.csv");

        //I create an object of the FeatureGeneration class in order to use its methods
        FeatureGeneration featureGeneration = new FeatureGeneration();
        List<List<Peak>> groupedPeaks = featureGeneration.groupPeaksByRTWindow(peaks);
        List<Feature> generatedFeatures = new ArrayList<>();
        List<LabelledAdductFeature> labelledAdductFeatures = new ArrayList<>();

        for(List<Peak> currentPeaks : groupedPeaks){
            // I call the respective method in order to create the feature
            Feature feature = featureGeneration.generateFeatureFromGrouping(currentPeaks, IonizationMode.POSITIVE);
            generatedFeatures.add(feature);
        }
        /*
        According to how I programmed my deconvolution phase, the features that are generated out of the "Unknown" adduct
        are those corresponding to indexes 1, 4, 7 and 9. This is due to the fact of the position they occupy when the list
        of peaks is sorted in terms of the RT in the method "groupPeaksByRTWindow" inside the "FeatureGeneration" class.
        Also, no adducts will be detected for these features since they have only 1 associated peak, which is a condition in
        the method "detectAdducts()" that prevents adducts to be hypothesized over features with 1 peak.
        Therefore, I will check if the features with more than one peak have the correct adduct hypothesis associated
        and whether the final indexes of the feature list have no adducts detected.
         */
        assertDetectedAdducts(adductDetector, generatedFeatures.get(0), Set.of("[M+H]+", "[M+Na]+", "[M+2H]2+"), 0);
        assertDetectedAdducts(adductDetector, generatedFeatures.get(2), Set.of("[M+H]+", "[M+Na]+", "[M+NH4]+", "[2M+H]+"), 2);
        assertDetectedAdducts(adductDetector, generatedFeatures.get(3), Set.of("[M+H]+", "[M+Na]+", "[M+NH4]+"), 3);
        assertDetectedAdducts(adductDetector, generatedFeatures.get(5), Set.of("[M+H]+", "[M+Na]+", "[M+2H]2+"), 5);
        assertDetectedAdducts(adductDetector, generatedFeatures.get(6), Set.of("[M+H]+", "[M+Na]+", "[2M+H]+"), 6);
        assertDetectedAdducts(adductDetector, generatedFeatures.get(8), Set.of("[M+H]+", "[M+Na]+", "[M+NH4]+"), 8);
        assertDetectedAdducts(adductDetector, generatedFeatures.get(10), Set.of("[M+H]+", "[M+Na]+", "[M+NH4]+", "[M+2H]2+"), 10);
        assertDetectedAdducts(adductDetector, generatedFeatures.get(11), Set.of("[M+H]+", "[M+Na]+", "[2M+H]+"), 11);

        assertEquals(0, adductDetector.detectAdducts(generatedFeatures.get(1)).size(), 1);
        assertEquals(0, adductDetector.detectAdducts(generatedFeatures.get(4)).size(), 4);
        assertEquals(0, adductDetector.detectAdducts(generatedFeatures.get(7)).size(), 7);
        assertEquals(0, adductDetector.detectAdducts(generatedFeatures.get(9)).size(), 9);
    }

    /**
     * This method receives an AdductDetector object, a Feature obect and a set that contains strings (the adduct names that
     * should be obtained). Then, the method calls the detectAdducts method and checks if the retrieved adduct names coincide
     * with those contained in the set received as parameter.
     * @param detector object of AdductDetector
     * @param feature object of Feature
     * @param expectedAdductNames set that contains the names of the adducts that should be obtained (strings)
     */
    private void assertDetectedAdducts(AdductDetector detector, Feature feature, Set<String> expectedAdductNames, int featureNum) {
        List<LabelledAdductFeature> detectedLabelledAdductFeatures = detector.detectAdducts(feature);
        assertTrue(detectedLabelledAdductFeatures.size() > 0);

        Set<String> bestDetectedAdductNames = detectedLabelledAdductFeatures.stream()
                .map(this::adductNames)
                .filter(names -> names.containsAll(expectedAdductNames)) // We check if all the adduct names we are expecting are contained
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected adduct labels not detected: " + expectedAdductNames));

        assertEquals(expectedAdductNames, bestDetectedAdductNames);
        // I print the number of the features and all the labelled adduct features associated to each of them
        System.out.printf("\nFeature %d: ", featureNum+1);
        for(int i=0; i<detectedLabelledAdductFeatures.size(); i++){
            System.out.printf("\n%d- ", i+1);
            System.out.println(detectedLabelledAdductFeatures.get(i));
        }
    }

    /**
     * This method retrieves the name of the adducts out of a feature it receives as parameter.
     * @param feature object of Feature
     * @return a set of strings, where the names of the adducts are contained
     */
    private Set<String> adductNames(LabelledAdductFeature feature) {
        return feature.getLabelledPeaks().stream()
                .map(labelledPeak -> labelledPeak.getAdduct().getAdductName())
                .collect(Collectors.toSet());
    }

}

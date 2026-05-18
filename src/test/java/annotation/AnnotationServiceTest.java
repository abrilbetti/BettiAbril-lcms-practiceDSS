package annotation;

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

import static org.junit.Assert.*;

public class AnnotationServiceTest {
    private AdductDetector adductDetector;
    private List<Peak> peaks;

    @Before
    public void setup() {
        adductDetector = new AdductDetector();
        // I create a list where I will store the peak objects I create from reading the CSV file
        peaks = new ArrayList<>();
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

    @Test
    public void shouldAnnotateFeatureWithInventedLabelledAdductFeature() {
        AnnotationService annotationService = new AnnotationService(false);

        // I call the method that generates the labelled adduct features and store the retrieved list in labelledAdductFeatures
        List<LabelledAdductFeature> labelledAdductFeatures = generateLabelledAdductFeature();
        List<Annotation> results = new ArrayList<>();

        double aproxDopamineMass = 153.0789;

        // I create a Peak that looks like a Dopamine [M+H]+ adduct
        Peak dopaminePeak = new Peak();
        dopaminePeak.setMz(154.086255); // m/z = 153.078979 + 1.007276 (mass of a proton) = 154.086255
        dopaminePeak.setIntensity(100000.0); // this intensity value represents a mid-range value for a clear and identifiable peak.
        dopaminePeak.setRt(2.0);

        // Here we tell the object that its neutral mass is the one determined above, which is very close to that of the Dopamine compound
        LabelledPeak lp = new LabelledPeak(dopaminePeak, Adduct.M_PLUS_H, aproxDopamineMass);

        Feature f = new Feature(2.0, List.of(dopaminePeak), IonizationMode.POSITIVE);

        LabelledAdductFeature inventedLabelledAdductFeature = new LabelledAdductFeature(f, List.of(lp), IonizationMode.POSITIVE);
        results.addAll(annotationService.annotate(inventedLabelledAdductFeature));;

        assertEquals(1, results.size());
        assertEquals("Dopamine", results.get(0).getCompound().getName());

        if(!results.isEmpty()) {
            printAnnotations(results);
        }
    }

    @Test
    public void shouldAnnotateFeatureWithSeveralInventedLabelledAdductFeatures() {
        AnnotationService annotationService = new AnnotationService(false);
        List<LabelledAdductFeature> inventedLabelledAdductFeatures = new ArrayList<>();

        // LabelledAdductFeature that matches with Dopamine
        double dopamineMass = 153.078979;
        Peak p1 = new Peak();
        p1.setMz(154.086255); // 153.078979 + 1.007276
        p1.setIntensity(100000.0);
        p1.setRt(2.0);
        LabelledPeak lp1 = new LabelledPeak(p1, Adduct.M_PLUS_H, dopamineMass);
        inventedLabelledAdductFeatures.add(new LabelledAdductFeature(new Feature(2.0, List.of(p1), IonizationMode.POSITIVE), List.of(lp1), IonizationMode.POSITIVE));

        // LabelledAdductFeature that matches with Serotonine
        double serotoninMass = 176.0950;
        Peak p2 = new Peak();
        p2.setMz(177.102276); // 176.0950 + 1.007276
        p2.setIntensity(150000.0);
        p2.setRt(3.5);
        LabelledPeak lp2 = new LabelledPeak(p2, Adduct.M_PLUS_H, serotoninMass);
        inventedLabelledAdductFeatures.add(new LabelledAdductFeature(new Feature(3.5, List.of(p2), IonizationMode.POSITIVE), List.of(lp2), IonizationMode.POSITIVE));

        // LabelledAdductFeature that matches with GABA
        double gabaMass = 103.0633;
        Peak p3 = new Peak();
        p3.setMz(104.070576); // 103.0633 + 1.007276
        p3.setIntensity(80000.0);
        p3.setRt(1.2);
        LabelledPeak lp3 = new LabelledPeak(p3, Adduct.M_PLUS_H, gabaMass);
        inventedLabelledAdductFeatures.add(new LabelledAdductFeature(new Feature(1.2, List.of(p3), IonizationMode.POSITIVE), List.of(lp3), IonizationMode.POSITIVE));

        // LabelledAdductFeature that matches with Acetylcholine
        // Note: Acetylcholine is already a positive ion (C7H16NO2+), so m/z = mass
        double achMass = 146.1176;
        Peak p4 = new Peak();
        p4.setMz(146.1176);
        p4.setIntensity(200000.0);
        p4.setRt(0.8);
        LabelledPeak lp4 = new LabelledPeak(p4, Adduct.M_PLUS_H, achMass); // Using M+H as placeholder
        inventedLabelledAdductFeatures.add(new LabelledAdductFeature(new Feature(0.8, List.of(p4), IonizationMode.POSITIVE), List.of(lp4), IonizationMode.POSITIVE));

        List<Annotation> results = new ArrayList<>();

        for (LabelledAdductFeature inventedFeature : inventedLabelledAdductFeatures) {
            results.addAll(annotationService.annotate(inventedFeature));
        }
        for(int i=0; i<results.size(); i++) {
            double retrievedMass = results.get(i).getCompound().getMonoisotopicMass();
             if(retrievedMass == 153.078979) {
                 assertEquals("Dopamine", results.get(0).getCompound().getName());
             }else if(retrievedMass == 176.0950) {
                 assertEquals("Serotonin", results.get(1).getCompound().getName());
             }else if(retrievedMass == 103.0633) {
                 assertEquals("GABA", results.get(2).getCompound().getName());
             }else if(retrievedMass == 146.1176) {
                 assertEquals("Acetylcholine", results.get(3).getCompound().getName());
             }else{
                 assertFalse("No match found for mass: " + retrievedMass, results.contains(results.get(0)));
                 // TODO THIS DOES NOT MAKE SENSE BUT IDK WHAT TO PUT
            }
        }
        if(!results.isEmpty()) {
            printAnnotations(results);
        }
    }

    @Test
    public void shouldAnnotateFeatureFromDatabase() {
        AnnotationService annotationService = new AnnotationService(false);

        // I call the method that generates the labelled adduct features and store the retrieved list in labelledAdductFeatures
        List<LabelledAdductFeature> labelledAdductFeatures = generateLabelledAdductFeature();
        List<Annotation> results = new ArrayList<>();
        // For loop that runs the list of LabelledAdductFeature objects and tries the annotate method
        for(LabelledAdductFeature labelledAdductFeature : labelledAdductFeatures) {
            results.addAll(annotationService.annotate(labelledAdductFeature));;
        }
        assertEquals(0, results.size());
        if(!results.isEmpty()) {
            printAnnotations(results);
        }
    }

    /**
     * This method generates the labelled adduct feature objects out of reading the csv file of the peaks, and applying the code
     * corresponding to both stages 1 and 2. This is done because these objects are needed in order to test the methods related
     * to stage 3 of the practice.
     * @return a list that contains all the generated labelled adduct feature objects
     */
    private List<LabelledAdductFeature> generateLabelledAdductFeature(){
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
            // I call the method I want to test and store the results in a list of LabelledAdductFeature objects
            labelledAdductFeatures.addAll(adductDetector.detectAdducts(feature));
        }
        return labelledAdductFeatures;
    }

    /**
     * This internal method is in charge of printing the Annotation objects that were generated in the tests.
     * @param annotations list of Annotation objects that contains all the annotations that were made and we want to output.
     */
    private void printAnnotations(List<Annotation> annotations) {
        for(Annotation annotation : annotations) {
            System.out.println(annotation);
            System.out.printf("\n");
        }
    }
}
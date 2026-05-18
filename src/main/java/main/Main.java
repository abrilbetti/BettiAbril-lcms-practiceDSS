package main;

import adduct.IonizationMode;
import adduct.AdductDetector;
import annotation.Annotation;
import annotation.AnnotationService;
import feature.Feature;
import feature.FeatureGeneration;
import feature.LabelledAdductFeature;
import inputOutput.Input;
import inputOutput.Output;
import scoredAnnotation.AdductPatternScoringService;
import scoredAnnotation.FinalScoringService;
import scoredAnnotation.RetentionTimeScoringService;
import scoredAnnotation.ScoredAnnotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import peak.Peak;


public class Main {

    private static final String INPUT_FILEPATH = "peakData.csv";
    private static Input input = new Input();
    private static Output output = new Output();

    public static void main(String[] args) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        //STAGE 1: read the raw peak table from the CSV
        // First I call the method that reads the CSV file.
        List<Peak> peaks = input.loadPeaksFromCSV(INPUT_FILEPATH);
        output.outputStartOfStage(1);
        output.outputPeaks(peaks);

        // Stage 1 - Step 2: create chromatographic features by grouping nearby peaks in retention time.
        FeatureGeneration featureGeneration = new FeatureGeneration();
        // I call the method that groups the peaks depending on their RT.
        List<List<Peak>> groupedPeaks = featureGeneration.groupPeaksByRTWindow(peaks);
        List<Feature> listOfFeatures = new ArrayList<>();

        for(int i=0; i<groupedPeaks.size(); i++){
            Feature feature = featureGeneration.generateFeatureFromGrouping(groupedPeaks.get(i), IonizationMode.POSITIVE);
            listOfFeatures.add(feature);
        }
        output.outputFeatures(listOfFeatures);
        output.endOfStage();

        // STAGE 2:
        // Create adduct-labelled features by testing adduct hypothesis inside each feature
        output.outputStartOfStage(2);
        AdductDetector adductDetector = new AdductDetector();
        List<LabelledAdductFeature> detectedLabelledAdductFeatures = new ArrayList<>();
        // I run through the list of features that where created in stage 2
        for(Feature feature : listOfFeatures) {
            detectedLabelledAdductFeatures.addAll(adductDetector.detectAdducts(feature));
        }
        output.outputLabelledAdductFeatures(detectedLabelledAdductFeatures);
        output.endOfStage();

        // STAGE 3:
        // Create compound annotations by matching each labelled feature neutral mass against the database.
        output.outputStartOfStage(3);

        // I pass a true since we are working with the complete database of compounds.
        AnnotationService annotationService = new AnnotationService(true);
        List<Annotation> annotations = new ArrayList<>();

        // For each loop that iterates through the list of labelled adduct features and calls the annotate method.
        for(LabelledAdductFeature labAdductFeature : detectedLabelledAdductFeatures) {
            annotations.addAll(annotationService.annotate(labAdductFeature));
        }
        output.outputAnnotations(annotations);
        output.endOfStage();

        // STAGE 4
        output.outputStartOfStage(4);
        // First, we create the scored annotation objects
        List<ScoredAnnotation> scoredAnnotations = new ArrayList<>();
        for (Annotation annotation : annotations) {
            scoredAnnotations.add(new ScoredAnnotation(annotation));
        }

        // Afterward, we run the adduct-pattern rules over the already created Scored Annotation objects.
        AdductPatternScoringService adductPatternScoringService = new AdductPatternScoringService();
        // I use the created object to call the method that internally instantiates and fires the rules.
        adductPatternScoringService.scoreAdductPatternsInPlace(scoredAnnotations);

        output.outputScoredAnnotations("Annotations scored in terms of their adduct patterns.", scoredAnnotations);
        output.endOfStage();

        // STAGE 5
        output.outputStartOfStage(5);
        RetentionTimeScoringService rtScoringService = new RetentionTimeScoringService();
        rtScoringService.scoreRetentionTimesInPlace( scoredAnnotations);

        output.outputScoredAnnotations("Annotations scored in terms of their RTs.", scoredAnnotations);
        output.endOfStage();

        // STAGE 6
        output.outputStartOfStage(6);
        List<ScoredAnnotation> finalRanking = new FinalScoringService().rankByFinalScore(scoredAnnotations);
        output.outputScoredAnnotations("Final ranked annotations.", finalRanking);
        output.endOfStage();
    }
}

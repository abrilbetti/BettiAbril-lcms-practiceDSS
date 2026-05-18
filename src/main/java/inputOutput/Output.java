package inputOutput;

import annotation.Annotation;
import feature.Feature;
import feature.LabelledAdductFeature;
import peak.Peak;
import scoredAnnotation.ScoredAnnotation;

import java.util.List;

/**
 * This class is in charge of handling the outputs of the main.
 * It contains several methods that output the desired information.
 */
public class Output {

    public void outputStartOfStage(int stageNum){
        System.out.println("\n=================================================================================================================================================");
        System.out.printf("\n STAGE %d:", stageNum);
    }

    public void endOfStage(){
        System.out.println("\n=================================================================================================================================================");
    }

    public void outputPeaks(List<Peak> peaks){
        System.out.printf("-Read %d raw peaks from the CSV file.\n", peaks.size());
        for (Peak peak : peaks) {
            System.out.printf("\t");
            System.out.println(peak);
        }
    }

    public void outputFeatures(List<Feature> features){
        System.out.printf("\n-Grouped the peaks into %d features.\n", features.size());
        for (Feature feature : features) {
            System.out.printf("\t");
            System.out.println(feature);
        }
    }

    public void outputLabelledAdductFeatures(List<LabelledAdductFeature> detectedLabelledAdductFeatures){
        System.out.println("\n-Labelled Adduct Features\n");
        for(int i=0; i<detectedLabelledAdductFeatures.size(); i++){
            System.out.printf("\n\t%d- ", i+1);
            System.out.println(detectedLabelledAdductFeatures.get(i));
        }
    }

    public void outputAnnotations(List<Annotation> annotations){
        System.out.printf("\n-Made %d annotations\n", annotations.size());
        for (int i = 0; i < annotations.size(); i++) {
            Annotation annotation = annotations.get(i);
            System.out.printf("  annotation %02d  %-60s  massDiff=% .6f  ppm=% .3f  logP=% .2f%n",
                    i + 1,
                    annotation.getCompound().getName(),
                    annotation.getMassDifference(),
                    annotation.getPpmDifference(),
                    annotation.getCompound().getLogp());
        }
    }

    public void outputScoredAnnotations(String outputString, List<ScoredAnnotation> scoredAnnotations){
        System.out.printf("\n-Made %d score annotations\n", scoredAnnotations.size());
        System.out.printf("\n-%s\n", outputString);

        for (int i = 0; i < scoredAnnotations.size(); i++) {
            ScoredAnnotation scoredAnnotation = scoredAnnotations.get(i);
            Annotation annotation = scoredAnnotation.getAnnotation();
            System.out.printf("\t");
            System.out.printf("  scored %02d  %-60s  adduct=%6s  rt=%6s  final=%6s  adductEvidence=%d  rtFeatures=%d%n",
                    i + 1,
                    annotation.getCompound().getName(),
                    formatScore(scoredAnnotation.getScoreAdduct()),
                    formatScore(scoredAnnotation.getScoreRT()),
                    formatScore(scoredAnnotation.getScoreFinal()),
                    scoredAnnotation.getAdductEvidenceCount(),
                    scoredAnnotation.getRtEvidenceFeatureCount());
        }
    }

    private String formatScore(Double score) {
        if (score == null) {
            return "null";
        }
        return String.format("%.3f", score);
    }

}

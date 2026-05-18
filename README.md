# Metabolite annotation by Liquid- Chromatography-Mass Spectrometry (LC-MS) Practice

## Decision Support System

### Abril Maitena Betti

This practice emulates the core functionality of the metabolite annotation tools, which
integrate liquid chromatography and mass spectrometry data with metabolite databases to
identify potential compounds and adducts based on the data acquired by analytical
instrumentation. Concretely it will be based on the **Retention Time (RT)** acquired by **Liquid
Chromatography (LC)** and the experimental **mass-to-charge ratio (m/z)** obtained by **Mass
Spectrometry (MS)** means.

Additionally, the practice includes Drools files where rules where developed in order to fulfill the
program's objective. Within these files, rule-based reasoning enable the program to match m/z values to metabolite candidates and
subsequently use the elution order based on the RT to confirm or reject annotations.

Regarding the architecture of the practice, the package separation and their responsibilities will be explained in depth:
1. **main:** represents the entry point of the application. Contains Main.java, which acts as the central orchestrator, initializing configuration 
settings, instantiating services, and executing pipeline stages sequentially.
2. **peak:** encapsulates data models representing raw analytical signals. 
   1. **Peak:** tracks raw mass spectrometry physical outputs—specifically mass-to-charge ratio ($m/z$), intensity, and retention time ($RT$).
   2. **LabelledPeak:** enriches a baseline peak object with assigned theoretical adduct hypotheses and calculated neutral molecular masses.
3. **feature:** handles data aggregation and signal clustering.
   1. **FeatureGeneration:** groups raw chromatographic peak nodes that share a common retention time footprint across an experimental run.
   2. **LabelledAdductFeature:** Represents a consolidated, multi-peak feature cluster with calculated consensus neutral mass variables ready for matching against chemical databases.
4. **adduct:** contains chemical rules metadata for ionization configurations.
   1. **Adduct:** Defines mathematical parameters (mass deltas, charge attributes) for target groups like $[M+H]^+$, $[M+Na]^+$, or $[M-H]^-$.
   2. **AdductDetector:** Automatically applies ionization logic models to screen features for potential adduct configurations based on raw observed $m/z$ variances.
5. **annotation:** represents the structural data layer. 
   1. **AnnotationService:** manages database life-cycles. It parses local flat-text resources (.tsv and .csv files) and executes optimized range queries to search for candidate compounds matching incoming consensus masses. 
   2. **Annotation:** A domain model matching a clustered experimental feature against an identified database compound hit.
6. **scoredAnnotation:** the system's decision-support layer. It maps candidate annotations into working memory networks to score data using the declarative Drools v10 rule engine framework.
   1. **AdductPatternScoringService** & **RetentionTimeScoringService:** services feeding data elements directly into custom-built RuleUnitData objects.
   2. **FinalScoringService:** combines rule penalty scores, organizes confidence scoring matrices, and compiles the final sorted metabolite metrics.
7. **resources:**
   1. **scoredAnnotation:** contains declarative production files (.drl scripts) that run structural validation checks (such as adduct pattern confirmation and experimental vs. expected LogP/RT retention consistency).
   2. **full_compounds_random_logps.tsv:** this is the master chemical compound matrix.
   3. **metabolites-example.csv:** this file corresponds to a smaller set of compounds that was used for testing methods.
   4. **peakData.csv:** file that contains the data of observed peaks.



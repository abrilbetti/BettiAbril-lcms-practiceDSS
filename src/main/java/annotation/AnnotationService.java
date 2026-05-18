package annotation;

import compound.Compound;
import feature.LabelledAdductFeature;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for annotating AdductLabelledFeature against compounds in an SQLite database.
 * This class was designed so it can work with two databases, a reduced one and a complete one. This decision is made when
 * instantiating the class, since the URL of the database is initialised in the constructor of the class.
 */
public class AnnotationService {

    // @TODO In a real application, these would be loaded from configuration or environment variables
    // @TODO For testing, you can use an in-memory database like H2 or SQLite and adjust the connection parameters accordingly
    // @TODO CHANGE YOUR PARAMETERS HERE
    private final String DB_URL;
    // be aware that different RDBMS may require different connection parameters, e.g. for H2
    // or SQLite you may not need user/password or the URL format may differ
    private static final String DB_USER = ""; // your user for your RDBMS
    private static final String DB_PASSWORD = ""; // your password for your RDBMS
    private static final double TOLERANCE_PPM = 10.0;

    /**
     * Constructor of the class AnnotationService that initialises the attribute DB_URL with the corresponding filepath depending on the
     * value of the variable received as parameter.
     * This is done because we have two databases, a reduced one and the complete one. Since some tests were done making use of the reduced
     * database, the code was adapted so it could change the databases back and forth.
     * Finally, the constructor calls the method that initialises the database.
     * @param completeDB variable of boolean type that is True when we want to use the complete database and False when we want
     * to use the reduced database.
     */
    public AnnotationService(boolean completeDB){
        if(completeDB) {
            this.DB_URL = "jdbc:sqlite:metaboliteComplete.db";
            initializeDatabase();
            populateDatabaseFromTextFile("full_compounds_random_logps.tsv");
        } else{
            this.DB_URL = "jdbc:sqlite:metabolitesExample.db";
            initializeDatabase();
            populateDatabaseFromTextFile("metabolites-example.csv");
        }
    }

    /**
     * This method initializes the database, creating a table called compounds that has a column per attribute.
     */
    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS compounds (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "formula TEXT, " +
                "monoisotopic_mass REAL, " +
                "logp REAL, " +
                "inchi TEXT);";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // By using a RuntimeException, the program stops if the DB fails
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    /**
     * Annotates the given AdductLabelledFeature by finding a matching compound in
     * the database
     * based on monoisotopic mass within 10 ppm tolerance.
     *
     * @param feature the feature to annotate
     * @return a list of compound annotations found within the mass tolerance
     */
    public List<Annotation> annotate(LabelledAdductFeature feature) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            return annotate(feature, TOLERANCE_PPM, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database: " + e.getMessage(), e);
        }
    }

    /**
     * Annotates the given AdductLabelledFeature using the provided connection.
     * Package-private for testing purposes.
     *
     * @param adductLabelledFeature the feature to annotate
     * @param conn    the database connection
     * @return a list of compound annotations found within the mass tolerance
     */
    List<Annotation> annotate(LabelledAdductFeature adductLabelledFeature, double toleranceInPPM, Connection conn) {
        double featureMonoIsotopicMass = adductLabelledFeature.getConsensusNeutralMass();
        String sql = "SELECT id, name, formula, monoisotopic_mass, logp, inchi FROM compounds" +
                " WHERE monoisotopic_mass BETWEEN ? AND ? ORDER BY ABS(monoisotopic_mass - ?)";
        List<Annotation> compoundAnnotations = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            double delta = featureMonoIsotopicMass * toleranceInPPM * 1e-6; // e = 10^ --> e-6 = 10ˆ-6 !!!
            double lower = featureMonoIsotopicMass - delta;
            double upper = featureMonoIsotopicMass + delta;
            stmt.setDouble(1, lower);
            stmt.setDouble(2, upper);
            stmt.setDouble(3, featureMonoIsotopicMass);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Compound compound = getCompoundFromResultSet(rs);
                    double massDifference = featureMonoIsotopicMass - compound.getMonoisotopicMass();
                    double ppmDifference = massDifference / compound.getMonoisotopicMass() * 1e6;
                    compoundAnnotations.add(new Annotation(adductLabelledFeature, compound, massDifference, ppmDifference));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error annotating feature: " + e.getMessage(), e);
        }
        return compoundAnnotations;
    }

    /**
     * This method retrieves each attribute from the read data from the database and creates a compound object with this information.
     * @param rs result set obtained out of executing the query
     * @return the created compound object
     * @throws SQLException in case there is any exception when using the result set instance to get the data through the getter functions.
     */
    private Compound getCompoundFromResultSet(ResultSet rs) throws SQLException {
        Compound compound = new Compound(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("formula"),
                rs.getDouble("monoisotopic_mass"),
                rs.getDouble("logp"),
                rs.getString("inchi"));
        return compound;
    }

    /**
     * Method that checks if the compounds table is empty. If no records exist, it reads the specified resource file
     * line by line and populates the database using a transaction loop.
     *
     * @param fileName the name of the file located in src/main/resources (.tsv or .csv)
     */
    public void populateDatabaseFromTextFile(String fileName) {
        // First we check if the table already contains data to avoid duplication
        String checkSql = "SELECT COUNT(*) FROM compounds;";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) > 0) {
                // Data is already loaded, exit safely
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error verifying database initialization state: " + e.getMessage());
            return;
        }

        java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            System.err.println("ERROR: Could not find compound resource file: " + fileName);
            return;
        }

        // Removed 'id' parameter target so auto-increment handles it sequentially without breaking uniqueness
        String insertSql = "INSERT INTO compounds (name, formula, monoisotopic_mass, logp, inchi) VALUES (?, ?, ?, ?, ?);";

        // Determine delimiter format: \\| for .tsv files, Semicolon (;) or comma for standard .csv files
        String delimiter = fileName.endsWith(".tsv") ? "\\|" : ";";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            // Disable auto-commit to drastically speed up batch execution in SQLite
            conn.setAutoCommit(false);

            String line;
            boolean isFirstLine = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // Skip the table header columns
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(delimiter);
                if (values.length < 5) {
                    continue;
                }

                try {
                    // Exact mappings matching the file columns shown in your editor:
                    pstmt.setString(1, values[0].trim());                      // name (e.g. Dimethylallyl pyrophosphate)
                    pstmt.setString(2, values[1].trim());                      // formula (e.g. C5H12O7P2)
                    pstmt.setDouble(3, Double.parseDouble(values[2].trim()));  // monoisotopic mass (e.g. 246.0058)
                    pstmt.setDouble(4, Double.parseDouble(values[3].trim()));  // logp (e.g. -1.1797)
                    pstmt.setString(5, values[4].trim());                      // inchi (e.g. InChI=1S/...)
                    pstmt.addBatch();
                    count++;

                    // Flush batch groups every 1000 items to balance operational memory
                    if (count % 1000 == 0) {
                        pstmt.executeBatch();
                    }
                } catch (Exception parseException) {
                    continue;
                }
            }

            pstmt.executeBatch();
            conn.commit();
            System.out.println("Imported " + count + " compounds into your database.");

        } catch (Exception e) {
            throw new RuntimeException("Error encountered during database population.", e);
        }
    }
}
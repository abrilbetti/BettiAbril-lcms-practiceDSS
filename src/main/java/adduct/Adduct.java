package adduct;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enumeration of common adduct types used in mass spectrometry for both positive and negative ionization modes.
 * Each adduct defines the mass difference, charge, and multimer information needed for peak annotation.
 */
public enum Adduct {
    // Positive mode adducts
    /** Proton adduct: [M+H]+ */
    M_PLUS_H(IonizationMode.POSITIVE, "[M+H]+", 1.007276, 1, 1),
    /** Double proton adduct: [M+2H]2+ */
    M_PLUS_2H(IonizationMode.POSITIVE, "[M+2H]2+", 1.007276*2, 2, 1),
    /** Sodium adduct: [M+Na]+ */
    M_PLUS_NA(IonizationMode.POSITIVE, "[M+Na]+", 22.989218, 1, 1),
    /** Potassium adduct: [M+K]+ */
    M_PLUS_K(IonizationMode.POSITIVE, "[M+K]+", 38.963158, 1, 1),
    /** Ammonium adduct: [M+NH4]+ */
    M_PLUS_NH4(IonizationMode.POSITIVE, "[M+NH4]+", 18.033823, 1, 1),
    /** Proton adduct with water loss: [M+H-H2O]+ */
    M_PLUS_H_MINUS_H2O(IonizationMode.POSITIVE, "[M+H-H2O]+", -17.0032, 1, 1),
    /** Mixed proton and ammonium adduct: [M+H+NH4]2+ */
    M_PLUS_H_PLUS_NH4(IonizationMode.POSITIVE, "[M+H+NH4]2+", 19.04110284, 2, 1),
    /** Dimer proton adduct: [2M+H]+ */
    M2_PLUS_H(IonizationMode.POSITIVE, "[2M+H]+", 1.007276, 1, 2),
    /** Dimer sodium adduct: [2M+Na]+ */
    M2_PLUS_NA(IonizationMode.POSITIVE, "[2M+Na]+", 22.989218, 1, 2),
    /** Formate sodium adduct: [M+H+HCOONa]+ */
    M_PLUS_H_PLUS_HCOO_NA(IonizationMode.POSITIVE, "[M+H+HCOONa]+", 68.9946, 1, 1),
    /** Dimer proton adduct with water loss: [2M+H-H2O]+ */
    M2_PLUS_H_MINUS_H2O(IonizationMode.POSITIVE, "[2M+H-H2O]+", -17.0032, 1, 2),

    // Negative mode adducts
    /** Deprotonated adduct: [M-H]- */
    M_MINUS_H(IonizationMode.NEGATIVE, "[M-H]-", -1.007276, 1, 1),
    /** Chloride adduct: [M+Cl]- */
    M_PLUS_CL(IonizationMode.NEGATIVE, "[M+Cl]-", 34.969402, 1, 1),
    /** Formate adduct: [M+HCOOH-H]- */
    M_PLUS_HCOO_H_MINUS_H(IonizationMode.NEGATIVE, "[M+HCOOH-H]-", 44.998201, 1, 1),
    /** Deprotonated adduct with water loss: [M-H-H2O]- */
    M_MINUS_H_MINUS_H2O(IonizationMode.NEGATIVE, "[M-H-H2O]-", -19.01839, 1, 1),
    /** Dimer deprotonated adduct: [2M-H]- */
    M2_MINUS_H(IonizationMode.NEGATIVE, "[2M-H]-", -1.007276, 1, 2),
    /** Double deprotonated adduct: [M-2H]2- */
    M_MINUS_2H(IonizationMode.NEGATIVE, "[M-2H]2-", -1.007276*2, 2, 1),
    /** Fluoride adduct: [M+F]- */
    M_PLUS_F(IonizationMode.NEGATIVE, "[M+F]-", 18.99895, 1, 1);

    private final IonizationMode mode;
    private final String adductName;
    private final double massDifference;
    private final int charge;
    private final int multimerNumber;

    /**
     * Creates a new Adduct with the specified properties.
     *
     * @param mode the ionization mode (positive or negative)
     * @param adductName the string representation of the adduct formula
     * @param massDifference the mass difference from the neutral molecule in daltons
     * @param charge the charge state of the adduct
     * @param multimerNumber the number of molecules in the adduct (1 for monomer, 2 for dimer, etc.)
     */
    Adduct(IonizationMode mode, String adductName, double massDifference, int charge, int multimerNumber) {
        this.mode = mode;
        this.adductName = adductName;
        this.massDifference = massDifference;
        this.charge = charge;
        this.multimerNumber = multimerNumber;
    }

    /**
     * Returns the ionization mode for this adduct.
     *
     * @return the ionization mode
     */
    public IonizationMode getMode() {
        return mode;
    }

    /**
     * Returns the string representation of this adduct formula.
     *
     * @return the adduct name (e.g., "[M+H]+")
     */
    public String getAdductName() {
        return adductName;
    }

    /**
     * Returns the charge state of this adduct.
     *
     * @return the charge (positive for cations, negative for anions)
     */
    public int getCharge() {
        return charge;
    }

    /**
     * Returns the mass difference from the neutral molecule.
     *
     * @return the mass difference in daltons
     */
    public double getMassDifference() {
        return massDifference;
    }

    /**
     * Returns the multimer number (number of molecules in this adduct).
     *
     * @return 1 for monomers, 2 for dimers, etc.
     */
    public int getMultimerNumber() {
        return multimerNumber;
    }

    /**
     * Checks if this adduct represents a multimer (more than one molecule).
     *
     * @return true if this is a multimer adduct, false otherwise
     */
    public boolean isMultimer() {
        return this.multimerNumber > 1;
    }

    /**
     * Returns all adducts that are compatible with the specified ionization mode.
     *
     * @param mode the ionization mode to filter by
     * @return a list of adducts for the specified mode
     */
    public static List<Adduct> getByMode(IonizationMode mode) {
        return Arrays.stream(values())
                .filter(a -> a.mode == mode)
                .collect(Collectors.toList());
    }

    /**
     * Finds an adduct by its string representation.
     *
     * @param adductName the adduct name to search for (e.g., "[M+H]+")
     * @return the matching adduct
     * @throws IllegalArgumentException if no adduct with the given name exists
     */
    public static Adduct getByAdductName(String adductName) {
        return Arrays.stream(values())
                .filter(a -> a.adductName.equals(adductName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown adduct with name: " + adductName));
    }
}
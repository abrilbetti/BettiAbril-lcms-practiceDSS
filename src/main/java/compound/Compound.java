package compound;

import java.util.Objects;

/**
 * This class represents a compound from the database.
 */
public final class Compound {
    private final int id;
    private final String name;
    private final String formula;
    private final double monoisotopicMass;
    private final double logp;
    private final String inchi;

    public Compound(int id, String name, String formula, double monoisotopicMass, double logp, String inchi) {
        this.id = id;
        this.name = name;
        this.formula = formula;
        this.monoisotopicMass = monoisotopicMass;
        this.logp = logp;
        this.inchi = inchi;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormula() {
        return formula;
    }

    public double getMonoisotopicMass() {
        return monoisotopicMass;
    }

    public double getLogp() {
        return logp;
    }

    public String getInchi() {
        return inchi;
    }

    @Override
    public String toString() {
        return "Compound{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", formula='" + formula + '\'' +
                ", monoisotopicMass=" + monoisotopicMass +
                ", logp=" + logp +
                ", inchi='" + inchi + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compound)) {
            return false;
        }
        Compound other = (Compound) o;
        return id == other.id && Double.compare(monoisotopicMass, other.monoisotopicMass) == 0 && Double.compare(logp, other.logp) == 0 && Objects.equals(name, other.name) && Objects.equals(formula, other.formula) && Objects.equals(inchi, other.inchi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, formula, monoisotopicMass, logp, inchi);
    }
}

package peak;

public class Peak {

    double mz;
    double intensity;
    double rt;

    /**
     * Empty constructor of the peak class
     */
    public Peak(){
        super();
    }

    /**
     * This constructor only initializes the attributes mz and intensity
     * @param mz value for the mz attribute
     * @param intensity value of the intensity attribute
     */
    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    /**
     * Constructor of the class peak that receives 3 parameters and sets each attribute of the class to the values passed as parameters.
     * @param mz value for the mz attribute
     * @param intensity value of the intensity attribute
     * @param rt value of the RT attribute
     */
    public Peak(double mz, double intensity, double rt) {
        this.mz = mz;
        this.intensity = intensity;
        this.rt = rt;
    }

    /**
     * Getter for the mz attribute of the class Peak
     * @return the value of mz
     */
    public double getMz() {
        return mz;
    }

    /**
     * Getter for the intensity attribute of the class Peak
     * @return the value of the intensity
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * Getter for the RT of the class Peak
     * @return the value of the RT
     */
    public double getRt() {
        return rt;
    }

    /**
     * Setter for RT that receives a value as parameter and sets it as the RT
     * @param rt value for the RT attribute
     */
    public void setRt(double rt) {
        this.rt = rt;
    }

    /**
     * Setter for mz that receives a value as parameter and sets it as the mz
     * @param mz value for the mz attribute
     */
    public void setMz(double mz) {
        this.mz = mz;
    }

    /**
     * Setter for the intensity that receives a value as parameter and sets it as the intensity
     * @param intensity value for the intensity attribute
     */
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    @Override
    public String toString() {
        return String.format("Peak(mz=%.4f, intensity=%.2f, rt=%.2f)", mz, intensity, rt);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(mz) * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Peak)) return false;
        Peak other = (Peak) obj;
        return Double.compare(mz, other.mz) == 0;
    }
}

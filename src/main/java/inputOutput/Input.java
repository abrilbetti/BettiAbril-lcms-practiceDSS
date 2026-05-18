package inputOutput;

import peak.Peak;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Input {

    /**
     * This method reads the CSV file where the data of the peaks is included and creates a list that contains
     * the Peak objects that were created out of reading the CSV file.
     * @param fileName name of the CSV file
     * @return the list of peaks
     */
    public List<Peak> loadPeaksFromCSV(String fileName) {
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
}

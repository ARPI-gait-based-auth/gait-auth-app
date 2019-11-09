package c.arp.gaitauth.Preprocessor;

import com.opencsv.CSVReader;

import java.io.FileReader;

import c.arp.gaitauth.StaticStore;


public class Preprocessor {

    /**
     * *.raw.csv file path.
     */
    public String rawFilePath;
    public String username;
    public AccData data;

    public Preprocessor(String rawFilePath) {
        if (rawFilePath == null) {
            rawFilePath = StaticStore.selectedFile;
        }
        this.openRawFile(rawFilePath);
    }

    /**
     * Saves processed data as *.processed.csv
     */
    void save() {

    }

    void clear() {
        this.data = new AccData();
    }

    /**
     * Opens file *.raw.csv
     *
     * @param filePath
     */
    void openRawFile(String filePath) {
        this.rawFilePath = filePath;
        this.clear();

        if (this.rawFilePath == null) {
            return;
        }
        try {
            CSVReader reader = new CSVReader(new FileReader(StaticStore.selectedFile));
            String[] nextLine;
            int c = -1;
            while ((nextLine = reader.readNext()) != null) {
                if (c == -1) {
                    c++;
                    continue;
                }
                c++;
                if (c < 50) {
                    continue;
                }
                if (c > 150) {
                    continue;
                }
                String index = nextLine[0];
                this.data.time.add(Long.parseLong(nextLine[1]));
                this.data.x.add(Double.parseDouble(nextLine[2]));
                this.data.y.add(Double.parseDouble(nextLine[3]));
                this.data.z.add(Double.parseDouble(nextLine[4]));
                this.username = nextLine[5];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

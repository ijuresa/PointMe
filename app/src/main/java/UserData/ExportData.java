package UserData;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanj on 20/01/2017.
 * @Description Class used to write data to .csv file
 */

public class ExportData {
    private String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private String fileName = "AnalysisData.csv";
    private String filePath = baseDir + File.separator + fileName;

    File f = new File(filePath);
    CSVWriter writer;

    /**
     * @Description Constructor of ExportData class
     *              Checks for file "AnalysisData.csv" and if it doesn't exist it creates new one
     *              with headers. Else it just appends data to existing .csv file
     * @throws IOException
     */
    private ExportData() throws IOException {
        if(f.exists() && !f.isDirectory()){
            FileWriter mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filePath));
            setHeaders();
        }
    }

    /**
     * @Description This function is called when file "AnalysisData.csv" is not found on drive
     *              It writes headers to .csv file for further data allocation
     * @throws IOException
     */
    private void setHeaders() throws IOException {
        List<String[]> data = new ArrayList<String[]>();
        data.add(new String[] {"Timestamp", "User", "Age", "Gender", "Phrase ID", "Phrase Length",
                "Wrong Chars", "Backspaces", "Time"});
        writer.writeAll(data);
        writer.close();
    }

    /**
     * @Description Called from main "TestActivity.java" when some parameters are met
     *                  a) InputUser string must be equal with User string - then it writes data
     *                      to .csv file
     *
     * @param _user         Name of the user
     * @param _age          Age of the user
     * @param _gender       Gender of the user
     * @param _index        Index of input string
     * @param _length       Length of the input string
     * @param _wBackspace   Number of wrong backspaces
     * @param _tBackspaces  Number of total backspaces
     * @param _time         Time needed to write string
     * @throws IOException
     */
    public void writeRow(String _user, String _age, String _gender, int _index, int _length,
                         int _wBackspace, int _tBackspaces, long _time) throws IOException {
        writer = new CSVWriter(new FileWriter(filePath, true));
        List<String[]> data = new ArrayList<String[]>();
        data.add(new String[] {String.valueOf(System.currentTimeMillis()), _user, _age, _gender,
                String.valueOf(_index), String.valueOf(_length), String.valueOf(_wBackspace),
                String.valueOf(_tBackspaces), String.valueOf(_time)});

        writer.writeAll(data);
        writer.close();

    }

    private static ExportData _exportData;
    static {
        try {
            _exportData = new ExportData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ExportData get_exportData() { return _exportData; }
}

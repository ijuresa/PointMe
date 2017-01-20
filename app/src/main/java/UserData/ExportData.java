package UserData;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanj on 20/01/2017.
 */

public class ExportData {
    private String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private String fileName = "AnalysisData.csv";
    private String filePath = baseDir + File.separator + fileName;

    File f = new File(filePath );
    CSVWriter writer;

    private ExportData() throws IOException {
        if(f.exists() && !f.isDirectory()){
            FileWriter mFileWriter = new FileWriter(filePath);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filePath));
            setHeaders();
        }
    }

    private void setHeaders() throws IOException {
        List<String[]> data = new ArrayList<String[]>();
        data.add(new String[] {"Timestamp", "User", "Age", "Gender", "Phrase ID", "Phrase Length",
                "Wrong Chars", "Backspaces", "Time"});
        writer.writeAll(data);
        writer.close();
    }

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

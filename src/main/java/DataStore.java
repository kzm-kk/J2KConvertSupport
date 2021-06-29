import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataStore {
    private static ArrayList<String> already_analyzed;
    private static HashMap<String, String> analyzed_data;
    private static HashMap<String, Long> file_lastmodified;
    private static HashMap<String, ArrayList<DataPanel>> analyzed_datapanel;

    public static void StartUp() {
        already_analyzed = new ArrayList<>();
        analyzed_data = new HashMap<>();
        file_lastmodified = new HashMap<>();
        analyzed_datapanel = new HashMap<>();
    }

    public static boolean Check_Already_Analyze(String myfilename) {
        for (String filename : already_analyzed) {
            if (filename.equals(myfilename)) return true;
        }
        return false;
    }

    public static void AddAnalyzed(String myfilename, String data) {
        already_analyzed.add(myfilename);
        analyzed_data.put(myfilename, data);
    }

    public static String GetAnalyzed(String myfilename) {
        return analyzed_data.get(myfilename);
    }

    public static void setTimeStamp(String myfilename, long modifiedtime) {
        file_lastmodified.put(myfilename, modifiedtime);
    }

    public static Long getTimeStamp(String myfilename) {
        return file_lastmodified.get(myfilename);
    }

    public static boolean isModified(String myfilename, long modifiedtime) {
        return file_lastmodified.get(myfilename) > modifiedtime;
    }

}

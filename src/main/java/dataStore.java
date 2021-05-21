import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class dataStore {
    public static String classname = new String();
    public static ArrayList<String> memory_classlibrary = new ArrayList<>();
    public static HashMap<String, ArrayList<ImportDeclaration>> memory_import = new HashMap<>();
    public static ArrayList<String> memory_classname = new ArrayList<>();
    public static HashMap<String, String> memory_extend = new HashMap<>();
    public static HashMap<String, ArrayList<String>> memory_implement = new HashMap<>();
    public static HashMap<String, List<FieldDeclaration>> memory_classfield = new HashMap<>();
    public static HashMap<String, List<MethodDeclaration>> memory_classmethod = new HashMap<>();
    public static HashMap<String, ArrayList<String>> memory_innerclass = new HashMap<>();
    public static HashMap<String, List<ConstructorDeclaration>> memory_constructor = new HashMap<>();
    public static HashMap<String, HashMap<String, HashMap<String, Object>>> memory_field_im = new HashMap<>();
}

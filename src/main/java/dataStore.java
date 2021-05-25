import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class dataStore {
    public static ArrayList<String> memory_classlibrary;
    public static HashMap<String, ArrayList<ImportDeclaration>> memory_import;
    public static ArrayList<String> memory_classname;
    public static HashMap<String, String> memory_extend;
    public static HashMap<String, ArrayList<String>> memory_implement;
    public static HashMap<String, List<FieldDeclaration>> memory_classfield;
    public static HashMap<String, List<MethodDeclaration>> memory_classmethod;
    public static HashMap<String, ArrayList<String>> memory_innerclass;
    public static HashMap<String, List<ConstructorDeclaration>> memory_constructor;
    public static HashMap<String, HashMap<String, HashMap<String, Object>>> memory_field_im;

    public static void init(){
        memory_classlibrary = new ArrayList<>();
        memory_import = new HashMap<>();
        memory_classname = new ArrayList<>();
        memory_extend = new HashMap<>();
        memory_implement = new HashMap<>();
        memory_classfield = new HashMap<>();
        memory_classmethod = new HashMap<>();
        memory_innerclass = new HashMap<>();
        memory_constructor = new HashMap<>();
        memory_field_im = new HashMap<>();
    }
}

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class GetClassFromFile {

    String path = "";
    private static ArrayList<String> classname;
    public static void setClassname(String name){
        classname.add(name);
    }
    public ArrayList<String> getClassname(){
        return classname;
    }
    public GetClassFromFile(String path) throws IOException {
        classname = new ArrayList<>();
        Path source = Paths.get(path);
        CompilationUnit cu = StaticJavaParser.parse(source);
        SearchVisitor visitor = new SearchVisitor();
        cu.accept(visitor,null);
    }

    public static class SearchVisitor extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(ClassOrInterfaceDeclaration md,Void arg){
            setClassname(md.getNameAsString());
        }

    }


}

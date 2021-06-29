import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class VisitorTools {

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


    private static String stacktext = new String();
    private static FilePath filePath = new FilePath();
    private static FixMarker marker;

    public static void setStacktext(String text){
        stacktext = text;
    }
    
    public static String getStacktext(){
        return stacktext;
    }

    public static void resetstack(){
        stacktext = "";
    }
    
    public static void visitorTools(Project project) throws IOException {

        List<String> sourceFileDirPathList = filePath.defineSourceFileDirPath(project);
        String path_root = sourceFileDirPathList.get(0);
        SourceRoot root = new SourceRoot(Paths.get(path_root));
        List<ParseResult<CompilationUnit>> cu2 = root.tryToParse("");
        init();
        resetstack();
        AnalyzerFactory.myToolWindow.resetPanel();
        marker = new FixMarker(project, filePath);
        //情報収集フェーズ
        for (ParseResult<CompilationUnit> compilationUnitParseResult : cu2) {
            VoidVisitor<?> visitor = new FirstVisitor();
            if (compilationUnitParseResult.getResult().isPresent())
                compilationUnitParseResult.getResult().get().accept(visitor, null);

        }
    }

    public static void judge_warning(String filename, ArrayList<String> arrayList){
        setStacktext("analyzed file:"+ filename + "\n");
        for(String classname:arrayList) {
            setStacktext("check start:" + classname + "\n");
            fullcheck_import(classname);
            check_initialize2(classname);
            check_allparameter(classname);
            judge_case1(classname);
            judge_case2(classname);
            judge_case3(classname);
            judge_case5and7(classname, "case5");
            judge_case5and7(classname, "case7");
            setStacktext("check finished:" + classname + "\n");
        }

    }

    private static void UsingMarker(Range range){
        //setStacktext(range.toString());
        marker.LineMarker(range);
    }

    private static void ShowResult_Editor(String text){
        AnalyzerFactory.myToolWindow.setOnPanel(new DataPanel(text));
    }

    public static void judge_case1(String key){
        if(memory_classfield.get(key) != null) {
            boolean already_warning_interface = false;
            boolean already_warning_superclass = false;
            for (FieldDeclaration field : memory_classfield.get(key)) {
                int size = field.getVariables().size();
                for (int i = 0; i < size; i++) {
                    String extend_field = field.getVariable(i).getNameAsString();
                    //implementsの確認、再帰関数使用

                    if (memory_implement.get(key) != null) {
                        for (String name_interface : memory_implement.get(key)) {
                            already_warning_interface = check_import(name_interface, already_warning_interface);
                            if (!check_import(name_interface, already_warning_interface)) check_ImplementField(name_interface, extend_field);
                        }
                    }

                    //extendsの確認、再帰関数使用
                    if (memory_extend.get(key) != null) {
                        already_warning_superclass = check_import(memory_extend.get(key), already_warning_superclass);
                        if (!check_import(memory_extend.get(key), already_warning_superclass)) check_ExtendField(memory_extend.get(key), extend_field);
                    }
                }
            }
        }
    }

    public static void judge_case2(String key){
        if(memory_classmethod.get(key) != null) {
            for (MethodDeclaration detail : memory_classmethod.get(key)) {
                //get/set探す部分
                String methodname = detail.getNameAsString();
                String cut_field = "";

                if (search_get("case2", detail))
                    cut_field = methodname.split("get")[1].toLowerCase();
                if (!cut_field.equals("")) {
                    if (!match_field(key, cut_field)) {
                        UsingMarker(detail.getRange().get());
                        ShowResult_Editor(detail.getRange().get()+"\n"+
                                "method \"" + methodname + "\" is existing getter" +
                                " but field \"" + cut_field + "\" is not existing.\n"
                                + "You should change methodname from "
                                + methodname + " to " + methodname.toLowerCase() + " or other name.\n");
                    }
                }
                cut_field = "";
                if (search_set("case2", detail))
                    cut_field = methodname.split("set")[1].toLowerCase();
                if (!cut_field.equals("")) {
                    if (!match_field(key, cut_field)) {
                        UsingMarker(detail.getRange().get());
                        ShowResult_Editor(detail.getRange().get()+"\n"+
                                "method \"" + methodname + "\" is existing setter" +
                                " but field \"" + cut_field + "\" is not existing.\n"
                                + "You should change methodname from "
                                + methodname + " to " + methodname.toLowerCase() + " or other name.\n");
                    }
                }

            }
        }
    }

    public static void judge_case3(String key){
        if(memory_innerclass.get(key) != null) {
            for (String inner : memory_innerclass.get(key)) {
                if (memory_classmethod.get(inner) == null) break;
                for (MethodDeclaration md : memory_classmethod.get(inner)) {
                    NodeList modifiers = md.getModifiers();
                    boolean flag = false;
                    int mod_size = modifiers.size();
                    if (mod_size == 0) continue;
                    String name = md.getNameAsString();
                    for (int i = 0; i < mod_size; i++) {
                        if (modifiers.get(i).toString().matches("private ")) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        for (MethodDeclaration enclosing : memory_classmethod.get(key)) {
                            VoidVisitor<?> visitor = new Checking_Case3(name);
                            enclosing.accept(visitor, null);//visitorを利用して捜索と警告を両方行う
                        }
                    }
                }
            }
        }
    }

    public static void judge_case5and7(String key, String mode){
            List<MethodDeclaration> judge_array = memory_classmethod.get(key);//今のクラスのフィールド取得
            if(judge_array != null) {
                for (MethodDeclaration detail:judge_array) {//フィールドを１つずつ見る

                    int size = detail.getParameters().size();
                    if (size == 0) continue;
                    else {
                        String methodname = detail.getNameAsString();
                        for (int i = 0; i < size; i++) {

                            String param = detail.getParameter(i).getNameAsString();
                            VoidVisitor<?> visitor = new Checking_Case5and7(key, methodname, param, mode);
                            detail.accept(visitor, null);//visitorを利用して捜索と警告を両方行う

                        }
                    }
                }
            }
    }

    public static void check_ExtendField(String origin, String extend_field){
        if(memory_classfield.get(origin) != null){
            for(FieldDeclaration field: memory_classfield.get(origin)){
                int size = field.getVariables().size();
                for (int i = 0; i < size; i++) {
                    String origin_field = field.getVariable(i).getNameAsString();
                    if (extend_field.equals(origin_field)) {
                        ShowResult_Editor("same name field:" + origin_field + "(origin), "
                                + extend_field + "(extends)"
                                + "\nIf a correction is necessary, you should change name of field(extends).\n");
                        break;
                    }
                }
            }}
        if(memory_extend.get(origin) != null) check_ExtendField(memory_extend.get(origin), extend_field);
        if(memory_implement.get(origin) != null){
            for(String key: memory_implement.get(origin)) check_ImplementField(key, extend_field);
        }
    }

    public static void check_ImplementField(String origin, String implement_field){
        if(memory_classfield.get(origin) != null) {
            for (FieldDeclaration field : memory_classfield.get(origin)) {
                int size = field.getVariables().size();
                for (int i = 0; i < size; i++) {
                    String origin_field = field.getVariable(i).getNameAsString();
                    if (implement_field.equals(origin_field)) {
                        ShowResult_Editor("same name field:" + origin_field + "(interface), "
                                + implement_field + "(implements-class)"
                                + "\nIf a correction is necessary, you should change name of field(implements-class).\n");
                        break;
                    }
                }
            }
        }
        if(memory_extend.get(origin) != null) check_ExtendField(origin, implement_field);
    }

    public static boolean search_get(String mode, MethodDeclaration detail){
        String methodname = detail.getNameAsString();
        boolean flag = false;

        if (methodname.matches("get[A-Z].*")) {
            if (detail.getParameters().isEmpty()) {
                String returns = detail.getTypeAsString();
                if (!returns.equals("void") || (returns.equals("void") && !mode.equals("case2"))) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public static boolean search_set(String mode, MethodDeclaration detail){
        String methodname = detail.getNameAsString();
        boolean flag = false;
        if (methodname.matches("set[A-Z].*")) {
            int size_param = detail.getParameters().size();
            if (size_param == 1) {
                String returns = detail.getTypeAsString();
                if (returns.equals("void")) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public static boolean match_field(String method, String xxx){
        if(memory_classfield.get(method) != null){
            for (FieldDeclaration field : memory_classfield.get(method)) {
                int size = field.getVariables().size();
                for (int i = 0; i < size; i++) {
                    String fieldname = field.getVariable(i).getNameAsString();
                    if (fieldname.equals(xxx) || fieldname.toLowerCase().equals(xxx) || fieldname.toUpperCase().equals(xxx)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean check_ExtendMethod(String origin, String mode){
        List<MethodDeclaration> check_array = memory_classmethod.get(origin);
        boolean found = false;
        if(check_array != null) {
            for(MethodDeclaration detail:check_array) {
                found = search_get(mode, detail);
                if (!found) {
                    found = search_set(mode, detail);
                    if (!found) {
                        continue;
                    } else break;
                } else break;
            }
        }
        if(memory_extend.get(origin) != null && !found){
            found = check_ExtendMethod(memory_extend.get(origin), mode);
        }
        if(memory_implement.get(origin) != null && !found){
            for(String key: memory_implement.get(origin)){
                found = check_ImplementMethod(key, mode);
                if(found) break;
            }
        }
        //found = check_import(origin, false);
        return found;
    }

    public static boolean check_ImplementMethod(String origin, String mode){
        List<MethodDeclaration> check_array = memory_classmethod.get(origin);
        boolean found = false;
        if(check_array != null) {
            for(MethodDeclaration detail:check_array) {
                found = search_get(mode, detail);
                if (!found) {
                    found = search_set(mode, detail);
                    if (!found) {
                        continue;
                    } else break;
                } else break;
            }
        }
        while (!found){
            if(memory_extend.get(origin) != null){
                found = check_ExtendMethod(memory_extend.get(origin), mode);
            } else break;
        }
        //found = check_import(origin, false);
        return found;
    }

    public static class FirstVisitor extends VoidVisitorAdapter<Void>{
        ArrayList<ImportDeclaration> Import_list = new ArrayList<>();

        @Override
        public void visit(ImportDeclaration md, Void arg){
            super.visit(md, arg);
            Import_list.add(md);
            
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration md, Void arg){
            memory_import.put(md.getNameAsString(), Import_list);
            SomeVisitor visitor = new SomeVisitor(md.getNameAsString());
            md.accept(visitor, null);
        }

    }

    public static class SomeVisitor extends VoidVisitorAdapter<Void> {
        String classname = "";
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        List<FieldDeclaration> fieldDeclarations = new ArrayList<>();
        ArrayList<String> inner_list = new ArrayList<>();
        HashMap<String,HashMap<String,Object>> field_data = new HashMap<>();

        public SomeVisitor(String name){
            classname = name;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration md, Void arg){
            if(classname.equals(md.getNameAsString())){
                memory_classname.add(classname);
                int size_extend = md.getExtendedTypes().size();
                int size_implement = md.getImplementedTypes().size();
                if(size_extend != 0){
                    memory_extend.put(classname, md.getExtendedTypes().get(0).getNameAsString());
                }
                if(size_implement != 0){
                    ArrayList<String> names = new ArrayList<>();
                    for(int i = 0; i < size_implement ;i++){
                        names.add(md.getImplementedTypes(i).getNameAsString());
                    }
                    memory_implement.put(classname, names);
                }

                fieldDeclarations = md.getFields();
                memory_classfield.put(classname, fieldDeclarations);
                //ここから新しいやつ
                for (FieldDeclaration field : fieldDeclarations) {
                    int size = field.getVariables().size();
                    for (int i = 0; i < size; i++) {
                        String fieldname = field.getVariable(i).getNameAsString();
                        HashMap<String,Object> data = new HashMap<>();
                        //name
                        data.put("type",field.getVariable(i).getTypeAsString());
                        //initial,nullable
                        if (field.getVariable(i).getInitializer().isPresent()) {
                            data.put("initial",null);
                            if(field.getVariable(i).getType().isPrimitiveType())
                                data.put("nullable",false);
                            else data.put("nullable",true);
                        } else {
                            data.put("initial", field.getVariable(i).getInitializer());
                            data.put("nullable",false);
                        }
                        //need_fix
                        data.put("need_fix", false);
                        //range
                        data.put("range",field.getVariable(i).getRange().get());
                        //istype
                        if(field.getVariable(i).getType().isPrimitiveType()) data.put("IsType", 0);
                        else if(field.getVariable(i).getType().isReferenceType()) data.put("IsType", 1);
                        else data.put("IsType", 2);

                        field_data.put(fieldname, data);
                    }
                }
                memory_field_im.put(classname, field_data);
                //ここまで
                methodDeclarations = md.getMethods();
                memory_classmethod.put(classname, methodDeclarations);
                memory_constructor.put(classname, md.getConstructors());
                super.visit(md, arg);
            } else {
                if(memory_innerclass.get(classname) == null) inner_list = new ArrayList<>();
                else inner_list = memory_innerclass.get(classname);
                inner_list.add(md.getNameAsString());
                memory_innerclass.put(classname, inner_list);
                SomeVisitor visitor = new SomeVisitor(md.getNameAsString());
                md.accept(visitor, null);
            }
        }
    }

    public static class Checking_Case3 extends VoidVisitorAdapter<Void>{
        String methodname = "";

        public Checking_Case3(String name){
            methodname = name;
        }

        @Override
        public void visit(MethodCallExpr md, Void arg){
            String calling = md.getNameAsString();
            if(calling.equals(methodname)){
                UsingMarker(md.getRange().get());
                ShowResult_Editor(md.getRange().get()+"\n"+methodname + " is private method. " +
                        "This code can cause an error after converting to Kotlin.\n");
            }
        }

    }

    //継承先の呼び出し箇所→メソッドの存在場所の順(これまでと反対)に探す新しいバージョン
    public static class Checking_Case5and7 extends VoidVisitorAdapter<Void> {
        ArrayList<MethodCallExpr> list = new ArrayList<>();
        String classname = "";
        String methodname = "";
        String looking_argument = "";
        String mode = "";
        public Checking_Case5and7(String classname,String methodname, String looking_argument, String mode){
            this.classname = classname;
            this.methodname = methodname;
            this.looking_argument = looking_argument;
            this.mode = mode;
        }

        @Override
        public void visit(MethodCallExpr md, Void arg){

            boolean flag = false;
            if(mode.equals("case5"))flag = md.getScope().isEmpty();
            else if(mode.equals("case7") && md.getScope().isPresent())
                flag = md.getScope().get().isThisExpr();
            if(flag) {
                String methodname = md.getNameAsString();

                if (methodname.matches("get[A-Z].*")) {
                    if (md.getArguments() == null) {
                        boolean break_flag = false;
                        for(MethodDeclaration mine: memory_classmethod.get(classname)) {
                            String mine_name = mine.getNameAsString();
                            String mine_type = mine.getTypeAsString();
                            int mine_sizeParameter = mine.getParameters().size();
                            if (mine_name.equals(methodname)) {//自分のメソッドの場合
                                break_flag = true;
                                break;
                            }
                        }//継承元クラス・インターフェースのデフォルトクラスの場合

                        boolean warning_flag = false;
                        //implementsの確認、再帰関数使用
                        if (memory_implement.get(classname) != null) {
                            for (String name_interface : memory_implement.get(classname)) {
                                warning_flag = check_ImplementMethod(name_interface, mode);
                            }
                        }

                        //extendsの確認、再帰関数使用
                        if (memory_extend.get(classname) != null && !warning_flag) {
                            warning_flag = check_ExtendMethod(memory_extend.get(classname), mode);
                        }
                        if(!break_flag || warning_flag)  {
                            UsingMarker(md.getRange().get());
                            ShowResult_Editor(md.getRange().get()+"\n"+"This code may give the following error after converting to Kotlin: val cannnot reassigned.\n"
                                    + "It is recommended to rename the argument \"" + looking_argument + "\" in method \"" + this.methodname + "\".\n");
                        }
                    }
                } else if (methodname.matches("set[A-Z].*")) {
                    int size_param = md.getArguments().size();
                    if (size_param == 1) {
                        String argument = md.getArgument(0).toString();
                        String cut_field = methodname.split("set")[1].toLowerCase();
                        if (argument.equals(looking_argument) && cut_field.equals(argument)) {
                            boolean break_flag = false;
                            for(MethodDeclaration mine: memory_classmethod.get(classname)) {
                                String mine_name = mine.getNameAsString();
                                String mine_type = mine.getTypeAsString();
                                int mine_sizeParameter = mine.getParameters().size();
                                if (mine_name.equals(methodname)) {//自分のメソッドの場合
                                    if(!mine_type.equals("void")){//返り値voidかつ引数１
                                        if(mine_sizeParameter == 1){
                                            break_flag = true;
                                            break;
                                        }
                                    }
                                }
                            }//継承元クラス・インターフェースのデフォルトクラスの場合

                            boolean warning_flag = false;
                            //implementsの確認、再帰関数使用
                            if (memory_implement.get(classname) != null) {
                                for (String name_interface : memory_implement.get(classname)) {
                                    warning_flag = check_ImplementMethod(name_interface, mode);
                                }
                            }
                            //extendsの確認、再帰関数使用
                            if (memory_extend.get(classname) != null && !warning_flag) {
                                warning_flag = check_ExtendMethod(memory_extend.get(classname), mode);
                            }
                            if(!break_flag || warning_flag) {

                                UsingMarker(md.getRange().get());
                                ShowResult_Editor(md.getRange().get()+"\n"+"This code may give the following error after converting to Kotlin: val cannnot reassigned.\n"
                                        + "It is recommended to rename the argument \"" + looking_argument + "\" in method \"" + this.methodname + "\".\n");
                            }
                        }
                    }
                }
            }
        }
    }

    public static void check_initialize2(String classname){
        HashMap<String,HashMap<String,Object>> field_list2 = memory_field_im.get(classname);
        if(field_list2 != null){
            for(String fieldname : field_list2.keySet()){
                HashMap<String, Object> detail = field_list2.get(fieldname);
                if((boolean)detail.get("nullable")){
                    boolean flag = true;
                    List<ConstructorDeclaration> CdList = memory_constructor.get(classname);
                    if(CdList != null) {
                        for (ConstructorDeclaration constructorDeclaration : CdList) {
                            check_constructor visitor = new check_constructor(classname,fieldname);
                            if(constructorDeclaration.accept(visitor, null) != null)
                                flag = constructorDeclaration.accept(visitor, null);
                        }
                    }
                    List<MethodDeclaration> MdList = memory_classmethod.get(classname);
                    if(MdList != null) {
                        for (MethodDeclaration methodDeclaration: MdList) {
                            check_nullable visitor = new check_nullable(classname, fieldname);
                            methodDeclaration.accept(visitor, null);
                        }
                    }
                    List<FieldDeclaration> FdList = memory_classfield.get(classname);
                    if(FdList != null && !(boolean)detail.get("nullable")) {
                        for (FieldDeclaration fieldDeclaration : FdList) {
                            check_nullable visitor = new check_nullable(classname, fieldname);
                            fieldDeclaration.accept(visitor, null);
                        }
                    }
                    if(flag && !(boolean)detail.get("nullable")){
                        if ((int)detail.get("IsType") != Integer.parseInt("0")) {
                            UsingMarker((Range)detail.get("range"));
                            ShowResult_Editor(detail.get("range")+"\n"+"Field \"" + fieldname + "\" doesn't have initializer."
                                    + "You should use modifer \"lateinit\" after convert to Kotlin.\n");
                            detail.put("need_fix",true);
                        }
                    }
                }
            }
        }
    }

    public static void check_allparameter(String classname){
        HashMap<String,HashMap<String,Object>> field_list2 = memory_field_im.get(classname);
        if(field_list2 != null){
            for(String fieldname : field_list2.keySet()){
                HashMap<String, Object> detail = field_list2.get(fieldname);
                if(!(boolean)detail.get("nullable")){
                    List<MethodDeclaration> MdList = memory_classmethod.get(classname);
                    if(MdList != null) {
                        for(MethodDeclaration methodDeclaration: MdList){
                            for(Parameter parameter :methodDeclaration.getParameters()) {
                                check_parameter visitor = new check_parameter
                                        (classname, fieldname,parameter.getNameAsString(), parameter.getRange().get().toString());
                                methodDeclaration.accept(visitor, null);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class check_constructor extends GenericVisitorAdapter<Boolean,Void> {
        String classname = "";
        String fieldname = "";

        public check_constructor(String classname, String fieldname) {
            this.classname = classname;
            this.fieldname = fieldname;
        }

        @Override
        public Boolean visit(AssignExpr md, Void arg){
            if(md.getTarget().toString().equals(fieldname)){
                memory_field_im.get(classname).get(fieldname).put("nullable",true);
                memory_field_im.get(classname).get(fieldname).put("need_fix",false);
                return false;
            }
            memory_field_im.get(classname).get(fieldname).put("nullable",false);
            memory_field_im.get(classname).get(fieldname).put("need_fix",true);
            return true;
        }
    }

    public static class check_nullable extends VoidVisitorAdapter<Void> {
        String classname = "";
        String fieldname = "";

        public check_nullable(String classname, String fieldname) {
            this.classname = classname;
            this.fieldname = fieldname;
        }

        @Override
        public void visit(AssignExpr md, Void arg){
            boolean flag = false;
            if(md.getTarget().toString().equals(fieldname)){
                if(md.getOperator().name().equals("ASSIGN")){
                    if(md.getValue().toString().equals("null")){
                        memory_field_im.get(classname).get(fieldname).put("nullable",true);
                        memory_field_im.get(classname).get(fieldname).put("need_fix",false);
                    } else flag = true;
                } else flag = true;
            } else flag = true;
            if(flag) {
                memory_field_im.get(classname).get(fieldname).put("nullable", false);
                memory_field_im.get(classname).get(fieldname).put("need_fix", true);
            }
        }
    }

    public static class check_parameter extends VoidVisitorAdapter<Void> {
        String classname = "";
        String fieldname = "";
        String parameter = "";
        String range = "";

        public check_parameter(String classname, String fieldname, String parameter, String range) {
            this.classname = classname;
            this.fieldname = fieldname;
            this.parameter = parameter;
            this.range = range;
        }

        @Override
        public void visit(AssignExpr md, Void arg){
            if(md.getValue().toString().equals(parameter)){
                if(md.getTarget().toString().equals(fieldname)){
                    if((boolean) memory_field_im.get(classname).get(fieldname).get("need_fix")){
                        ShowResult_Editor(range+"\nparameter:"+parameter+" will be changed nullable parameter after conversion.\n" +
                                "You should use @Notnull annotation.\n");
                    }
                }
            }

        }
    }

    public static boolean check_import(String checkname, boolean already){
        for(String classname: memory_classname){
            if(classname.equals(checkname)){
                return false;
            }
        }
        if(!already)ShowResult_Editor("\""+checkname+"\" is probably a library. Please check the detail of library if necessary.\n");
        return true;
    }

    public static void fullcheck_import(String checkname){
        boolean flag = true;
        if(memory_implement.get(checkname) != null) {
            for (String implement : memory_implement.get(checkname)) {
                flag = true;
                for (String classname : memory_classname) {
                    if (classname.equals(implement)) {
                        flag = false;
                        break;
                    }
                }
                if (flag && !duplicate_check(implement)) memory_classlibrary.add(implement);
            }
        }
        flag = true;
        String extend = memory_extend.get(checkname);
        for(String classname: memory_classname){
            if(classname.equals(extend)) {
                flag = false;
            }
        }
        if(extend == null) flag = false;
        if(flag && !duplicate_check(extend)) memory_classlibrary.add(extend);
    }

    public static boolean duplicate_check(String name){
        if(memory_classlibrary != null) {
            for (String library : memory_classlibrary) {
                if(library == null) break;
                if (library.equals(name)) return true;
            }
        }
        return false;
    }

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

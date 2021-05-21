import com.android.aapt.Resources;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisitorTools {
    
    private static String stacktext = new String();
    private static FilePath filePath = new FilePath();
    
    public static void setStacktext(String text){
        stacktext = stacktext.concat(text+"\n");
    }
    
    public String getStacktext(){
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
        resetstack();
        //情報収集フェーズ
        for(int i = 0; i < cu2.size(); i++){
            VoidVisitor<?> visitor = new FirstVisitor();
            cu2.get(i).getResult().get().accept(visitor, null);
        }

        /*//判断・警告フェーズ(出力)
        for(String classname:memory_classname){
            setStacktext("\ncheck start:"+classname+"\n");
            fullcheck_import(classname);
            check_initialize2(classname);
            check_allparameter(classname);
            judge_case1(classname);
            judge_case2(classname);
            judge_case3(classname);
            judge_case5and7(classname, "case5");
            judge_case5and7(classname, "case7");
            setStacktext("check finished:"+classname+"\n");
        }

        for(String name:memory_classlibrary){
            setStacktext(name);
        }*/

    }

    public void judge_warning(String classname){
            setStacktext("\ncheck start:"+classname+"\n");
            fullcheck_import(classname);
            check_initialize2(classname);
            check_allparameter(classname);
            judge_case1(classname);
            judge_case2(classname);
            judge_case3(classname);
            judge_case5and7(classname, "case5");
            judge_case5and7(classname, "case7");
            setStacktext("check finished:"+classname+"\n");
    }

    public static void judge_case1(String key){
        setStacktext("check start:case1\n");
        if(dataStore.memory_classfield.get(key) != null) {
            boolean already_warning_interface = false;
            boolean already_warning_superclass = false;
            for (FieldDeclaration field : dataStore.memory_classfield.get(key)) {
                int size = field.getVariables().size();
                for (int i = 0; i < size; i++) {
                    String extend_field = field.getVariable(i).getNameAsString();
                    //implementsの確認、再帰関数使用

                    if (dataStore.memory_implement.get(key) != null) {
                        for (String name_interface : dataStore.memory_implement.get(key)) {
                            already_warning_interface = check_import(name_interface, already_warning_interface);
                            if (!check_import(name_interface, already_warning_interface)) check_ImplementField(name_interface, extend_field);
                        }
                    }

                    //extendsの確認、再帰関数使用
                    if (dataStore.memory_extend.get(key) != null) {
                        already_warning_superclass = check_import(dataStore.memory_extend.get(key), already_warning_superclass);
                        if (!check_import(dataStore.memory_extend.get(key), already_warning_superclass)) check_ExtendField(dataStore.memory_extend.get(key), extend_field);
                    }
                }
            }
        }
        setStacktext("check finished:case1\n");
    }

    public static void judge_case2(String key){
        setStacktext("check start:case2\n");
        if(dataStore.memory_classmethod.get(key) != null) {
            for (MethodDeclaration detail : dataStore.memory_classmethod.get(key)) {
                //get/set探す部分
                String methodname = detail.getNameAsString();
                String cut_field = "";

                if (search_get("case2", detail))
                    cut_field = methodname.split("get")[1].toLowerCase();
                if (!cut_field.equals("")) {
                    if (!match_field(key, cut_field)) {
                        setStacktext(detail.getRange().get().toString());
                        setStacktext("method \"" + methodname + "\" is existing getter" +
                                " but field \"" + cut_field + "\" is not existing.");
                        setStacktext("You should change methodname from "
                                + methodname + " to " + methodname.toLowerCase() + " or other name.\n");
                    }
                }
                cut_field = "";
                if (search_set("case2", detail))
                    cut_field = methodname.split("set")[1].toLowerCase();
                if (!cut_field.equals("")) {
                    if (!match_field(key, cut_field)) {
                        setStacktext(detail.getRange().get().toString());
                        setStacktext("method \"" + methodname + "\" is existing setter" +
                                " but field \"" + cut_field + "\" is not existing.");
                        setStacktext("You should change methodname from "
                                + methodname + " to " + methodname.toLowerCase() + " or other name.\n");
                    }
                }

            }
        }
        setStacktext("check finished:case2\n");
    }

    public static void judge_case3(String key){
        setStacktext("check start:case3\n");
        if(dataStore.memory_innerclass.get(key) != null) {
            for (String inner : dataStore.memory_innerclass.get(key)) {
                if (dataStore.memory_classmethod.get(inner) == null) break;
                for (MethodDeclaration md : dataStore.memory_classmethod.get(inner)) {
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
                        for (MethodDeclaration enclosing : dataStore.memory_classmethod.get(key)) {
                            VoidVisitor<?> visitor = new Checking_Case3(name);
                            enclosing.accept(visitor, null);//visitorを利用して捜索と警告を両方行う
                        }
                    }
                }
            }
        }
        setStacktext("check finished:case3\n");
    }

    public static void judge_case5and7(String key, String mode){
        setStacktext("check start:" + mode+"\n");
            List<MethodDeclaration> judge_array = dataStore.memory_classmethod.get(key);//今のクラスのフィールド取得
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
        setStacktext("check finished:"+ mode +"\n");
    }

    public static void check_ExtendField(String origin, String extend_field){
        if(dataStore.memory_classfield.get(origin) != null){
            for(FieldDeclaration field:dataStore.memory_classfield.get(origin)){
                int size = field.getVariables().size();
                for (int i = 0; i < size; i++) {
                    String origin_field = field.getVariable(i).getNameAsString();
                    if (extend_field.equals(origin_field)) {
                        setStacktext("same name field:" + origin_field + "(origin), "
                                + extend_field + "(extends)"
                                + "\nIf a correction is necessary, you should change name of field(extends).\n");
                        break;
                    }
                }
            }}
        if(dataStore.memory_extend.get(origin) != null) check_ExtendField(dataStore.memory_extend.get(origin), extend_field);
        if(dataStore.memory_implement.get(origin) != null){
            for(String key:dataStore.memory_implement.get(origin)) check_ImplementField(key, extend_field);
        }
    }

    public static void check_ImplementField(String origin, String implement_field){
        if(dataStore.memory_classfield.get(origin) != null) {
            for (FieldDeclaration field : dataStore.memory_classfield.get(origin)) {
                int size = field.getVariables().size();
                for (int i = 0; i < size; i++) {
                    String origin_field = field.getVariable(i).getNameAsString();
                    if (implement_field.equals(origin_field)) {
                        setStacktext("same name field:" + origin_field + "(interface), "
                                + implement_field + "(implements-class)"
                                + "\nIf a correction is necessary, you should change name of field(implements-class).\n");
                        break;
                    }
                }
            }
        }
        if(dataStore.memory_extend.get(origin) != null) check_ExtendField(origin, implement_field);
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
        if(dataStore.memory_classfield.get(method) != null){
            for (FieldDeclaration field : dataStore.memory_classfield.get(method)) {
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
        List<MethodDeclaration> check_array = dataStore.memory_classmethod.get(origin);
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
        if(dataStore.memory_extend.get(origin) != null && !found){
            found = check_ExtendMethod(dataStore.memory_extend.get(origin), mode);
        }
        if(dataStore.memory_implement.get(origin) != null && !found){
            for(String key:dataStore.memory_implement.get(origin)){
                found = check_ImplementMethod(key, mode);
                if(found) break;
            }
        }
        //found = check_import(origin, false);
        return found;
    }

    public static boolean check_ImplementMethod(String origin, String mode){
        List<MethodDeclaration> check_array = dataStore.memory_classmethod.get(origin);
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
            if(dataStore.memory_extend.get(origin) != null){
                found = check_ExtendMethod(dataStore.memory_extend.get(origin), mode);
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
            dataStore.memory_import.put(md.getNameAsString(), Import_list);
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
                dataStore.memory_classname.add(classname);
                int size_extend = md.getExtendedTypes().size();
                int size_implement = md.getImplementedTypes().size();
                if(size_extend != 0){
                    dataStore.memory_extend.put(classname, md.getExtendedTypes().get(0).getNameAsString());
                }
                if(size_implement != 0){
                    ArrayList<String> names = new ArrayList<>();
                    for(int i = 0; i < size_implement ;i++){
                        names.add(md.getImplementedTypes(i).getNameAsString());
                    }
                    dataStore.memory_implement.put(classname, names);
                }

                fieldDeclarations = md.getFields();
                dataStore.memory_classfield.put(classname, fieldDeclarations);
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
                        data.put("range",field.getVariable(i).getRange().get().toString());
                        //istype
                        if(field.getVariable(i).getType().isPrimitiveType()) data.put("IsType", 0);
                        else if(field.getVariable(i).getType().isReferenceType()) data.put("IsType", 1);
                        else data.put("IsType", 2);

                        field_data.put(fieldname, data);
                    }
                }
                dataStore.memory_field_im.put(classname, field_data);
                //ここまで
                methodDeclarations = md.getMethods();
                dataStore.memory_classmethod.put(classname, methodDeclarations);
                dataStore.memory_constructor.put(classname, md.getConstructors());

                super.visit(md, arg);
            } else {
                if(dataStore.memory_innerclass.get(classname) == null) inner_list = new ArrayList<>();
                else inner_list = dataStore.memory_innerclass.get(classname);
                inner_list.add(md.getNameAsString());
                dataStore.memory_innerclass.put(classname, inner_list);
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
                setStacktext(md.getRange().get().toString());
                setStacktext(methodname + " is private method. " +
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
            if(mode.equals("case5"))flag = md.getScope().isPresent() ;
            else if(mode.equals("case7") && !md.getScope().isPresent())
                flag = md.getScope().get().isThisExpr();
            if(flag) {
                String methodname = md.getNameAsString();

                if (methodname.matches("get[A-Z].*")) {
                    if (md.getArguments() == null) {
                        boolean break_flag = false;
                        for(MethodDeclaration mine:dataStore.memory_classmethod.get(classname)) {
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
                        if (dataStore.memory_implement.get(classname) != null) {
                            for (String name_interface : dataStore.memory_implement.get(classname)) {
                                warning_flag = check_ImplementMethod(name_interface, mode);
                            }
                        }

                        //extendsの確認、再帰関数使用
                        if (dataStore.memory_extend.get(classname) != null && !warning_flag) {
                            warning_flag = check_ExtendMethod(dataStore.memory_extend.get(classname), mode);
                        }
                        if(!break_flag || warning_flag)  {
                                setStacktext(md.getRange().get().toString());
                                setStacktext("This code may give the following error after converting to Kotlin: val cannnot reassigned.");
                                setStacktext("It is recommended to rename the argument \"" + looking_argument + "\" in method \"" + this.methodname + "\".\n");
                        }
                    }
                } else if (methodname.matches("set[A-Z].*")) {
                    int size_param = md.getArguments().size();
                    if (size_param == 1) {
                        String argument = md.getArgument(0).toString();
                        String cut_field = methodname.split("set")[1].toLowerCase();
                        if (argument.equals(looking_argument) && cut_field.equals(argument)) {
                            boolean break_flag = false;
                            for(MethodDeclaration mine:dataStore.memory_classmethod.get(classname)) {
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
                            if (dataStore.memory_implement.get(classname) != null) {
                                for (String name_interface : dataStore.memory_implement.get(classname)) {
                                    warning_flag = check_ImplementMethod(name_interface, mode);
                                }
                            }
                            //extendsの確認、再帰関数使用
                            if (dataStore.memory_extend.get(classname) != null && !warning_flag) {
                                warning_flag = check_ExtendMethod(dataStore.memory_extend.get(classname), mode);
                            }
                            if(!break_flag || warning_flag) {

                                    setStacktext(md.getRange().get().toString());
                                    setStacktext("This code may give the following error after converting to Kotlin: val cannnot reassigned.");
                                    setStacktext("It is recommended to rename the argument \"" + looking_argument + "\" in method \"" + this.methodname + "\".\n");
                            }
                        }
                    }
                }
            }
        }
    }

    public static void check_initialize2(String classname){
        HashMap<String,HashMap<String,Object>> field_list2 = dataStore.memory_field_im.get(classname);
        if(field_list2 != null){
            setStacktext("start field checking\n");
            for(String fieldname : field_list2.keySet()){
                HashMap<String, Object> detail = field_list2.get(fieldname);
                if((boolean)detail.get("nullable")){
                    boolean flag = true;
                    List<ConstructorDeclaration> CdList = dataStore.memory_constructor.get(classname);
                    if(CdList != null) {
                        for (ConstructorDeclaration constructorDeclaration : CdList) {
                            check_constructor visitor = new check_constructor(classname,fieldname);
                            if(constructorDeclaration.accept(visitor, null) != null)
                                flag = constructorDeclaration.accept(visitor, null);
                        }
                    }
                    List<MethodDeclaration> MdList = dataStore.memory_classmethod.get(classname);
                    if(MdList != null) {
                        for (MethodDeclaration methodDeclaration: MdList) {
                            check_nullable visitor = new check_nullable(classname, fieldname);
                            methodDeclaration.accept(visitor, null);
                        }
                    }
                    List<FieldDeclaration> FdList = dataStore.memory_classfield.get(classname);
                    if(FdList != null && !(boolean)detail.get("nullable")) {
                        for (FieldDeclaration fieldDeclaration : FdList) {
                            check_nullable visitor = new check_nullable(classname, fieldname);
                            fieldDeclaration.accept(visitor, null);
                        }
                    }
                    if(flag && !(boolean)detail.get("nullable")){
                        if ((int)detail.get("IsType") != Integer.parseInt("0")) {
                            setStacktext(detail.get("range").toString());
                            setStacktext("Field \"" + fieldname + "\" doesn't have initializer.");

                            setStacktext("You should use modifer \"lateinit\" after convert to Kotlin.\n");
                            detail.put("need_fix",true);
                        }// else setStacktext("");
                    }
                }
            }
            setStacktext("finished field checking\n");
        }
    }

    public static void check_allparameter(String classname){
        HashMap<String,HashMap<String,Object>> field_list2 = dataStore.memory_field_im.get(classname);
        if(field_list2 != null){
            setStacktext("start parameter checking\n");
            for(String fieldname : field_list2.keySet()){
                HashMap<String, Object> detail = field_list2.get(fieldname);
                if(!(boolean)detail.get("nullable")){
                    List<MethodDeclaration> MdList = dataStore.memory_classmethod.get(classname);
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
            setStacktext("finished parameter checking\n");
        }
    }

    public static void check_initialize(String classname){
            List<FieldDeclaration> field_list = dataStore.memory_classfield.get(classname);
            if(field_list != null) {
                setStacktext("start field checking\n");
                for (FieldDeclaration field : field_list) {
                    int size = field.getVariables().size();
                    for (int i = 0; i < size; i++) {
                        if (!field.getVariable(i).getInitializer().isPresent()) {
                            boolean flag = true;
                            List<ConstructorDeclaration> CdList = dataStore.memory_constructor.get(classname);
                            if(CdList != null) {
                                for (ConstructorDeclaration constructorDeclaration : CdList) {
                                    check_constructor visitor = new check_constructor(classname, field.getVariable(i).getNameAsString());
                                    if(constructorDeclaration.accept(visitor, null) != null)
                                        flag = constructorDeclaration.accept(visitor, null);
                                }
                            }
                            if(flag){
                            setStacktext("line " + field.getRange().get().begin.line);
                            setStacktext("Field \"" + field.getVariable(i).getNameAsString()
                                    + "\" doesn't have initializer.");
                            if (field.getVariable(i).getType().isPrimitiveType())
                                setStacktext("You should use modifer \"not-null\" after convert to Kotlin\n");
                            else if (field.getVariable(i).getType().isReferenceType())
                                setStacktext("You should use modifer \"lateinit\" after convert to Kotlin\n");
                        }
                        }
                    }
                }
                setStacktext("finished field checking\n");
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
                dataStore.memory_field_im.get(classname).get(fieldname).put("nullable",true);
                dataStore.memory_field_im.get(classname).get(fieldname).put("need_fix",false);
                return false;
            }
            dataStore.memory_field_im.get(classname).get(fieldname).put("nullable",false);
            dataStore.memory_field_im.get(classname).get(fieldname).put("need_fix",true);
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
                        dataStore.memory_field_im.get(classname).get(fieldname).put("nullable",true);
                        dataStore.memory_field_im.get(classname).get(fieldname).put("need_fix",false);
                    } else flag = true;
                } else flag = true;
            } else flag = true;
            if(flag) {
                dataStore.memory_field_im.get(classname).get(fieldname).put("nullable", false);
                dataStore.memory_field_im.get(classname).get(fieldname).put("need_fix", true);
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
                    if((boolean)dataStore.memory_field_im.get(classname).get(fieldname).get("need_fix")){
                        setStacktext(range+"\nparameter:"+parameter+" will be changed nullable parameter after conversion.\n" +
                                "You should use @Notnull annotation.\n");
                    }
                }
            }

        }
    }

    public static boolean check_import(String checkname, boolean already){
        for(String classname:dataStore.memory_classname){
            if(classname.equals(checkname)){
                return false;
            }
        }
        if(!already)setStacktext("\""+checkname+"\" is probably a library. Please check the detail of library if necessary.\n");
        return true;
    }

    public static void fullcheck_import(String checkname){
        boolean flag = true;
        if(dataStore.memory_implement.get(checkname) != null) {
            for (String implement : dataStore.memory_implement.get(checkname)) {
                flag = true;
                for (String classname : dataStore.memory_classname) {
                    if (classname.equals(implement)) {
                        flag = false;
                        break;
                    }
                }
                if (flag && !duplicate_check(implement)) dataStore.memory_classlibrary.add(implement);
            }
        }
        flag = true;
        String extend = dataStore.memory_extend.get(checkname);
        for(String classname:dataStore.memory_classname){
            if(classname.equals(extend)) {
                flag = false;
            }
        }
        if(extend == null) flag = false;
        if(flag && !duplicate_check(extend)) dataStore.memory_classlibrary.add(extend);
    }

    public static boolean duplicate_check(String name){
        if(dataStore.memory_classlibrary != null) {
            for (String library : dataStore.memory_classlibrary) {
                if(library == null) break;
                if (library.equals(name)) return true;
            }
        }
        return false;
    }

}

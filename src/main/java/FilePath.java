import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The type File path.
 */
public class FilePath {
    private static final String TARGET_DIR_NAME = "src";

    /**
     * Define source file dir path list.
     *
     * @param myProject the my project
     * @return the list
     */
    public List<String> defineSourceFileDirPath(Project myProject) {
        String fileDirPath = "";

        List<String> projectFilePath = new ArrayList<String>();
        List<String> sourceFileDirPathList = new ArrayList<String>();
        List<String> sourceFileDirPathLists = new ArrayList<String>();
        String path = myProject.getBasePath();

        searchFile(path, ".java", projectFilePath);
        for (String filePath : projectFilePath) {
            fileDirPath = searchAnalysisDir(filePath);
            String[] sourcePath = fileDirPath.split("/");
            if (sourcePath[sourcePath.length - 1].equals(TARGET_DIR_NAME)) {
                sourceFileDirPathList.add(fileDirPath);
                sourceFileDirPathLists = new ArrayList<String>(new HashSet<>(sourceFileDirPathList));
            }
        }
        return sourceFileDirPathLists;
    }

    /**
     * Get file path in editor string.
     *
     * @param myProject the my project
     * @return the string
     */
    public String getFilePathInEditor(Project myProject){
        Editor editor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        if(editor != null) {
            Document document = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            String filePath = virtualFile.getPath();
            return filePath;
        }
        return null;
    }

    /**
     * Get file path string.
     *
     * @param dirPath  the dir path
     * @param fileName the file name
     * @return the string
     */
    public String getFilePath(String dirPath, String fileName){
        int i;
        String filePath = dirPath;
        String[] fileNamePath = fileName.split("/");
        String[] dirNamePath = dirPath.split("/");
        for(i = 0 ; fileNamePath[i].equals(dirNamePath[dirNamePath.length-1]); i++){
        }
        for(; i<fileNamePath.length;i++) {
            if(!fileNamePath[i].isEmpty()&&!fileNamePath[i].equals(dirNamePath[dirNamePath.length-1])){
                filePath = filePath + "/" + fileNamePath[i];
            }
        }
        filePath = filePath.replace(",","");
        return filePath;
    }

    private String compareDirPath(@NotNull List<String> dirPaths){
        String path = dirPaths.get(0);
        for(String dirPath:dirPaths){
            if(path.equals(dirPath)){
                path = dirPath;
            }else{
                String[] list1 = path.split("/");
                String[] list2 = dirPath.split("/");
                path = "";
                for (int i = 0; i < list1.length; i++ ){
                    if(!list1[i].equals(list2[i])){
                        break;
                    }else{
                        if(!list1[i].isEmpty()) {
                            path = path + "/" +list1[i];
                        }
                    }
                }
            }
        }
        return path;
    }

    private String searchAnalysisDir(String targetFilePath){
        String fileDirPath="";
        List<String> targetString = new ArrayList<>();
        targetString.add(TARGET_DIR_NAME);
        targetString.add("tests");
        targetString.add("Example");
        String[] splitStrs = targetFilePath.split("/");
        Boolean flag = true;

        for (int i = 0; i < splitStrs.length - 1; i++) {
            for(int n = 0; n < targetString.size(); n++){
                if (splitStrs[i].equals(targetString.get(n))) {
                    fileDirPath = fileDirPath + "/" + splitStrs[i];
                    flag = !flag;
                    break;
                }
            }
            if(!flag){
                break;
            }
            if (!splitStrs[i].isEmpty())
                fileDirPath = fileDirPath + "/" + splitStrs[i];
        }
        return fileDirPath;
    }

    private void searchFile(String path, String extension,List<String> projectfilePath) {
        File dir = new File(path);
        File files[] = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (files[i].isDirectory()) {
                searchFile(path + "/" + fileName, extension,projectfilePath);
            } else {
                if (fileName.endsWith(extension)) {
                    projectfilePath.add(path + "/" + fileName);
                }
            }
        }
    }
}

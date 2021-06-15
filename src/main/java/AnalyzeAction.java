import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

public class AnalyzeAction extends AnAction {

    public AnalyzeAction() {
        super("Check", "Edit target resource directory", AllIcons.General.Balloon);
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean active = vf != null && "java".equals(vf.getExtension());
        if(active){
            String data = "";
            if(DataStore.Check_Already_Analyze(vf.getName()))
                data = DataStore.Get_Analyzed(vf.getName());
            AnalyzerFactory.myToolWindow.setOutput(data);
        } else {
            e.getPresentation().setEnabledAndVisible(active);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
        //if(vf != null)CheckingGui.setClassname(vf.getName().split(".java")[0]);
        try {
            DataStore.init();
            VisitorTools.visitorTools(e.getProject());
            GetClassFromFile gcff = new GetClassFromFile(vf.getPath());
            VisitorTools.judge_warning(vf.getName(),gcff.getClassname());
            DataStore.Add_Analyzed(vf.getName(), VisitorTools.getStacktext());
            AnalyzerFactory.myToolWindow.setOutput(VisitorTools.getStacktext());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

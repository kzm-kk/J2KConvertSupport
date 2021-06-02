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
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if(vf != null)CheckingGui.setClassname(vf.getName().split(".java")[0]);
        try {
            DataStore.init();
            VisitorTools.visitorTools(e.getProject());
            VisitorTools.judge_warning(CheckingGui.getClassname());
            AnalyzerFactory.myToolWindow.setOutput(VisitorTools.getStacktext());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

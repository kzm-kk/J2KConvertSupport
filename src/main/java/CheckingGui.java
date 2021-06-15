import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;

public class CheckingGui extends SimpleToolWindowPanel {
    private static String classname = "";
    public static void setClassname(String name){
        classname = name;
    }
    public static String getClassname(){
        return classname;
    }
    private JPanel panel;
    private JTextArea output;


    public CheckingGui() {
        super(true,true);
        output.setEditable(false);
        setToolbar(createToolbarPanel());
        setContent(panel);
        DataStore.StartUp();
    }

    private JComponent createToolbarPanel() {
        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new AnalyzeAction());
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("AndroidDrawableViewer", actionGroup, true);
        return actionToolbar.getComponent();

    }

    public void setOutput(String text){
        output.setText(text);
    }
}
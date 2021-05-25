import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import javax.swing.*;
import java.io.IOException;

public class CheckingGui{
    private static String classname = "";
    public static void setClassname(String name){
        classname = name;
    }
    public static String getClassname(){
        return classname;
    }
    private JButton clearButton;
    private JPanel panel;
    private JTextArea output;
    private final VisitorTools analyzer = new VisitorTools();

    public CheckingGui(Project project,ToolWindow toolWindow) {
        output.setEditable(false);

        clearButton.addActionListener(e -> {
            try {
                dataStore.init();
                VisitorTools.visitorTools(project);
                analyzer.judge_warning(getClassname());
                output.setText(analyzer.getStacktext());

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }


    public JPanel getContent() {
        return panel;
    }



}


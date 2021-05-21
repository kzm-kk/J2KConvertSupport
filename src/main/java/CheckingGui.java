import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckingGui{
    private JButton clearButton;
    private JPanel panel;
    private JTextArea output;
    private Project project;
    private VisitorTools analyzer = new VisitorTools();

    public CheckingGui(Project project,ToolWindow toolWindow) {
        this.project = project;
        output.setEditable(false);

        clearButton.addActionListener(e -> {
            try {
                analyze();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        /*try {
            analyzer.visitorTools(project);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }*/
    }

    public void analyze() throws IOException {
        analyzer.visitorTools(project);
        analyzer.judge_warning(dataStore.classname);
        output.setText(analyzer.getStacktext());
    }

    public void analyze2(){
        analyzer.judge_warning(dataStore.classname);
        output.setText(analyzer.getStacktext());
    }

    public JPanel getContent() {
        return panel;
    }



}


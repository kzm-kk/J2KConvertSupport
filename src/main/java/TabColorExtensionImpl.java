import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class TabColorExtensionImpl implements EditorTabColorProvider {
    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String defaultExtension = virtualFile.getFileType().getDefaultExtension();
        if ("txt".equals(defaultExtension)) {
            return JBColor.GREEN;
        } else if ("xml".equals(defaultExtension)) {
            return JBColor.BLUE;
        } else if ("java".equals(defaultExtension)) {
            dataStore.classname = virtualFile.getName();
            return JBColor.YELLOW;
        }
        return null;
    }
}

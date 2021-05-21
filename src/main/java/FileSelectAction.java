import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

public class FileSelectAction extends AnAction {
    // メニュー表示/非表示の制御
    @Override
    public void update(AnActionEvent event) {
        VirtualFile vf = event.getData(CommonDataKeys.VIRTUAL_FILE);
        // ここでは、拡張子「py」の場合のみメニューを表示している
        boolean active = vf != null && "java".equals(vf.getExtension());
        event.getPresentation().setEnabledAndVisible(active);
    }

    // メニューを選択した時の処理
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project currentProject = event.getProject();
        VirtualFile vf = event.getData(CommonDataKeys.VIRTUAL_FILE);
        String fullPath = vf.getCanonicalPath();
        String basePath = currentProject.getBasePath();
        String filePath = fullPath.substring(basePath.length());

        // 処理を実施して良いかの確認ダイアログ表示
        int rtn = Messages.showOkCancelDialog(currentProject,
                String.format("%sに対して処理をします", vf.getName()),
                "ファイル対する処理", "Ok", "Cancel", Messages.getQuestionIcon());

        if (rtn == Messages.OK) {
            // TODO: ここにファイルに対するなんらかの処理を実装

            // 処理完了を通知
            final Notification notification = new Notification(
                    "FileSelectActionNotification",
                    "ファイル対する処理",
                    String.format("%sに対する処理を完了しました", filePath),
                    NotificationType.INFORMATION);
            notification.notify(currentProject);
        }
    }
}

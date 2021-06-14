import com.github.javaparser.Range;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;

public class FixMarker {
    Project project;
    FilePath filePath;
    public FixMarker(Project project,FilePath filePath){
        this.project = project;
        this.filePath = filePath;
    }

    public void ResetMarker(){
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(editor != null)
            editor.getMarkupModel().removeAllHighlighters();
    }

    public void LineMarker(Range range){
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        Document document = editor.getDocument();
        TextAttributes attr = new TextAttributes();
        attr.setBackgroundColor(JBColor.CYAN);

        int start = positionDecision(document, range.begin.line-1, range.begin.column-1);
        int end = positionDecision(document, range.end.line-1, range.end.column);

        if (start != end) {
            RangeHighlighter rangeHighlighter = editor.getMarkupModel().addRangeHighlighter(start, end, HighlighterLayer.FIRST, attr, HighlighterTargetArea.EXACT_RANGE);
            rangeHighlighter.setErrorStripeMarkColor(JBColor.BLUE);
        }
    }

    private int positionDecision(Document document, int lineNumber, int position) {
        if (position < 0) {
            position = document.getLineEndOffset(lineNumber);
        } else {
            position = document.getLineStartOffset(lineNumber) + position;
        }
        return position;
    }

}

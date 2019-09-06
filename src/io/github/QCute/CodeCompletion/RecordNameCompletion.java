package io.github.QCute.CodeCompletion;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import java.util.regex.*;

public class RecordNameCompletion extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        try {
            // whole text document
            Document document = editor.getDocument();
            // line start
            int startOffset = editor.getCaretModel().getVisualLineStart();
            // current cursor
            int currentOffset = editor.getCaretModel().getOffset();
            // line end
            int endOffset = editor.getCaretModel().getVisualLineEnd();
            // cut line text
            String line = document.getText(new TextRange(startOffset, endOffset));
            // convert field to variable
            final Pair<Integer, String> insert = getRecordName(line, currentOffset - startOffset);
            if (insert.getFirst() != -1) {
                // insert code
                WriteCommandAction.runWriteCommandAction(project, () -> document.insertString(insert.getFirst() + startOffset, insert.getSecond()));
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    private static Pair<Integer, String> getRecordName(String text, int currentOffset) {
        // in-line
        Matcher recordMatcher = Pattern.compile("#\\w+\\{.*?\\}").matcher(text);
        while(recordMatcher.find()) {
            // extract record name
            if ((recordMatcher.start() <= currentOffset) && (currentOffset <= recordMatcher.end())) {
                String group = recordMatcher.group();
                String snakeName = group.substring(group.indexOf("#") + 1, group.indexOf("{"));
                String pascalName = snakeCaseToPascalCase(snakeName);
                Matcher nameMatcher = Pattern.compile(pascalName + "\\s*=\\s*#" + snakeName + "\\{.*?\\}").matcher(text);
                if (!nameMatcher.find()) {
                    return new Pair<>(recordMatcher.start(), pascalName + " = ");
                } else {
                    return new Pair<>(-1, "");
                }
            }
        }

        // multi-line
        recordMatcher = Pattern.compile("#\\w+\\{").matcher(text);
        while(recordMatcher.find()) {
            // extract record name
            if ((recordMatcher.start() <= currentOffset) && (currentOffset <= recordMatcher.end())) {
                String group = recordMatcher.group();
                String snakeName = group.substring(group.indexOf("#") + 1, group.indexOf("{"));
                String pascalName = snakeCaseToPascalCase(snakeName);
                Matcher nameMatcher = Pattern.compile(pascalName + "\\s*=\\s*#" + snakeName + "\\{").matcher(text);
                if (!nameMatcher.find()) {
                    return new Pair<>(recordMatcher.start(), pascalName + " = ");
                } else {
                    return new Pair<>(-1, "");
                }
            }
        }


        return new Pair<>(-1, "");
    }

    private static String snakeCaseToPascalCase(String in) {
        StringBuilder result = new StringBuilder();
        String[] tokens = in.split("_");
        for (String token : tokens) {
            if (token.length() >= 1) {
                result.append(token.substring(0, 1).toUpperCase()).append(token.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }
}

package io.github.QCute.RecordCompletion;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;

public class RecordCompletion extends AnAction {

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
            // cut line text
            String line = document.getText(new TextRange(startOffset, currentOffset));
            // convert field to variable
            final String insert = parse(line);
            if (!insert.equals("")) {
                // insert code
                WriteCommandAction.runWriteCommandAction(project, () -> document.insertString(currentOffset, insert));
            }
            // move cursor
            editor.getCaretModel().moveToOffset(currentOffset + insert.length());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static String parse(String text) {
        int bracket = text.lastIndexOf("{");
        int comma = text.lastIndexOf(",");
        int index = Math.max(bracket, comma);
        if (index == -1 && !text.toLowerCase().equals(text)) return "";
        // cut string
        String expr = text.substring(index + 1);
        // blank
        if (expr.trim().isEmpty()) return "";
        // split with assignment
        String [] tokens = expr.split("=");
        if (tokens.length == 1) {
            StringBuilder result = new StringBuilder();
            // field right add space
            if (!tokens[0].substring(tokens[0].length() - 1).equals(" ")) {
                result.append(" ");
            }
            // no = add =
            if (!expr.contains("=")) {
                result.append("=");
            }
            String field = tokens[0].replace(" ", "");
            return result.append(" ").append(snakeCaseToPascalCase(field)).toString();
        } else if (tokens.length == 2 && tokens[1].equals(" ")) {
            String field = tokens[0].replace(" ", "");
            return snakeCaseToPascalCase(field);
        } else {
            return "";
        }
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

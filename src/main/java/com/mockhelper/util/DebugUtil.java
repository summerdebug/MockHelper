package com.mockhelper.util;

import com.intellij.debugger.actions.DebuggerAction;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator.XEvaluationCallback;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValue;
import com.sun.jdi.Value;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

/**
 * Works with debug session.
 */
public class DebugUtil {

    private DebugUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    public static XDebugSession getSession(AnActionEvent e) {
        DebuggerContextImpl debuggerContext = DebuggerAction.getDebuggerContext(e.getDataContext());
        return Objects.requireNonNull(debuggerContext.getDebuggerSession()).getXDebugSession();
    }

    public static String getExpression(AnActionEvent e, XDebugSession session) {
        XDebuggerEvaluator evaluator = session.getDebugProcess().getEvaluator();
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        String selectedText = editor != null ? editor.getSelectionModel().getSelectedText() : null;
        if (selectedText != null) {
            selectedText = Objects.requireNonNull(evaluator).formatTextForEvaluation(selectedText);
        }
        Promise<String> expressionTextPromise = Promise.resolve(selectedText);
        if (selectedText == null && editor != null) {
            expressionTextPromise = getExpressionText(evaluator, CommonDataKeys.PROJECT.getData(e.getDataContext()),
                    editor);
        }
        try {
            return expressionTextPromise.blockingGet(1000);
        } catch (TimeoutException | ExecutionException ignored) {
            return null;
        }
    }

    private static Promise<String> getExpressionText(@Nullable XDebuggerEvaluator evaluator, @Nullable Project project,
            @NotNull Editor editor) {
        if (project == null || evaluator == null) {
            return Promise.resolve(null);
        }

        Document document = editor.getDocument();
        Promise<ExpressionInfo> expressionInfoPromise = evaluator
                .getExpressionInfoAtOffsetAsync(project, document, editor.getCaretModel().getOffset(), true);
        return expressionInfoPromise.then(expressionInfo -> PsiManipulationUtil
                .getExpressionText(expressionInfo, document));
    }

    public static String evaluateExpression(XDebugSession session, String expression) {
        XStackFrame frame = session.getCurrentStackFrame();
        XDebuggerEvaluator evaluator = Objects.requireNonNull(frame).getEvaluator();
        WaitEvaluationCallBack waitEvaluationCallBack = new WaitEvaluationCallBack();
        Objects.requireNonNull(evaluator).evaluate(expression, waitEvaluationCallBack, session.getCurrentPosition());

        int cycles = 0;
        while (!waitEvaluationCallBack.isDone()) {
            try {
                cycles++;
                Thread.sleep(100);
            } catch (Exception ignored) {
                // Ignore exception.
            }
            if (cycles >= 1000) {
                break;
            }
        }

        return waitEvaluationCallBack.getResultValue();
    }

    static class WaitEvaluationCallBack implements XEvaluationCallback {

        private boolean done;

        private String resultValue;

        @Override
        public void evaluated(@NotNull XValue result) {
            done = true;

            JavaValue javaValue = (JavaValue) result;
            ValueDescriptorImpl descriptor = javaValue.getDescriptor();
            Value value = descriptor.getValue();

            resultValue = value.toString();
        }

        boolean isDone() {
            return done;
        }

        String getResultValue() {
            return resultValue;
        }

        @Override
        public void errorOccurred(@NotNull String errorMessage) {
            // Log error message.
        }
    }
}

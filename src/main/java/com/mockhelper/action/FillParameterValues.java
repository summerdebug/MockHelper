package com.mockhelper.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.ParamUtil;
import com.mockhelper.util.PsiManipulationUtil;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Fills values of the parameters of method or constructor call. To invoke, put mouse pointer to the method call and
 * press Ait + P. It will fill method call with parameter values. For primitive types default values used. For classes
 * mocked fields used. If appropriate mocked field doesn't exist, it will be created.
 */
public class FillParameterValues extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiElement selectedElement = e.getData(LangDataKeys.PSI_ELEMENT);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();
        if (selectedElement instanceof PsiMethod) {
            PsiMethod selectedMethod = (PsiMethod) selectedElement;
            PsiParameter[] params = selectedMethod.getParameterList().getParameters();
            String paramValues = ParamUtil.getParamValues(psiFile, project, params);
            PsiElement elementAtCaret = PsiManipulationUtil.getPsiElementAtCaret(e);
            PsiStatement statementAtCaret = PsiTreeUtil.getParentOfType(elementAtCaret, PsiStatement.class);
            String replacement = createReplacementStatementStr(selectedMethod, paramValues, statementAtCaret);
            CodeUpdateUtil.replaceStatement(project, statementAtCaret, replacement);
        }
    }

    @NotNull
    private String createReplacementStatementStr(PsiMethod selectedMethod, String paramValues,
            PsiStatement statementAtCaret) {
        if (statementAtCaret instanceof PsiExpressionStatement) {
            return getNewMethodInvocationStr(selectedMethod, paramValues, statementAtCaret);
        } else if (statementAtCaret instanceof PsiDeclarationStatement) {
            return getNewConstructorInvocationStr(selectedMethod, paramValues, statementAtCaret);
        } else {
            throw new IllegalStateException("Not supported statement: " + statementAtCaret.getClass());
        }
    }

    @NotNull
    private String getNewMethodInvocationStr(PsiMethod selectedMethod, String paramValues,
            PsiStatement statementAtCaret) {
        String varName = statementAtCaret.getText().substring(0, statementAtCaret.getText().indexOf('.'));
        String resultType = Objects.requireNonNull(selectedMethod.getReturnType()).getCanonicalText();
        return (!resultType.equals("void") ? resultType + " result = " : "") + varName
                + "." + selectedMethod.getName() + "(" + paramValues + ");";
    }

    @NotNull
    private String getNewConstructorInvocationStr(PsiMethod selectedMethod, String paramValues,
            PsiStatement statementAtCaret) {
        String statementStr = statementAtCaret.getText();
        String constructorStr = "new " + selectedMethod.getName() + "(";
        int paramsStart = statementStr.indexOf(constructorStr) + constructorStr.length();
        String declarationStart = statementStr.substring(0, paramsStart);
        return declarationStart + paramValues + ");";
    }
}

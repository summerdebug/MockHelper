package com.mockhelper.action;

import com.intellij.debugger.actions.DebuggerAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.xdebugger.XDebugSession;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.DebugUtil;
import com.mockhelper.util.ParamUtil;
import com.mockhelper.util.PsiManipulationUtil;
import com.mockhelper.util.TestUtil;
import com.mockhelper.util.result.ResultUtil;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Implements mock of the method in the test. To invoke, put mouse pointer to class and press Alt + M. Prerequisites:
 * the mockito should be available in classpath and the test class should already exist and have name
 * <TestedClass>Test.
 */
public class MockMethod extends DebuggerAction {

    private boolean thisMethodInvoked;
    private PsiFile thisPsiFile;

    @Override
    public void actionPerformed(AnActionEvent e) {
        thisPsiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiFile testPsiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();
        XDebugSession session = DebugUtil.getSession(e);
        String expression = DebugUtil.getExpression(e, session);
        validateBaseObject(session, expression);
        String[] params = ParamUtil.getParams(Objects.requireNonNull(expression));
        PsiMethod methodToMock = PsiManipulationUtil.getSelectedMethod(e);
        String baseObject = PsiManipulationUtil.getBaseObject(Objects.requireNonNull(methodToMock));
        String resultToReturn = ResultUtil.getResultToReturn(testPsiFile, methodToMock, project);
        String paramValues = ParamUtil.getParamValues(testPsiFile, session, params, methodToMock, project);
        PsiStatement mockStatement = getMockStatement(methodToMock, baseObject, resultToReturn, paramValues,
                project);
        PsiMethod testMethod = TestUtil.getLastTestMethod(testPsiFile, project);
        PsiComment actComment = TestUtil.findActComment(testMethod);
        CodeUpdateUtil.addMockStatement(mockStatement, testMethod, actComment, project);
    }

    private void validateBaseObject(XDebugSession session, String expression) {
        String objectName = getObjectName(expression);
        if (thisMethodInvoked) {
            return;
        }

        String objectInstance = DebugUtil.evaluateExpression(session, objectName);
        boolean isMock = objectInstance.contains("EnhancerByMockitoWithCGLIB");

        boolean isTestedObject = isTestedObject(expression);

        if (!isMock && !isTestedObject) {
            throw new IllegalStateException("Method is invoked not on mocked object.");
        }
    }

    @NotNull
    private String getObjectName(String expression) {
        int dotIndex = expression.indexOf('.');
        int openBraceIndex = expression.indexOf('(');
        if (dotIndex < 0 || dotIndex > openBraceIndex) {
            thisMethodInvoked = true;
            return getThisVarName();
        }
        thisMethodInvoked = false;

        return expression.substring(0, dotIndex);
    }

    @NotNull
    private String getThisVarName() {
        String fileName = thisPsiFile.getName();
        String className = fileName.substring(0, fileName.lastIndexOf(".java"));
        return ("" + className.charAt(0)).toLowerCase() + className.substring(1);
    }

    private boolean isTestedObject(String expression) {
        int dotIndex = expression.indexOf('.');
        int openBraceIndex = expression.indexOf('(');
        return dotIndex < 0 || dotIndex > openBraceIndex;
    }

    @NotNull
    private PsiStatement getMockStatement(PsiMethod psiMethod, String mockedObject, String result,
            String paramValues, Project project) {
        String mockMethodStr = getMockStr(mockedObject, psiMethod, result, paramValues);
        return PsiManipulationUtil.createPsiStatement(project, mockMethodStr);
    }

    private String getMockStr(String mockedObject, PsiMethod psiMethod, String result, String paramValues) {
        if (mockedObject.equals(getThisVarName())) {
            return "org.mockito.Mockito.doReturn(" + result + ").when(" + mockedObject + ")." + psiMethod.getName() +
                    paramValues + ";";
        }
        return "org.mockito.Mockito.when(" + mockedObject + "." + psiMethod.getName() + paramValues + ").thenReturn("
                + result + ");";
    }
}

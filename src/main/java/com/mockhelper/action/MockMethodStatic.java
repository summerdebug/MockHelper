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
import com.mockhelper.util.MockClassStaticUtil;
import com.mockhelper.util.ParamUtil;
import com.mockhelper.util.PsiManipulationUtil;
import com.mockhelper.util.TestUtil;
import com.mockhelper.util.result.ResultUtil;
import java.util.Objects;

/**
 * Implements mock of the static method using PowerMockito in the test. To invoke, put mouse pointer to class and press
 * Alt + O. Prerequisites: the power mockito should be available in classpath and the test class should already exist
 * and have name <TestedClass>Test.
 */
public class MockMethodStatic extends DebuggerAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        MockClassStaticUtil.mockClassStaticallyMethodIsSelected(e);

        PsiFile testPsiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();
        XDebugSession session = DebugUtil.getSession(e);
        String expression = DebugUtil.getExpression(e, session);
        PsiMethod methodToMock = PsiManipulationUtil.getSelectedMethod(e);
        String baseClass = PsiManipulationUtil.getBaseClass(Objects.requireNonNull(methodToMock));
        String[] params = ParamUtil.getParams(Objects.requireNonNull(expression));
        String paramValues = ParamUtil.getParamValues(testPsiFile, session, params, methodToMock, project);
        String resultToReturn = ResultUtil.getResultToReturn(testPsiFile, methodToMock, project);
        PsiStatement mockStatement = getMockStatement(methodToMock, baseClass, resultToReturn, paramValues, project);
        PsiMethod testMethod = TestUtil.getLastTestMethod(testPsiFile, project);
        PsiComment actComment = TestUtil.findActComment(testMethod);
        CodeUpdateUtil.addMockStatement(mockStatement, testMethod, actComment, project);
    }

    private PsiStatement getMockStatement(PsiMethod psiMethod, String mockedObject, String result,
            String paramValues, Project project) {
        String mockMethodStr = getMockStr(mockedObject, psiMethod, result, paramValues);
        return PsiManipulationUtil.createPsiStatement(project, mockMethodStr);
    }

    private String getMockStr(String mockedObject, PsiMethod psiMethod, String result, String paramValues) {
        return "org.powermock.api.mockito.PowerMockito.when(" + mockedObject + "." + psiMethod.getName() + paramValues
                + ").thenReturn(" + result + ");";
    }
}

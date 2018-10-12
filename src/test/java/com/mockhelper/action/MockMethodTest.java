package com.mockhelper.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({DebugUtil.class, PsiManipulationUtil.class, ResultUtil.class, ParamUtil.class, TestUtil.class,
        CodeUpdateUtil.class})
@RunWith(PowerMockRunner.class)
public class MockMethodTest {

    private static final String TEST = "test";
    @Mock
    private AnActionEvent anActionEvent;
    @Mock
    private PsiFile psiFile;
    @Mock
    private Project project;
    @Mock
    private XDebugSession xDebugSession;
    @Mock
    private PsiMethod psiMethod;
    @Mock
    private PsiStatement psiStatement;
    @Mock
    private PsiComment psiComment;

    @Test
    public void givenMockMethodWhenActionPerformedThenSuccessful() {
        // Arrange
        MockMethod mockMethod = new MockMethod();
        when(anActionEvent.getData(CommonDataKeys.PSI_FILE)).thenReturn(psiFile);
        when(anActionEvent.getProject()).thenReturn(project);
        mockStatic(DebugUtil.class, PsiManipulationUtil.class,
                ResultUtil.class, ParamUtil.class,
                TestUtil.class, CodeUpdateUtil.class);
        PowerMockito.when(DebugUtil.getSession(anActionEvent)).thenReturn(xDebugSession);
        PowerMockito.when(DebugUtil.getExpression(anActionEvent, xDebugSession)).thenReturn("test.test(test);");
        PowerMockito.when(DebugUtil.evaluateExpression(xDebugSession, TEST)).thenReturn("EnhancerByMockitoWithCGLIB");
        PowerMockito.when(PsiManipulationUtil.getSelectedMethod(anActionEvent)).thenReturn(psiMethod);
        PowerMockito.when(PsiManipulationUtil.getBaseObject(psiMethod)).thenReturn(TEST);
        PowerMockito.when(ResultUtil.getResultToReturn(psiFile, psiMethod, project)).thenReturn(TEST);
        PowerMockito.when(ParamUtil
                .getParamValues(eq(psiFile), eq(xDebugSession), any(String[].class), eq(psiMethod), eq(project)))
                .thenReturn(TEST);
        String[] strings = new String[1];
        strings[0] = TEST;
        PowerMockito.when(ParamUtil.getParams("test.test(test);")).thenReturn(strings);
        when(psiFile.getName()).thenReturn("Test.java");
        PowerMockito.when(PsiManipulationUtil
                .createPsiStatement(project, "org.mockito.Mockito.doReturn(test).when(test).nulltest;"))
                .thenReturn(psiStatement);
        PowerMockito.when(TestUtil.getLastTestMethod(psiFile, project)).thenReturn(psiMethod);
        PowerMockito.when(TestUtil.findActComment(psiMethod)).thenReturn(psiComment);

        // Act
        mockMethod.actionPerformed(anActionEvent);

        // Assert
        verify(anActionEvent, times(2)).getData(CommonDataKeys.PSI_FILE);
        verify(anActionEvent).getProject();
        verify(psiFile).getName();

        PowerMockito.verifyStatic();
        DebugUtil.getSession(anActionEvent);
        DebugUtil.getExpression(anActionEvent, xDebugSession);
        DebugUtil.evaluateExpression(xDebugSession, TEST);
        PsiManipulationUtil.getSelectedMethod(anActionEvent);
        PsiManipulationUtil.getBaseObject(psiMethod);
        ResultUtil.getResultToReturn(psiFile, psiMethod, project);
        ParamUtil.getParamValues(eq(psiFile), eq(xDebugSession), any(String[].class), eq(psiMethod), eq(project));
        ParamUtil.getParams("test.test(test);");
        PsiManipulationUtil.createPsiStatement(project, "org.mockito.Mockito.doReturn(test).when(test).nulltest;");
        TestUtil.getLastTestMethod(psiFile, project);
        TestUtil.findActComment(psiMethod);
    }
}

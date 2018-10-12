package com.mockhelper.action;

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
import com.mockhelper.util.MockClassStaticUtil;
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

@PrepareForTest({MockClassStaticUtil.class, AnActionEvent.class, DebugUtil.class,
        PsiManipulationUtil.class, ParamUtil.class,
        ResultUtil.class, TestUtil.class,
        CodeUpdateUtil.class})
@RunWith(PowerMockRunner.class)
public class MockMethodStaticTest {

    private static final String TEST = "test";
    private String[] strings;
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
    public void givenMockMethodStaticWhenActionPerformedThenSuccessful() {
        // Arrange
        MockMethodStatic mockMethodStatic = new MockMethodStatic();
        mockStatic(MockClassStaticUtil.class, AnActionEvent.class, DebugUtil.class, PsiManipulationUtil.class,
                ParamUtil.class, ResultUtil.class, TestUtil.class, CodeUpdateUtil.class);
        when(anActionEvent.getData(CommonDataKeys.PSI_FILE)).thenReturn(psiFile);
        when(anActionEvent.getProject()).thenReturn(project);
        PowerMockito.when(DebugUtil.getSession(anActionEvent)).thenReturn(xDebugSession);
        PowerMockito.when(DebugUtil.getExpression(anActionEvent, xDebugSession)).thenReturn(TEST);
        PowerMockito.when(PsiManipulationUtil.getSelectedMethod(anActionEvent)).thenReturn(psiMethod);
        PowerMockito.when(PsiManipulationUtil.getBaseClass(psiMethod)).thenReturn(TEST);
        strings = new String[1];
        strings[0] = TEST;
        PowerMockito.when(ParamUtil.getParams(TEST)).thenReturn(strings);
        PowerMockito.when(ParamUtil.getParamValues(psiFile, xDebugSession, strings, psiMethod, project)).thenReturn(
                TEST);
        PowerMockito.when(ResultUtil.getResultToReturn(psiFile, psiMethod, project)).thenReturn(TEST);
        PowerMockito.when(PsiManipulationUtil.createPsiStatement(project,
                "org.powermock.api.mockito.PowerMockito.when(test.nulltest).thenReturn(test);"))
                .thenReturn(psiStatement);
        PowerMockito.when(TestUtil.getLastTestMethod(psiFile, project)).thenReturn(psiMethod);
        PowerMockito.when(TestUtil.findActComment(psiMethod)).thenReturn(psiComment);

        // Act
        mockMethodStatic.actionPerformed(anActionEvent);

        // Assert
        verify(anActionEvent).getData(CommonDataKeys.PSI_FILE);
        verify(anActionEvent).getProject();

        PowerMockito.verifyStatic();
        DebugUtil.getSession(anActionEvent);
        DebugUtil.getExpression(anActionEvent, xDebugSession);
        PsiManipulationUtil.getSelectedMethod(anActionEvent);
        PsiManipulationUtil.getBaseClass(psiMethod);
        ParamUtil.getParams(TEST);
        ParamUtil.getParamValues(psiFile, xDebugSession, strings, psiMethod, project);
        ResultUtil.getResultToReturn(psiFile, psiMethod, project);
        PsiManipulationUtil.createPsiStatement(project, "org.powermock.api.mockito.test.nulltest).thenReturn(test);");
        TestUtil.getLastTestMethod(psiFile, project);
        TestUtil.findActComment(psiMethod);
    }
}

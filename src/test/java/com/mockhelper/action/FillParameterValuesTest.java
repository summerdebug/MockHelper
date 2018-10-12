package com.mockhelper.action;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.ParamUtil;
import com.mockhelper.util.PsiManipulationUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({ParamUtil.class, PsiManipulationUtil.class,
        PsiTreeUtil.class, CodeUpdateUtil.class})
@RunWith(PowerMockRunner.class)
public class FillParameterValuesTest {

    private PsiParameter[] psiParameters;
    @Mock
    private AnActionEvent anActionEvent;
    @Mock
    private PsiMethod psiMethod;
    @Mock
    private PsiFile psiFile;
    @Mock
    private Project project;
    @Mock
    private PsiParameterList psiParameterList;
    @Mock
    private PsiParameter psiParameter;
    @Mock
    private PsiElement psiElement;
    @Mock
    private PsiType psiType;
    @Mock
    private PsiExpressionStatement psiExpressionStatement;

    @Test
    public void givenFillParameterValuesWhenActionPerformedThenSuccessful() {
        // Arrange
        FillParameterValues fillParameterValues = new FillParameterValues();
        when(anActionEvent.getData(LangDataKeys.PSI_ELEMENT)).thenReturn(psiMethod);
        when(anActionEvent.getData(CommonDataKeys.PSI_FILE)).thenReturn(psiFile);
        when(anActionEvent.getProject()).thenReturn(project);
        when(psiMethod.getParameterList()).thenReturn(psiParameterList);
        psiParameters = new PsiParameter[1];
        psiParameters[0] = psiParameter;
        when(psiParameterList.getParameters()).thenReturn(psiParameters);
        mockStatic(ParamUtil.class, PsiManipulationUtil.class, PsiTreeUtil.class, CodeUpdateUtil.class);
        PowerMockito.when(ParamUtil.getParamValues(psiFile, project, psiParameters)).thenReturn("test");
        PowerMockito.when(PsiManipulationUtil.getPsiElementAtCaret(anActionEvent)).thenReturn(psiElement);
        PowerMockito.when(PsiTreeUtil.getParentOfType(psiElement, PsiStatement.class))
                .thenReturn(psiExpressionStatement);
        when(psiExpressionStatement.getText()).thenReturn("test.test");
        when(psiMethod.getReturnType()).thenReturn(psiType);
        when(psiType.getCanonicalText()).thenReturn("test");

        // Act
        fillParameterValues.actionPerformed(anActionEvent);

        // Assert
        verify(anActionEvent).getData(LangDataKeys.PSI_ELEMENT);
        verify(anActionEvent).getData(CommonDataKeys.PSI_FILE);
        verify(anActionEvent).getProject();
        verify(psiMethod).getParameterList();
        verify(psiParameterList).getParameters();
        verify(psiMethod).getReturnType();
        verify(psiType).getCanonicalText();

        PowerMockito.verifyStatic();
        ParamUtil.getParamValues(psiFile, project, psiParameters);
        PsiManipulationUtil.getPsiElementAtCaret(anActionEvent);
        PsiTreeUtil.getParentOfType(psiElement, PsiStatement.class);
    }

}

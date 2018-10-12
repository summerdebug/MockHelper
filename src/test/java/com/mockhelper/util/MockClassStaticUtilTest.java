package com.mockhelper.util;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({TestUtil.class, WriteCommandAction.class, PsiTreeUtil.class})
@RunWith(PowerMockRunner.class)
public class MockClassStaticUtilTest {

    private static final String TEST = "test";
    private Collection<PsiStatement> psiStatements = new ArrayList<>();
    @Mock
    private AnActionEvent anActionEvent;
    @Mock
    private PsiFile psiFile;
    @Mock
    private Project project;
    @Mock
    private PsiClass psiClass;
    @Mock
    private PsiAnnotation psiAnnotation;
    @Mock
    private PsiMethod psiMethod;
    @Mock
    private PsiStatement psiStatement;

    @Test
    public void givenMockClassStaticUtilGivenMockClassStaticallyThenSuccessful() {
        // Arrange
        when(anActionEvent.getData(CommonDataKeys.PSI_FILE)).thenReturn(psiFile);
        when(anActionEvent.getProject()).thenReturn(project);
        when(anActionEvent.getData(LangDataKeys.PSI_ELEMENT)).thenReturn(psiClass);
        mockStatic(TestUtil.class, WriteCommandAction.class, PsiTreeUtil.class);
        PowerMockito.when(TestUtil.getTestClass(psiFile, project)).thenReturn(psiClass);
        PsiAnnotation[] psiAnnotations = new PsiAnnotation[1];
        psiAnnotations[0] = psiAnnotation;
        when(psiClass.getAnnotations()).thenReturn(psiAnnotations);
        when(psiAnnotation.getText()).thenReturn(TEST);
        PowerMockito.when(TestUtil.getLastTestMethod(psiClass)).thenReturn(psiMethod);
        psiStatements.add(psiStatement);
        PowerMockito.when(PsiTreeUtil.findChildrenOfType(psiMethod, PsiStatement.class)).thenReturn(psiStatements);
        when(psiStatement.getText()).thenReturn(TEST);
        when(psiClass.getQualifiedName()).thenReturn(TEST);

        // Act
        MockClassStaticUtil.mockClassStatically(anActionEvent);

        // Assert
        verify(anActionEvent).getData(CommonDataKeys.PSI_FILE);
        verify(anActionEvent).getProject();
        verify(anActionEvent).getData(LangDataKeys.PSI_ELEMENT);
        verify(psiClass).getAnnotations();
        verify(psiAnnotation).getText();
        verify(psiStatement).getText();
        verify(psiClass, times(2)).getQualifiedName();

        PowerMockito.verifyStatic();
        TestUtil.getTestClass(psiFile, project);
        TestUtil.getLastTestMethod(psiClass);
        PsiTreeUtil.findChildrenOfType(psiMethod, PsiStatement.class);
    }
}

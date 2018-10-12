package com.mockhelper.action;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.mockhelper.util.PsiManipulationUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({PsiTreeUtil.class, PsiManipulationUtil.class, com.mockhelper.util.CodeUpdateUtil.class,
        WriteCommandAction.class, WriteCommandAction.class, JavaPsiFacade.class,
        GlobalSearchScope.class, PsiType.class})
@RunWith(PowerMockRunner.class)
public class ArrangeFieldsTest {

    private static final String TEST = "test";
    private static final String ORG_JUNIT_RULE = "org.junit.Rule";
    @Mock
    private AnActionEvent anActionEvent;
    @Mock
    private Project project;
    @Mock
    private PsiElement psiElement;
    @Mock
    private PsiClass psiClass;
    @Mock
    private PsiField psiField;
    @Mock
    private PsiModifierList psiModifierList;
    @Mock
    private PsiType psiType;
    @Mock
    private JavaPsiFacade javaPsiFacade;
    @Mock
    private GlobalSearchScope globalSearchScope;
    @Mock
    private PsiClassType psiClassType;
    @Mock
    private PsiElementFactory psiElementFactory;
    @Mock
    private PsiAnnotation psiAnnotation;


    @Test
    public void givenArrangeFieldsWhenActionPerformedThenSuccessful() {
        // Arrange
        ArrangeFields arrangeFields = new ArrangeFields();
        when(anActionEvent.getProject()).thenReturn(project);
        mockStatic(PsiManipulationUtil.class, PsiTreeUtil.class, com.mockhelper.util.CodeUpdateUtil.class,
                WriteCommandAction.class, JavaPsiFacade.class, GlobalSearchScope.class, PsiType.class);
        PowerMockito.when(PsiManipulationUtil.getPsiElementAtCaret(anActionEvent)).thenReturn(psiElement);
        PowerMockito.when(PsiTreeUtil.getParentOfType(psiElement, PsiClass.class)).thenReturn(psiClass);
        PsiField[] psiFields = new PsiField[1];
        psiFields[0] = psiField;
        when(psiClass.getFields()).thenReturn(psiFields);
        when(psiField.getModifierList()).thenReturn(psiModifierList);
        when(psiField.hasAnnotation(ORG_JUNIT_RULE)).thenReturn(true);
        when(psiField.getType()).thenReturn(psiType);
        when(psiType.getCanonicalText()).thenReturn(TEST);
        PsiAnnotation[] psiAnnotations = new PsiAnnotation[1];
        psiAnnotations[0] = psiAnnotation;
        when(psiModifierList.getAnnotations()).thenReturn(psiAnnotations);
        PowerMockito.when(JavaPsiFacade.getInstance(project)).thenReturn(javaPsiFacade);
        when(javaPsiFacade.getElementFactory()).thenReturn(psiElementFactory);
        PowerMockito.when(GlobalSearchScope.allScope(project)).thenReturn(globalSearchScope);
        PowerMockito.when(PsiType.getTypeByName(TEST, project, globalSearchScope)).thenReturn(psiClassType);
        when(psiElementFactory.createField(null, psiClassType)).thenReturn(psiField);

        // Act
        arrangeFields.actionPerformed(anActionEvent);

        // Assert
        verify(anActionEvent).getProject();
        verify(psiClass, times(5)).getFields();
        verify(psiField, times(8)).getModifierList();
        verify(psiField, times(5)).hasAnnotation(ORG_JUNIT_RULE);
        verify(psiField, times(2)).getType();
        verify(psiType, times(2)).getCanonicalText();
        verify(psiModifierList, times(2)).getAnnotations();
        verify(javaPsiFacade, times(2)).getElementFactory();
        verify(psiElementFactory, times(2)).createField(null, psiClassType);

        PowerMockito.verifyStatic();
        PsiManipulationUtil.getPsiElementAtCaret(anActionEvent);
        PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        JavaPsiFacade.getInstance(project);
        GlobalSearchScope.allScope(project);
        PsiType.getTypeByName(TEST, project, globalSearchScope);
    }

}

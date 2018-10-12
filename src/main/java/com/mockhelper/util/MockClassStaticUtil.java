package com.mockhelper.util;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class MockClassStaticUtil {

    private static final String MOCK_STATIC = "org.powermock.api.mockito.PowerMockito.mockStatic(";
    private static final String PREPARE_FOR_TEST = "org.powermock.core.classloader.annotations.PrepareForTest({";

    private MockClassStaticUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    /**
     * Implements static mock of the class using PowerMockito in the test. To invoke, put mouse pointer to class and
     * press Alt + K. Prerequisites: the power mockito should be available in classpath and the test class should
     * already exist and have name <TestedClass>Test.
     */
    public static void mockClassStatically(AnActionEvent e) {
        PsiFile testPsiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();
        PsiElement selectedClass = e.getData(LangDataKeys.PSI_ELEMENT);
        if (selectedClass instanceof PsiClass) {
            mockClassStatically(testPsiFile, project, (PsiClass) selectedClass);
        }
    }

    private static void mockClassStatically(PsiFile testPsiFile, Project project, PsiClass selectedClass) {
        if (selectedClass.hasModifier(JvmModifier.FINAL)) {
            Messages.showMessageDialog(project, "Cannot mock final class " + selectedClass.getQualifiedName(),
                    "MockHelper", Messages.getInformationIcon());
            return;
        }
        PsiClass testClass = TestUtil.getTestClass(testPsiFile, project);
        addPrepareForTestAnnotation(project, selectedClass, testClass);
        addMockStaticStatement(project, selectedClass, testClass);
    }

    private static void addPrepareForTestAnnotation(Project project, PsiClass psiClass, PsiClass testClass) {
        PsiAnnotation[] annotations = testClass.getAnnotations();
        PsiAnnotation prepareForTest = null;
        for (PsiAnnotation annotation : annotations) {
            String text = annotation.getText();
            if (text.startsWith("@PrepareForTest")) {
                prepareForTest = annotation;
                break;
            }
        }
        if (prepareForTest == null) {
            String qualifiedName = PREPARE_FOR_TEST + psiClass.getQualifiedName() + ".class})";
            Runnable addAnnotation = () -> Objects.requireNonNull(testClass.getModifierList())
                    .addAnnotation(qualifiedName);
            WriteCommandAction.runWriteCommandAction(project, addAnnotation);
        } else {
            String text = prepareForTest.getText();
            int start = text.indexOf('{');
            int end = text.indexOf('}');
            String classesStr = text.substring(start + 1, end);
            String newClassesStr = getUpdatedClassesStr(psiClass, classesStr);

            if (newClassesStr != null) {
                String annStr = PREPARE_FOR_TEST + newClassesStr + "})";
                final PsiAnnotation deleteAnnotation = prepareForTest;
                Runnable addAnnotation = () -> {
                    deleteAnnotation.delete();
                    Objects.requireNonNull(testClass.getModifierList()).addAnnotation(annStr);
                };
                WriteCommandAction.runWriteCommandAction(project, addAnnotation);
            }
        }
    }

    @Nullable
    private static String getUpdatedClassesStr(PsiClass psiClass, String classesStr) {
        String[] classesArray = classesStr.split(",");
        boolean found = false;
        for (int i = 0; i < classesArray.length; i++) {
            classesArray[i] = classesArray[i].trim();
            if ((psiClass.getQualifiedName() + ".class").contains(classesArray[i])) {
                found = true;
            }
        }
        return found ? null : classesStr + ", " + psiClass.getQualifiedName() + ".class";
    }

    private static void addMockStaticStatement(Project project, PsiClass classToMock, PsiClass testClass) {
        PsiMethod lastTestMethod = TestUtil.getLastTestMethod(testClass);
        Collection<PsiStatement> assignments = PsiTreeUtil.findChildrenOfType(lastTestMethod, PsiStatement.class);
        PsiStatement mockStaticStatement = null;
        for (PsiStatement psiStatement : assignments) {
            String text = psiStatement.getText();
            if (text.contains("mockStatic")) {
                mockStaticStatement = psiStatement;
                break;
            }
        }
        if (mockStaticStatement == null) {
            String mockStr = MOCK_STATIC + classToMock.getQualifiedName() + ".class);";
            TestUtil.addStatementBeforeActCommentInLastTestMethod(project, testClass, mockStr);
        } else {
            String mockStaticStr = mockStaticStatement.getText();
            String classToMockSimpleName = PsiManipulationUtil.getSimpleClassName(
                    Objects.requireNonNull(classToMock.getQualifiedName()));
            if (mockStaticStr.contains(classToMockSimpleName)) {
                return;
            }

            int start = mockStaticStr.indexOf('(');
            int end = mockStaticStr.indexOf(')');
            String classesStr = mockStaticStr.substring(start + 1, end);
            String newClassesStr = getUpdatedClassesStr(classToMock, classesStr);

            String newMockStatementStr = MOCK_STATIC + newClassesStr + ");";
            PsiStatement newMockStatement = PsiManipulationUtil.createPsiStatement(project, newMockStatementStr);
            final PsiStatement statementToReplace = mockStaticStatement;
            Runnable mockStaticRunnable = () -> statementToReplace.replace(newMockStatement);
            WriteCommandAction.runWriteCommandAction(project, mockStaticRunnable);
        }
    }

    public static void mockClassStaticallyMethodIsSelected(AnActionEvent e) {
        PsiFile testPsiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();

        PsiMethod methodToMock = PsiManipulationUtil.getSelectedMethod(e);
        PsiElement selectedClass = Objects.requireNonNull(methodToMock).getParent();
        if (selectedClass instanceof PsiClass) {
            mockClassStatically(testPsiFile, project, (PsiClass) selectedClass);
        }
    }
}

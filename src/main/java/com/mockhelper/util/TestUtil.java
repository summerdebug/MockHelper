package com.mockhelper.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestUtil {

    private TestUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    public static PsiMethod getLastTestMethod(PsiFile testPsiFile, Project project) {
        PsiClass testClass = getTestClass(testPsiFile, project);
        PsiMethod[] methods = testClass.getMethods();
        return methods[methods.length - 1];
    }

    public static PsiClass getTestClass(PsiFile testPsiFile, Project project) {
        PsiFile testFile = getTestFile(testPsiFile, project);
        return PsiTreeUtil.findChildOfType(testFile, PsiClass.class);
    }

    private static PsiFile getTestFile(PsiFile testPsiFile, Project project) {
        String testName = getTestFileName(testPsiFile);
        PsiFile[] testFiles = FilenameIndex.getFilesByName(project, testName, GlobalSearchScope.allScope(project));
        if (testFiles.length != 1) {
            testFiles = FilenameIndex
                    .getFilesByName(project, testPsiFile.getName(), GlobalSearchScope.allScope(project));
            if (testFiles.length != 1) {
                throw new IllegalStateException("Cannot find test file. " + testPsiFile.getName());
            }
        }
        return testFiles[0];
    }

    @NotNull
    private static String getTestFileName(PsiFile testPsiFile) {
        String fileName = testPsiFile.getName();
        return fileName.substring(0, fileName.lastIndexOf(".java")) + "Test.java";
    }

    static void addStatementBeforeActCommentInLastTestMethod(Project project, PsiClass testClass,
            String mockStr) {
        PsiStatement mockStatement = PsiManipulationUtil.createPsiStatement(project, mockStr);
        PsiMethod testMethod = getLastTestMethod(testClass);
        PsiComment actComment = findActComment(testMethod);
        CodeUpdateUtil.addMockStatement(mockStatement, testMethod, actComment, project);
    }

    static PsiMethod getLastTestMethod(PsiClass testClass) {
        PsiMethod[] testMethods = testClass.getMethods();
        return testMethods[testMethods.length - 1];
    }

    @NotNull
    public static PsiComment findActComment(PsiMethod method) {
        Collection<PsiComment> comments = PsiTreeUtil.findChildrenOfType(method, PsiComment.class);
        for (PsiComment comment : comments) {
            if (comment.getText().contains("// Act")) {
                return comment;
            }
        }
        throw new IllegalStateException("Act comment not found in " + method);
    }

    @Nullable
    public static PsiField findTestField(String fieldName, PsiClass testClass) {
        PsiField[] testFields = testClass.getFields();

        PsiField fieldFound = null;
        for (PsiField testField : testFields) {
            if (fieldName.equals(testField.getName())) {
                fieldFound = testField;
                break;
            }
        }
        return fieldFound;
    }

    public static boolean isFieldAlreadyInitialized(PsiField testField, PsiMethod method) {
        boolean initialized = false;
        Collection<PsiAssignmentExpression> assignments =
                PsiTreeUtil.findChildrenOfType(method, PsiAssignmentExpression.class);
        for (PsiAssignmentExpression assignment : assignments) {
            PsiExpression left = assignment.getLExpression();
            String leftText = left.getText();
            if (leftText.equals(testField.getName())) {
                initialized = true;
                break;
            }
        }
        return initialized;
    }

    @NotNull
    public static String getVariableName(String className) {
        int lastIndexOfDot = className.lastIndexOf('.');
        String simpleClassName = lastIndexOfDot > 0 ? className.substring(lastIndexOfDot + 1) :
                className;
        return ("" + simpleClassName.charAt(0)).toLowerCase() + simpleClassName.substring(1);
    }
}

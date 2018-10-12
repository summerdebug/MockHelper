package com.mockhelper.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Performs code changes.
 */
public class CodeUpdateUtil {

    private CodeUpdateUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    public static void addMockStatement(PsiStatement mockStatement, PsiMethod testMethod, PsiComment actComment,
            Project project) {
        Runnable addMockMethod = () -> testMethod.getLastChild().addBefore(mockStatement, actComment);
        WriteCommandAction.runWriteCommandAction(project, addMockMethod);
    }

    @NotNull
    public static PsiField getPsiField(PsiType returnType, String fieldName, PsiClass testClass,
            Project project) {
        PsiField testField = TestUtil.findTestField(fieldName, testClass);
        if (testField == null) {
            // Create a field.
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiField newField = factory.createField(fieldName, returnType);

            // Add field to the test class.
            Runnable addField = () -> testClass.add(newField);
            WriteCommandAction.runWriteCommandAction(project, addField);

            testField = newField;
        }
        return testField;
    }

    public static void replaceStatement(Project project, PsiStatement statementToReplace, String replacement) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiStatement replacementStatement = factory.createStatementFromText(replacement, null);
        Runnable addMockMethod = () -> statementToReplace.replace(replacementStatement);
        WriteCommandAction.runWriteCommandAction(project, addMockMethod);
    }

    public static void addModifierToField(Project project, PsiField field, String modifierStr) {
        Runnable changeModifier = () -> Objects.requireNonNull(field.getModifierList())
                .setModifierProperty(modifierStr, true);
        WriteCommandAction.runWriteCommandAction(project, changeModifier);
    }
}

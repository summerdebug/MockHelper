package com.mockhelper.util.result;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.PsiManipulationUtil;
import com.mockhelper.util.TestUtil;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResultUtil {

    private static final String ONE = "1";

    private ResultUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    public static String getResultToReturn(PsiFile testPsiFile, PsiMethod psiMethod, Project project) {
        PsiType returnType = psiMethod.getReturnType();
        return getValueForType(testPsiFile, project, returnType);
    }

    @Nullable
    public static String getValueForType(PsiFile testPsiFile, Project project, PsiType returnType) {
        String primitiveValue = getPrimitiveValue(returnType);
        if (primitiveValue != null) {
            return primitiveValue;
        }

        PsiClass returnTypeClass = PsiManipulationUtil.getPsiClass(returnType.getCanonicalText(), project);
        if (returnTypeClass != null && returnTypeClass.hasModifier(JvmModifier.FINAL)) {
            Messages.showMessageDialog(project, "Cannot mock final class " + returnType.getCanonicalText(),
                    "MockHelper", Messages.getInformationIcon());
            return null;
        }

        if (returnType instanceof PsiArrayType) {
            return getArrayResult(testPsiFile, returnType, project);
        }
        if (ListResultUtil.isCollectionListArrayList(returnType)) {
            return ListResultUtil.getCollectionListArrayListResult(testPsiFile, returnType, project);
        }
        if (MapResultUtil.isMapHashMap(returnType)) {
            return MapResultUtil.getMapHashMapResult(testPsiFile, returnType, project);
        }
        if (EnumResultUtil.isEnum(returnType)) {
            return EnumResultUtil.getEnumResult(returnType, project);
        }
        if (canBeMocked(returnType)) {
            return getMockedResult(testPsiFile, returnType, project);
        }

        return null;
    }

    @Nullable
    static String getPrimitiveValue(PsiType returnType) {
        if (returnType.getCanonicalText().equals(String.class.getCanonicalName())) {
            return "\"test\"";
        }
        if (PsiType.BYTE.equals(returnType) || returnType.getCanonicalText().equals(Byte.class.getCanonicalName())) {
            return "0";
        }
        if (PsiType.CHAR.equals(returnType) || returnType.getCanonicalText()
                .equals(Character.class.getCanonicalName())) {
            return "'a'";
        }
        String numericValue = getNumericValue(returnType);
        if (numericValue != null) {
            return numericValue;
        }
        if (PsiType.BOOLEAN.equals(returnType) || returnType.getCanonicalText()
                .equals(Boolean.class.getCanonicalName())) {
            return "true";
        }
        if (PsiType.VOID.equals(returnType)) {
            return "Something is wrong. Void return type.";
        }
        return null;
    }

    @Nullable
    private static String getNumericValue(PsiType returnType) {
        if (PsiType.DOUBLE.equals(returnType) || returnType.getCanonicalText()
                .equals(Double.class.getCanonicalName())) {
            return ONE;
        }
        if (PsiType.FLOAT.equals(returnType) || returnType.getCanonicalText().equals(Float.class.getCanonicalName())) {
            return ONE;
        }
        if (PsiType.INT.equals(returnType) || returnType.getCanonicalText().equals(Integer.class.getCanonicalName())) {
            return ONE;
        }
        if (PsiType.LONG.equals(returnType) || returnType.getCanonicalText().equals(Long.class.getCanonicalName())) {
            return "1L";
        }
        if (PsiType.SHORT.equals(returnType) || returnType.getCanonicalText().equals(Short.class.getCanonicalName())) {
            return ONE;
        }
        return null;
    }

    private static String getArrayResult(PsiFile testPsiFile, PsiType returnType, Project project) {
        String result;
        String arrayType = ((PsiArrayType) returnType).getComponentType().getCanonicalText();
        String fieldName = getFieldName(arrayType);

        // Find appropriate array field in test.
        PsiClass testClass = TestUtil.getTestClass(testPsiFile, project);
        PsiField testField = CodeUpdateUtil.getPsiField(returnType, fieldName, testClass, project);

        // Add array initialization to the last method in the test if it's not initialized in the method yet.
        PsiMethod[] methods = testClass.getMethods();
        PsiMethod method = methods[methods.length - 1];

        // Find place in test method where to put array initialization.
        PsiComment actComment = TestUtil.findActComment(method);

        boolean initialized = TestUtil.isFieldAlreadyInitialized(testField, method);
        if (!initialized) {
            InitUtil.initArray(testPsiFile, (PsiArrayType) returnType, arrayType, testField, method, actComment,
                    project);
        }

        result = testField.getName();
        return result;
    }

    @NotNull
    static String getFieldName(String arrayType) {
        String fieldNameCapitalized = arrayType.substring(arrayType.lastIndexOf('.') + 1) + "s";
        return fieldNameCapitalized.substring(0, 1).toLowerCase() + fieldNameCapitalized.substring(1);
    }

    static boolean canBeMocked(PsiType returnType) {
        boolean canBeMocked = !(returnType instanceof PsiArrayType)
                && !returnType.getCanonicalText().startsWith(Collection.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(List.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(ArrayList.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(LinkedList.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(Set.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(HashSet.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(Map.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(HashMap.class.getCanonicalName())
                && !returnType.getCanonicalText().startsWith(Hashtable.class.getCanonicalName());
        if (!canBeMocked) {
            return false;
        }
        try {
            Class clazz = Class.forName(returnType.getCanonicalText());
            return !Modifier.isFinal(clazz.getModifiers());
        } catch (Exception ignored) {
            return true;
        }
    }

    static String getMockedResult(PsiFile testPsiFile, PsiType returnType, Project project) {
        // Define field name by type.
        String typeCanonicalText = returnType.getCanonicalText();
        String fieldNameCapitalized = typeCanonicalText.substring(typeCanonicalText.lastIndexOf('.') + 1);
        fieldNameCapitalized = fieldNameCapitalized.substring(0, 1).toLowerCase() + fieldNameCapitalized.substring(1);

        // Find existing test field.
        PsiClass testClass = TestUtil.getTestClass(testPsiFile, project);
        PsiField testField = TestUtil.findTestField(fieldNameCapitalized, testClass);

        // If field not exists, append it.
        if (testField == null) {
            // Create a field.
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiField newField = factory.createField(fieldNameCapitalized, returnType);
            Objects.requireNonNull(newField.getModifierList()).addAnnotation("org.mockito.Mock");

            // Add field to the test class.
            Runnable addField = () -> testClass.add(newField);
            WriteCommandAction.runWriteCommandAction(project, addField);

            testField = newField;
        }
        return testField.getName();
    }
}

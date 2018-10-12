package com.mockhelper.util.result;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.TestUtil;
import java.util.Objects;

class InitUtil {

    private InitUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    static void initMap(PsiFile testPsiFile, String keyType, String valueType,
            PsiField testField,
            PsiMethod method,
            Project project) {
        // Find place in test method where to put array initialization.
        PsiComment actComment = TestUtil.findActComment(method);

        // Add collection initialization statement.
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        String mapInitStr;
        if (keyType != null && valueType != null) {
            mapInitStr = testField.getName() + " = new java.util.HashMap<" + keyType + "," + valueType + ">();";
        } else {
            mapInitStr = testField.getName() + " = new java.util.HashMap();";
        }

        PsiStatement colInit = factory.createStatementFromText(mapInitStr, null);

        CodeUpdateUtil.addMockStatement(colInit, method, actComment, project);

        // Add element to Map.
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);

        PsiType keyPsiType = PsiType.getTypeByName(Objects.requireNonNull(keyType), project, scope);
        PsiType valuePsiType = PsiType.getTypeByName(Objects.requireNonNull(valueType), project, scope);
        String keyElement = getElementToAdd(testPsiFile, keyPsiType, project);
        String valueElement = getElementToAdd(testPsiFile, valuePsiType, project);
        String addElementStr = testField.getName() + ".put(" + keyElement + "," + valueElement + ");";
        PsiStatement putElement = factory.createStatementFromText(addElementStr, null);
        CodeUpdateUtil.addMockStatement(putElement, method, actComment, project);
    }

    private static String getElementToAdd(PsiFile testPsiFile, PsiType paramPsiType, Project project) {
        return ResultUtil.canBeMocked(paramPsiType)
                ? ResultUtil.getMockedResult(testPsiFile, paramPsiType, project)
                : ResultUtil.getPrimitiveValue(paramPsiType);
    }

    static void initArray(PsiFile testPsiFile, PsiArrayType returnType, String arrayType,
            PsiField testField,
            PsiMethod method, PsiComment actComment, Project project) {
        // Add array initialization statement.
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        String arrayInitStr = testField.getName() + " = new " + arrayType + "[1];";
        PsiStatement arrayInit = factory.createStatementFromText(arrayInitStr, null);
        CodeUpdateUtil.addMockStatement(arrayInit, method, actComment, project);

        // Add element to array.
        String arrayElement = getElementToAdd(testPsiFile, returnType.getComponentType(), project);

        String setArrayElementStr = testField.getName() + "[0] = " + arrayElement + ";";
        PsiStatement setArrayElement = factory.createStatementFromText(setArrayElementStr, null);
        CodeUpdateUtil.addMockStatement(setArrayElement, method, actComment, project);
    }

    static void initCollection(PsiFile testPsiFile, String typeParam, PsiField testField,
            PsiMethod method,
            Project project) {
        // Find place in test method where to put array initialization.
        PsiComment actComment = TestUtil.findActComment(method);

        // Add collection initialization statement.
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        String colInitStr;
        if (typeParam != null) {
            colInitStr = testField.getName() + " = new java.util.ArrayList<" + typeParam + ">();";
        } else {
            colInitStr = testField.getName() + " = new java.util.ArrayList();";
        }

        PsiStatement colInit = factory.createStatementFromText(colInitStr, null);

        CodeUpdateUtil.addMockStatement(colInit, method, actComment, project);

        // Add element to collection.
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);

        String colElement;
        if (typeParam != null) {
            PsiType paramPsiType = PsiType.getTypeByName(typeParam, project, scope);

            // Define element to be added to the collection.
            colElement = getElementToAdd(testPsiFile, paramPsiType, project);
        } else {
            colElement = "\"test\"";
        }

        String addElementStr = testField.getName() + ".add(" + colElement + ");";
        PsiStatement addElement = factory.createStatementFromText(addElementStr, null);
        CodeUpdateUtil.addMockStatement(addElement, method, actComment, project);
    }
}

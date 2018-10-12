package com.mockhelper.util.result;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.TestUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ListResultUtil {

    private ListResultUtil() {
        throw new AssertionError("Utility class should not be initialized.");
    }

    static String getCollectionListArrayListResult(PsiFile testPsiFile, PsiType returnType,
            Project project) {
        String result;
        String canonicalText = returnType.getCanonicalText();
        int openBracketIndex = canonicalText.indexOf('<');
        int closeBraceIndex = canonicalText.indexOf('>');
        String typeParam = null;
        if (openBracketIndex > 0 && closeBraceIndex > 0) {
            typeParam = canonicalText.substring(openBracketIndex + 1, closeBraceIndex);
        }

        // Find field name
        String fieldName;
        if (typeParam == null) {
            fieldName = "strings";
        } else {
            fieldName = ResultUtil.getFieldName(typeParam);
        }

        // Find appropriate field in test.
        PsiClass testClass = TestUtil.getTestClass(testPsiFile, project);
        PsiField testField = CodeUpdateUtil.getPsiField(returnType, fieldName, testClass, project);

        // Add collection initialization to the last method in the test if it's not initialized in the method yet.
        PsiMethod[] methods = testClass.getMethods();
        PsiMethod method = methods[methods.length - 1];

        boolean initialized = TestUtil.isFieldAlreadyInitialized(testField, method);
        if (!initialized) {
            InitUtil.initCollection(testPsiFile, typeParam, testField, method, project);
        }

        result = testField.getName();
        return result;
    }

    static boolean isCollectionListArrayList(PsiType returnType) {
        return returnType.getCanonicalText().startsWith(Collection.class.getCanonicalName())
                || returnType.getCanonicalText().startsWith(List.class.getCanonicalName())
                || returnType.getCanonicalText().startsWith(ArrayList.class.getCanonicalName());
    }
}

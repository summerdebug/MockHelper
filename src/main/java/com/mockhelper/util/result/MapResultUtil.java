package com.mockhelper.util.result;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.TestUtil;
import java.util.HashMap;
import java.util.Map;

class MapResultUtil {

    private MapResultUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }


    static boolean isMapHashMap(PsiType returnType) {
        return returnType.getCanonicalText().startsWith(Map.class.getCanonicalName())
                || returnType.getCanonicalText().startsWith(HashMap.class.getCanonicalName());
    }

    static String getMapHashMapResult(PsiFile testPsiFile, PsiType returnType,
            Project project) {
        String canonicalText = returnType.getCanonicalText();
        int openBraceIndex = canonicalText.indexOf('<');
        int closeBraceIndex = canonicalText.indexOf('>');
        boolean typesDefined = openBraceIndex > 0 && closeBraceIndex > 0;

        // Define key and value types.
        String keyType = String.class.getCanonicalName();
        String valueType = String.class.getCanonicalName();
        if (typesDefined) {
            String typeStr = canonicalText.substring(openBraceIndex + 1, closeBraceIndex);
            int commaIndex = typeStr.indexOf(',');
            keyType = typeStr.substring(0, commaIndex).trim();
            valueType = typeStr.substring(commaIndex + 1).trim();
        }

        // Define fieldName.
        String keyVarName = TestUtil.getVariableName(keyType);
        String valueVarName = TestUtil.getVariableName(valueType);
        String fieldName = keyVarName + ("" + valueVarName.charAt(0)).toUpperCase() + valueVarName.substring(1) + "Map";

        // Find appropriate field in test.
        PsiClass testClass = TestUtil.getTestClass(testPsiFile, project);
        PsiField testField = CodeUpdateUtil.getPsiField(returnType, fieldName, testClass, project);

        // Add map initialization, if not initialized yet.
        PsiMethod[] methods = testClass.getMethods();
        PsiMethod method = methods[methods.length - 1];
        boolean initialized = TestUtil.isFieldAlreadyInitialized(testField, method);
        if (!initialized) {
            InitUtil.initMap(testPsiFile, keyType, valueType, testField, method, project);
        }

        return testField.getName();
    }
}

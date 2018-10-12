package com.mockhelper.util.result;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.mockhelper.util.PsiManipulationUtil;

class EnumResultUtil {

    private EnumResultUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    static String getEnumResult(PsiType returnType, Project project) {
        PsiClass psiClass = PsiManipulationUtil.getPsiClass(returnType.getCanonicalText(), project);
        if (psiClass != null) {
            PsiField[] enumFields = psiClass.getFields();
            if (enumFields.length > 0) {
                String elementName = enumFields[0].getText();
                return returnType.getCanonicalText() + "." + elementName;
            }
        }
        return null;
    }

    static boolean isEnum(PsiType returnType) {
        PsiType[] superTypes = returnType.getSuperTypes();
        if (superTypes.length > 0) {
            return superTypes[0].getCanonicalText().startsWith("java.lang.Enum");
        }
        return false;
    }
}

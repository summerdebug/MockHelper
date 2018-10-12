package com.mockhelper.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.xdebugger.XDebugSession;
import com.mockhelper.util.result.ResultUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Working with methods parameters.
 */
public class ParamUtil {

    private static final String INSTANCE_OF = "instance of";
    private static final String ENHANCER_BY_MOCKITO = "EnhancerByMockito";

    private ParamUtil() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    @NotNull
    public static String[] getParams(String expression) {
        int lastCloseBraceIndex = expression.lastIndexOf(')');
        int closeBraceCounter = 1;
        int currentIndex = getCurrentIndex(expression, lastCloseBraceIndex, closeBraceCounter);
        String paramExpression = expression.substring(currentIndex, lastCloseBraceIndex + 1);

        // Try to get params considering method calls as a param values.
        List<String> paramsList = new ArrayList<>();
        int firstOpenBraceIndex = paramExpression.indexOf('(');
        int i = firstOpenBraceIndex + 1;
        int previousIndex = firstOpenBraceIndex;
        int braceCounter = 0;
        while (i < paramExpression.length()) {
            if (paramExpression.charAt(i) == '(') {
                braceCounter++;
            } else if (paramExpression.charAt(i) == ')') {
                braceCounter--;
            }

            if ((paramExpression.charAt(i) == ',' || paramExpression.charAt(i) == ')' && braceCounter < 0)
                    && braceCounter <= 0) {
                String parameter = paramExpression.substring(previousIndex + 1, i);
                paramsList.add(parameter);
                previousIndex = i;
            }

            if (braceCounter < 0) {
                break;
            }

            i++;
        }

        String[] result = new String[paramsList.size()];
        return paramsList.toArray(result);
    }

    private static int getCurrentIndex(String expression, int lastCloseBraceIndex, int closeBraceCounter) {
        int currentIndex = lastCloseBraceIndex;
        while (currentIndex > 0) {
            currentIndex--;
            if (expression.charAt(currentIndex) == ')') {
                closeBraceCounter++;
            } else if (expression.charAt(currentIndex) == '(') {
                closeBraceCounter--;
            }
            if (closeBraceCounter == 0) {
                break;
            }
        }
        return currentIndex;
    }

    @NotNull
    public static String getParamValues(PsiFile testPsiFile, XDebugSession session, String[] params,
            PsiMethod psiMethod, Project project) {
        boolean notMockedObject = isParametersContainNotMockedObject(session, params);
        StringBuilder paramValues = new StringBuilder("(");
        for (int i = 0; i < params.length; i++) {
            if (!"".equals(params[i])) {
                String paramValue = DebugUtil.evaluateExpression(session, params[i]);
                PsiType paramType = psiMethod.getParameterList().getParameters()[i].getType();
                paramValue = getParamValue(testPsiFile, project, notMockedObject, paramValue, paramType);
                paramValues.append(paramValue);
                if (i < params.length - 1) {
                    paramValues.append(", ");
                }
            }
        }
        paramValues.append(")");
        return paramValues.toString();
    }

    private static boolean isParametersContainNotMockedObject(XDebugSession session, String[] params) {
        boolean notMockedObject = false;
        for (String param : params) {
            if (!"".equals(param)) {
                String paramValue = DebugUtil.evaluateExpression(session, param);
                if (paramValue.startsWith(INSTANCE_OF) && !paramValue.contains(ENHANCER_BY_MOCKITO)) {
                    notMockedObject = true;
                    break;
                }
            }
        }
        return notMockedObject;
    }

    private static String getParamValue(PsiFile testPsiFile, Project project, boolean notMockedObject,
            String paramValue, PsiType paramType) {
        if (needAddL(paramValue, paramType)) {
            paramValue += "L";
        } else if (paramValue.contains(ENHANCER_BY_MOCKITO)) {
            paramValue = getTestField(testPsiFile, project, paramValue, paramType);
        } else if (notMockedObject(notMockedObject, paramValue)) {
            paramValue = "eq(" + paramValue + ")";
        } else if (Objects.requireNonNull(paramValue).startsWith(INSTANCE_OF) && !paramValue
                .contains(ENHANCER_BY_MOCKITO)) {
            paramValue = "any(" + paramType.getCanonicalText() + ".class)";
        }
        return paramValue;
    }

    private static boolean needAddL(String paramValue, PsiType paramType) {
        return (paramType.equals(PsiType.LONG)
                || paramType.getCanonicalText().equals(Long.class.getCanonicalName())) && !paramValue
                .endsWith("L");
    }

    private static String getTestField(PsiFile testPsiFile, Project project, String paramValue, PsiType paramType) {
        String typeCanonicalText = paramType.getCanonicalText();
        String fieldNameCapitalized = typeCanonicalText.substring(typeCanonicalText.lastIndexOf('.') + 1);
        fieldNameCapitalized =
                fieldNameCapitalized.substring(0, 1).toLowerCase() + fieldNameCapitalized.substring(1);

        // find existing test field.
        PsiClass testClass = TestUtil.getTestClass(testPsiFile, project);
        PsiField testField = TestUtil.findTestField(fieldNameCapitalized, testClass);

        if (testField != null) {
            paramValue = testField.getName();
        }
        return paramValue;
    }

    private static boolean notMockedObject(boolean notMockedObject, String paramValue) {
        return notMockedObject
                && !(Objects.requireNonNull(paramValue).startsWith(INSTANCE_OF) && !paramValue
                .contains(ENHANCER_BY_MOCKITO));
    }

    @NotNull
    public static String getParamValues(PsiFile testPsiFile, Project project, PsiParameter[] params) {
        StringBuilder values = new StringBuilder();
        for (PsiParameter param : params) {
            PsiType psiType = param.getType();
            String value = ResultUtil.getValueForType(testPsiFile, project, psiType);
            if (values.length() > 0) {
                values.append(", ");
            }
            values.append(value);
        }
        return values.toString();
    }
}

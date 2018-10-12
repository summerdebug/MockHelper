package com.mockhelper.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.mockhelper.util.CodeUpdateUtil;
import com.mockhelper.util.PsiManipulationUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Rearranges the fields in test. To invoke, put mouse pointer somewhere inside the test class and press Alt + L
 * <ul>
 * <li>Make all fields, except JUnit Rules, private.</li>
 * <li>Put fields in the following order: constants, not mocked fields, mocked fields, JUnit rules.<li/>
 * <ul/>
 */
public class ArrangeFields extends AnAction {

    private static final String NAME = "final";
    private static final String[] modifiers = new String[]{NAME, "public", "static", "private", "protected"};
    private static final String ORG_JUNIT_RULE = "org.junit.Rule";
    private static final String ORG_MOCKITO_MOCK = "org.mockito.Mock";

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        PsiElement elementAtCaret = PsiManipulationUtil.getPsiElementAtCaret(e);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass.class);
        arrangeFields(project, psiClass);
    }

    private void arrangeFields(Project project, PsiClass psiClass) {
        arrangeFinalFields(project, psiClass, psiClass.getFields());
        arrangeNotMockedNotFinalNoRuleField(project, psiClass, psiClass.getFields());
        arrangeMocks(project, psiClass, psiClass.getFields());
        arrangeRules(project, psiClass, psiClass.getFields());
        arrangeAllFields(project, psiClass, psiClass.getFields());
    }

    private void arrangeFinalFields(Project project, PsiClass psiClass, PsiField[] fields) {
        for (PsiField field : fields) {
            boolean isFinal = Objects.requireNonNull(field.getModifierList()).hasExplicitModifier(NAME);
            boolean hasRuleAnnotation = field.hasAnnotation(ORG_JUNIT_RULE);
            if (isFinal) {
                updateField(project, psiClass, field, hasRuleAnnotation);
            }
        }
    }

    private void updateField(Project project, PsiClass psiClass, PsiField field, boolean hasRuleAnnotation) {
        CodeUpdateUtil.addModifierToField(project, field, hasRuleAnnotation ? "public" : "private");
        String type = getType(field);
        String variableName = field.getName();
        String[] annotations = getAnnotations(field);
        Map<String, Boolean> modifiersMap = getModifiersMap(field);
        String initExpression = getInitExpression(field);

        WriteCommandAction.runWriteCommandAction(project, field::delete);

        PsiField newField = createField(project, type, variableName, annotations, modifiersMap, initExpression);
        WriteCommandAction.runWriteCommandAction(project, (Runnable) () -> psiClass.add(newField));
    }

    @NotNull
    private String getType(PsiField oldField) {
        PsiType psiType = oldField.getType();
        return psiType.getCanonicalText();
    }

    @NotNull
    private String[] getAnnotations(PsiField oldField) {
        PsiAnnotation[] annotations = Objects.requireNonNull(oldField.getModifierList()).getAnnotations();
        String[] annotationStrings = new String[annotations.length];
        int i = 0;
        for (PsiAnnotation psiAnnotation : annotations) {
            annotationStrings[i++] = psiAnnotation.getQualifiedName();
        }
        return annotationStrings;
    }

    @NotNull
    private Map<String, Boolean> getModifiersMap(PsiField oldField) {
        PsiModifierList modifierList = oldField.getModifierList();
        Map<String, Boolean> modifiersMap = new HashMap<>();
        for (String modifier : modifiers) {
            modifiersMap.put(modifier, Objects.requireNonNull(modifierList).hasExplicitModifier(modifier));
        }
        return modifiersMap;
    }

    @Nullable
    private String getInitExpression(PsiField oldField) {
        boolean isInit = oldField.hasInitializer();
        PsiExpression psiExpression;
        String initExpressionStr = null;
        if (isInit) {
            psiExpression = oldField.getInitializer();
            initExpressionStr = Objects.requireNonNull(psiExpression).getText();
        }
        return initExpressionStr;
    }

    @NotNull
    private PsiField createField(Project project, String type, String variableName, String[] annotations,
            Map<String, Boolean> modifiersMap, String initExpression) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiClassType psiType = PsiType.getTypeByName(type, project, GlobalSearchScope.allScope(project));
        PsiField newField = factory.createField(variableName, psiType);
        addAnnotations(annotations, newField);
        addModifiers(project, modifiersMap, newField);
        addInitializer(initExpression, factory, newField);
        return newField;
    }

    private void addAnnotations(String[] annotationStrings, PsiField newField) {
        for (String oldAnnotation : annotationStrings) {
            Objects.requireNonNull(newField.getModifierList()).addAnnotation(oldAnnotation);
        }
    }

    private void addModifiers(Project project, Map<String, Boolean> modifiersMap,
            PsiField newField) {
        for (String modifier : ArrangeFields.modifiers) {
            if (modifiersMap.get(modifier)) {
                CodeUpdateUtil.addModifierToField(project, newField, modifier);
            }
        }
    }

    private void addInitializer(String initExpressionStr, PsiElementFactory factory, PsiField newField) {
        if (initExpressionStr != null) {
            PsiExpression newInitExpr = factory.createExpressionFromText(initExpressionStr, null);
            newField.setInitializer(newInitExpr);
        }
    }

    private void arrangeNotMockedNotFinalNoRuleField(Project project, PsiClass psiClass, PsiField[] fields) {
        for (PsiField oldField : fields) {
            boolean isFinal = Objects.requireNonNull(oldField.getModifierList()).hasExplicitModifier(NAME);
            boolean hasMockAnnotation = oldField.hasAnnotation(ORG_MOCKITO_MOCK);
            boolean hasRuleAnnotation = oldField.hasAnnotation(ORG_JUNIT_RULE);
            if (!isFinal && !hasMockAnnotation && !hasRuleAnnotation) {
                updateField(project, psiClass, oldField, false);
            }
        }
    }

    private void arrangeMocks(Project project, PsiClass psiClass, PsiField[] fields) {
        for (PsiField oldField : fields) {
            boolean hasMockAnnotation = oldField.hasAnnotation(ORG_MOCKITO_MOCK);
            boolean hasRuleAnnotation = oldField.hasAnnotation(ORG_JUNIT_RULE);
            if (hasMockAnnotation) {
                updateField(project, psiClass, oldField, hasRuleAnnotation);
            }
        }
    }

    private void arrangeRules(Project project, PsiClass psiClass, PsiField[] fields) {
        for (PsiField oldField : fields) {
            boolean hasRuleAnnotation = oldField.hasAnnotation(ORG_JUNIT_RULE);
            if (hasRuleAnnotation) {
                updateField(project, psiClass, oldField, true);
            }
        }
    }

    private void arrangeAllFields(Project project, PsiClass psiClass, PsiField[] fields) {
        for (PsiField oldField : fields) {
            boolean hasRuleAnnotation = oldField.hasAnnotation(ORG_JUNIT_RULE);
            updateField(project, psiClass, oldField, hasRuleAnnotation);
        }
    }

}

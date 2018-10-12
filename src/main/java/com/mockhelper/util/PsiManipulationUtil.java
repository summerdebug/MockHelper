package com.mockhelper.util;

import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageView;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import java.util.Objects;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiManipulationUtil {

    private PsiManipulationUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    @Nullable
    static String getExpressionText(@Nullable ExpressionInfo expressionInfo, @NotNull Document document) {
        if (expressionInfo == null) {
            return null;
        }
        String text = expressionInfo.getExpressionText();
        return text == null ? document.getText(expressionInfo.getTextRange()) : text;
    }

    public static PsiClass getPsiClass(String className, Project project) {
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, className + ".java",
                GlobalSearchScope.allScope(project));
        PsiClass psiClass = null;
        if (psiFiles.length > 0) {
            psiClass = PsiTreeUtil.findChildOfType(psiFiles[0], PsiClass.class);
        }
        return psiClass;
    }

    @NotNull
    public static PsiStatement createPsiStatement(Project project, String mockMethodStr) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        return factory.createStatementFromText(mockMethodStr, null);
    }

    @NotNull
    static String getSimpleClassName(String qualifiedClassName) {
        int lastDotIndex = qualifiedClassName.lastIndexOf('.');
        String simpleName = qualifiedClassName;
        if (lastDotIndex > 0) {
            simpleName = qualifiedClassName.substring(lastDotIndex);
        }
        return simpleName;
    }

    public static PsiMethod getSelectedMethod(AnActionEvent e) {
        UsageTarget[] usageTargets = e.getData(UsageView.USAGE_TARGETS_KEY);
        if (ArrayUtils.isEmpty(usageTargets)) {
            return null;
        }
        UsageTarget usageTarget = usageTargets[0];
        if (usageTarget instanceof PsiElement2UsageTargetAdapter) {
            PsiElement2UsageTargetAdapter adapter = (PsiElement2UsageTargetAdapter) usageTarget;
            PsiElement psiElement = adapter.getElement();
            if (psiElement instanceof PsiMethod) {
                return (PsiMethod) psiElement;
            }
        }
        return null;
    }

    @Nullable
    public static String getBaseObject(PsiMethod psiMethod) {
        PsiElement parent = psiMethod.getParent();
        if (parent instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) parent;
            String className = psiClass.getName();
            return Objects.requireNonNull(className).substring(0, 1).toLowerCase() + className.substring(1);
        }
        return null;
    }

    public static String getBaseClass(PsiMethod psiMethod) {
        PsiElement parent = psiMethod.getParent();
        if (parent instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) parent;
            return psiClass.getQualifiedName();
        }
        return null;
    }

    public static PsiElement getPsiElementAtCaret(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        int offset = Objects.requireNonNull(editor).getCaretModel().getOffset();
        return Objects.requireNonNull(psiFile).findElementAt(offset);
    }

}

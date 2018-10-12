package com.mockhelper.util.result;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.mockhelper.util.PsiManipulationUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({PsiManipulationUtil.class})
@RunWith(PowerMockRunner.class)
public class EnumResultUtilTest {

    @Mock
    private PsiType psiType;
    @Mock
    private Project project;
    @Mock
    private PsiClass psiClass;
    @Mock
    private PsiField psiField;

    @Test
    public void givenEnumResultUtilWhenGetEnumResultThenSuccessful() {
        // Arrange
        mockStatic(PsiManipulationUtil.class);
        PowerMockito.when(PsiManipulationUtil.getPsiClass(null, project)).thenReturn(psiClass);
        PsiField[] psiFields = new PsiField[1];
        psiFields[0] = psiField;
        when(psiClass.getFields()).thenReturn(psiFields);

        // Act
        String result = EnumResultUtil.getEnumResult(psiType, project);

        // Assert
        assertEquals("null.null", result);
        verify(psiClass).getFields();
        PowerMockito.verifyStatic();
        PsiManipulationUtil.getPsiClass(null, project);
    }
}

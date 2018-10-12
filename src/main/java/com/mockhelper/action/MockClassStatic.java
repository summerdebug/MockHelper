package com.mockhelper.action;

import com.intellij.debugger.actions.DebuggerAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.mockhelper.util.MockClassStaticUtil;

public class MockClassStatic extends DebuggerAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        MockClassStaticUtil.mockClassStatically(e);
    }

}

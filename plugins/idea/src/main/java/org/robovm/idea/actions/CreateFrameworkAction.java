/*
 * Copyright 2016 Justin Shapcott.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robovm.idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import org.robovm.idea.RoboVmPlugin;

import java.io.File;

public class CreateFrameworkAction extends AnAction {
    public static final Key<FrameworkConfig> FRAMEWORK_CONFIG_KEY = Key.create("FRAMEWORK_CONFIG");

    public void actionPerformed(final AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            RoboVmPlugin.logBalloon(null, MessageType.ERROR, "Oops. Project is missing.");
            return;
        }

        final CreateFrameworkDialog dialog = new CreateFrameworkDialog(project);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            // create Framework
            FrameworkConfig frameworkConfig = dialog.getFrameworkConfig();
            CompileScope scope = CompilerManager.getInstance(project).createModuleCompileScope(frameworkConfig.module, true);
            scope.putUserData(FRAMEWORK_CONFIG_KEY, frameworkConfig);
            CompilerManager.getInstance(project).compile(scope, (aborted, errors, warnings, compileContext) ->
                    RoboVmPlugin.logInfo(project, "Framework creation complete, %d errors, %d warnings", errors, warnings)
            );
        }
    }

    public static class FrameworkConfig {
        private final Module module;
        private final File destinationDir;

        public FrameworkConfig(Module module, File destinationDir) {
            this.module = module;
            this.destinationDir = destinationDir;
        }

        public Module getModule() {
            return module;
        }

        public File getDestinationDir() {
            return destinationDir;
        }
    }
}

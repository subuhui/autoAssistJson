package com.xxz.utils;

import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import io.flutter.FlutterMessages;
import io.flutter.pub.PubRoot;
import io.flutter.sdk.FlutterSdk;
import io.flutter.utils.ProgressHelper;
import org.jetbrains.annotations.NotNull;

public class PubRunUtils {
    public static void runFlutterPubRun(AnActionEvent event){
        final Presentation presentation = event.getPresentation();
        final Project project = event.getProject();
        if (project == null) {
            return;
        }
        final FlutterSdk sdk = FlutterSdk.getFlutterSdk(project);
        if (sdk == null) {
            return;
        }
        final PubRoot pubRoot = PubRoot.forEventWithRefresh(event);
        if (pubRoot == null) {
            return;
        }
        build(project, pubRoot, sdk, presentation.getDescription());
    }

    private static ColoredProcessHandler build(@NotNull Project project,
                                               @NotNull PubRoot pubRoot,
                                               FlutterSdk sdk,
                                               String desc) {
        final ProgressHelper progressHelper = new ProgressHelper(project);
        progressHelper.start(desc);
        final ColoredProcessHandler processHandler = sdk.flutterPub(pubRoot, "run","build_runner","build","--delete-conflicting-outputs").startInConsole(project);
        if (processHandler == null) {
            progressHelper.done();
        }
        else {
            processHandler.addProcessListener(new ProcessAdapter() {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    progressHelper.done();
                    final int exitCode = event.getExitCode();
                    if (exitCode != 0) {
                        FlutterMessages.showError("Error while building " + "build_runner", "`JsonSerializable-Build` returned: " + exitCode, project);
                    }
                }
            });
        }
        return processHandler;
    }
}

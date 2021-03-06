package com.wannabe.smartsearch;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.wannabe.smartsearch.actions.SmartSearch;
import com.wannabe.smartsearch.actions.WebSearchFromIde;
import com.wannabe.smartsearch.utils.TrimService;
import org.jetbrains.annotations.NotNull;

/**
 * Created by wannabe on 10.02.16.
 */
public class SmartSearchComponent implements ProjectComponent {

    private final Project project;

    public SmartSearchComponent(Project project) {
        this.project = project;
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "SmartSearchComponent";
    }

    @Override
    public void projectOpened() {
        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            TrimService trimService = new TrimService(project);
            SmartSearch.init(trimService);
            WebSearchFromIde.init(trimService);
        });
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {

    }

}

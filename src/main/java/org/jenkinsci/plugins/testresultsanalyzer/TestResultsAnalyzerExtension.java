package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

@Extension
public class TestResultsAnalyzerExtension extends TransientProjectActionFactory{


	@Override
	public Collection<? extends Action> createFor(@SuppressWarnings("rawtypes") AbstractProject target) {
		
		final List<TestResultsAnalyzerAction> projectActions = target
                .getActions(TestResultsAnalyzerAction.class);
        final ArrayList<Action> actions = new ArrayList<Action>();
        if (projectActions.isEmpty()) {
            final TestResultsAnalyzerAction newAction = new TestResultsAnalyzerAction(target);
            actions.add(newAction);
            return actions;
        } else {
            return projectActions;
        }
	}

}

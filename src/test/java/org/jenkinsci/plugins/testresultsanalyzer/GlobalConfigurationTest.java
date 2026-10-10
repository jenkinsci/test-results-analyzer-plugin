package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.htmlunit.html.HtmlForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class GlobalConfigurationTest {

    @AfterEach
    void resetPolicy() {
        TestResultsAnalyzerExtension.DESCRIPTOR.setDuplicateTestPolicy(DuplicateTestPolicy.DEFAULT);
    }

    @Test
    void duplicateTestPolicyIsSaved(JenkinsRule j) throws Exception {
        TestResultsAnalyzerExtension.DescriptorImpl descriptor = TestResultsAnalyzerExtension.DESCRIPTOR;
        assertThat(descriptor.getDuplicateTestPolicy(), is(DuplicateTestPolicy.FAILED_IF_ANY_FAILED));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlForm form = wc.goTo("configure").getFormByName("config");
            form.getSelectByName("duplicateTestPolicy")
                    .setSelectedAttribute(DuplicateTestPolicy.PASSED_IF_ANY_PASSED.name(), true);
            j.submit(form);
        }
        assertThat(descriptor.getDuplicateTestPolicy(), is(DuplicateTestPolicy.PASSED_IF_ANY_PASSED));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlForm form = wc.goTo("configure").getFormByName("config");
            assertThat(
                    form.getSelectByName("duplicateTestPolicy")
                            .getSelectedOptions()
                            .get(0)
                            .getValueAttribute(),
                    is(DuplicateTestPolicy.PASSED_IF_ANY_PASSED.name()));
        }
    }
}

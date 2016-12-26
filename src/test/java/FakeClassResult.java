import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

public class FakeClassResult extends FakeTabulatedResult {

    public FakeClassResult(TestObject parent, String name) {
        super(parent, name);
    }

    public void addTest(String testMethod, TestStatus testStatus) {
        addTestResult(new FakeTestMethod(this, testMethod, testStatus));
    }
}

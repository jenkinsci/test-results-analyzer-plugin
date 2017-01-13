import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

public class FakeTestMethod extends TestResult {
    private TestObject parent;
    private String name;
    private TestStatus status;

    public FakeTestMethod(TestObject parent, String name, TestStatus status) {
        this.parent = parent;
        this.name = name;
        this.status = status;
    }

    @Override
    public TestObject getParent() {
        return parent;
    }

    @Override
    public TestResult findCorrespondingResult(String s) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return name;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPassCount() {
        return status == TestStatus.Pass ? 1 : 0;
    }

    @Override
    public int getFailCount() {
        return status == TestStatus.Fail ? 1 : 0;
    }

    @Override
    public int getSkipCount() {
        return status == TestStatus.Skip ? 1 : 0;
    }
}

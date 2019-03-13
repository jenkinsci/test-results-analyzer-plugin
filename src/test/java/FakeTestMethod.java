import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

public class FakeTestMethod extends TestResult {

	private TestObject parent;
	private String name;
	private TestStatus status;
	private String errorDetails;
	private String errorStackTrace;

	public FakeTestMethod(TestObject parent, String name, TestStatus status) {
		this.parent = parent;
		this.name = name;
		this.status = status;
	}

	public FakeTestMethod(TestObject parent, String name, TestStatus status, String errorDetails, String errorStackTrace) {
		this(parent, name, status);
		this.errorDetails = errorDetails;
		this.errorStackTrace = errorStackTrace;
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

	@Override
	public String getErrorDetails() {
		return errorDetails;
	}

	@Override
	public String getErrorStackTrace() {
		return errorStackTrace;
	}
}

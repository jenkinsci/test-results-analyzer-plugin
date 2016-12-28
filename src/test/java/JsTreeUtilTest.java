import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.testresultsanalyzer.JsTreeUtil;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsTreeUtilTest {

    @Test
    public void noRequiredBuildsAndNoTestCases() {
        List<Integer> builds = new ArrayList<Integer>();
        ResultInfo results = new ResultInfo();

        JSONObject expected = buildRoot(jsonArray(), jsonArray());
        assertEquals(expected, new JsTreeUtil().getJsTree(builds, results, false));
    }

    @Test
    public void requiresBuildsButHasNoTestCases() {
        List<Integer> builds = Arrays.asList(9, 7, 6);
        ResultInfo results = new ResultInfo();

        JSONObject expected = buildRoot(jsonArray("9", "7", "6"), jsonArray());
        assertEquals(expected, new JsTreeUtil().getJsTree(builds, results, false));
    }

    @Test
    public void requiresBuildsButHasJustOneTestCase() {
        List<Integer> builds = Arrays.asList(9, 7);
        ResultInfo results = new ResultInfo();

        FakePackageResult packageFoo = new FakePackageResult("pn")
                .addTest("Class1", "method1", TestStatus.Pass);
        results.addPackage(7, packageFoo, "someUrl/");

        JSONObject method1Result = buildLeaf(1, 0, 0, "PASSED", "someUrl/testReport/pn/Class1/method1", 7);
        JSONObject class1Result = buildLeaf(1, 0, 0, "PASSED", "someUrl/testReport/pn/Class1", 7);
        JSONObject pnResult = buildLeaf(1, 0, 0, "PASSED", "someUrl/testReport/pn", 7);

        JSONObject method1Node = buildNode("method1", jsonArray(buildNA(9), method1Result), jsonArray());
        JSONObject class1Node = buildNode("Class1", jsonArray(buildNA(9), class1Result), jsonArray(method1Node));
        JSONObject pnNode = buildNode("pn", jsonArray(buildNA(9), pnResult), jsonArray(class1Node));

        JSONObject expected = buildRoot(jsonArray("9", "7"), jsonArray(pnNode));

        assertEquals(expected, new JsTreeUtil().getJsTree(builds, results, false));
    }

    @Test
    public void onePassedAndSkippedTestLeadsToPassed() {
        List<Integer> builds = Arrays.asList(1);
        ResultInfo results = new ResultInfo();

        FakePackageResult packageFoo = new FakePackageResult("pn")
                .addTest("Class1", "method1", TestStatus.Pass)
                .addTest("Class1", "method2", TestStatus.Skip);
        results.addPackage(1, packageFoo, "someUrl/");

        JSONObject method1Result = buildLeaf(1, 0, 0, "PASSED", "someUrl/testReport/pn/Class1/method1", 1);
        JSONObject method2Result = buildLeaf(0, 0, 1, "SKIPPED", "someUrl/testReport/pn/Class1/method2", 1);
        JSONObject class1Result = buildLeaf(1, 0, 1, "PASSED", "someUrl/testReport/pn/Class1", 1);
        JSONObject pnResult = buildLeaf(1, 0, 1, "PASSED", "someUrl/testReport/pn", 1);

        JSONObject method1Node = buildNode("method1", jsonArray(method1Result), jsonArray());
        JSONObject method2Node = buildNode("method2", jsonArray(method2Result), jsonArray());
        JSONObject class1Node = buildNode("Class1", jsonArray(class1Result), jsonArray(method1Node, method2Node));
        JSONObject pnNode = buildNode("pn", jsonArray(pnResult), jsonArray(class1Node));

        JSONObject expected = buildRoot(jsonArray("1"), jsonArray(pnNode));

        assertEquals(expected, new JsTreeUtil().getJsTree(builds, results, false));
    }

    @Test
    public void onePassedAndFailedTestLeadsToFailed() {
        List<Integer> builds = Arrays.asList(1);
        ResultInfo results = new ResultInfo();

        FakePackageResult packageFoo = new FakePackageResult("pn")
                .addTest("Class1", "method1", TestStatus.Pass)
                .addTest("Class1", "method2", TestStatus.Fail);
        results.addPackage(1, packageFoo, "someUrl/");

        JSONObject method1Result = buildLeaf(1, 0, 0, "PASSED", "someUrl/testReport/pn/Class1/method1", 1);
        JSONObject method2Result = buildLeaf(0, 1, 0, "FAILED", "someUrl/testReport/pn/Class1/method2", 1);
        JSONObject class1Result = buildLeaf(1, 1, 0, "FAILED", "someUrl/testReport/pn/Class1", 1);
        JSONObject pnResult = buildLeaf(1, 1, 0, "FAILED", "someUrl/testReport/pn", 1);

        JSONObject method1Node = buildNode("method1", jsonArray(method1Result), jsonArray());
        JSONObject method2Node = buildNode("method2", jsonArray(method2Result), jsonArray());
        JSONObject class1Node = buildNode("Class1", jsonArray(class1Result), jsonArray(method1Node, method2Node));
        JSONObject pnNode = buildNode("pn", jsonArray(pnResult), jsonArray(class1Node));

        JSONObject expected = buildRoot(jsonArray("1"), jsonArray(pnNode));

        assertEquals(expected, new JsTreeUtil().getJsTree(builds, results, false));
    }

    private static JSONObject buildRoot(JSONArray builds, JSONArray results) {
        JSONObject result = new JSONObject();

        result.put("builds", builds);
        result.put("results", results);

        return result;
    }

    private static JSONObject buildNode(String text, JSONArray buildResults, JSONArray children) {
        JSONObject result = new JSONObject();

        result.put("text", text);
        result.put("buildResults", buildResults);
        result.put("children", children);

        return result;
    }

    private static JSONObject buildLeaf(int passed, int failed, int skipped, String status, String url, Integer buildNumber) {
        JSONObject result = new JSONObject();

        result.put("buildNumber", buildNumber.toString());
        result.put("totalTests", passed + failed + skipped);
        result.put("totalFailed", failed);
        result.put("totalPassed", passed);
        result.put("totalSkipped", skipped);
        result.put("totalTimeTaken", 0);
        result.put("status", status);
        result.put("url", url);

        return result;
    }

    private static JSONObject buildNA(Integer buildNumber) {
        JSONObject result = new JSONObject();

        result.put("buildNumber", buildNumber.toString());
        result.put("status", "N/A");

        return result;
    }

    private static void assertEquals(String expected, JSONObject actual) {
        Assert.assertThat(actual.toString(), is(equalTo(expected)));
    }

    private static void assertEquals(JSONObject expected, JSONObject actual) {
        Assert.assertThat(actual.toString(2), is(equalTo(expected.toString(2))));
    }

    private static JSONArray jsonArray(Object... objects) {
        JSONArray result = new JSONArray();

        for (Object object : objects) {
            result.add(object);
        }

        return result;
    }

    private static JSONArray jsonArray() {
        return new JSONArray();
    }

}

package org.jenkinsci.plugins.testresultsanalyzer;

import hudson.model.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.*;
import org.junit.*;
import static org.junit.Assert.*;
import hudson.model.FreeStyleProject;
import java.util.Locale;
import java.io.File;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.openqa.selenium.support.ui.*;
import io.github.bonigarcia.wdm.*;

public class AnalyzerPageTest {
	private static WebDriver driver;
	private static JavascriptExecutor js;
	private static WebDriverWait wait;

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@BeforeClass
	public static void startDriver() throws Exception {
		//Web Driver Manager handling the driver binaries for us
		ChromeDriverManager.getInstance().setup();

		//Selenium chrome driver
		driver = new ChromeDriver();
		js = (JavascriptExecutor) driver;
		wait = new WebDriverWait(driver, 10);
	}

	@AfterClass
	public static void stopDriver() throws Exception {
		driver.quit();
	}

	@Before
	public void refreshDriver() throws Exception {
		FreeStyleProject project = jenkinsRule.createFreeStyleProject();
		project.getBuildersList().add(new FailureBuilder());
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		jenkinsRule.assertBuildStatus(Result.FAILURE, build);

		String url = jenkinsRule.getURL() + project.getUrl();
		String query = url + "test_results_analyzer";

		driver.get(query);
	}

	@Test
	public void OneTestTwoBuildsTrue() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
		js.executeScript(javaScriptCommand);
		WebElement exclamation_mark = driver.findElement(By.xpath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assertTrue(exclamation_mark.getAttribute("style").contains("inline-block"));
	}

	@Test
	public void OneTestTwoBuildsFalse() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
		js.executeScript(javaScriptCommand);
		WebElement exclamation_mark = driver.findElement(By.xpath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assertTrue(exclamation_mark.getAttribute("style").contains("none"));
	}

	@Test
	public void OneTestOneBuildFalse1() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
		js.executeScript(javaScriptCommand);
		WebElement exclamation_mark = driver.findElement(By.xpath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assertTrue(exclamation_mark.getAttribute("style").contains("none"));
	}

	@Test
	public void OneTestOneBuildFalse2() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
		js.executeScript(javaScriptCommand);
		WebElement exclamation_mark = driver.findElement(By.xpath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assertTrue(exclamation_mark.getAttribute("style").contains("none"));   
	}

	@Test
	public void OneTestMultipleBuildsTrue() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.0432}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
		js.executeScript(javaScriptCommand);
		WebElement exclamation_mark = driver.findElement(By.xpath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assert(exclamation_mark.getAttribute("style").contains("inline-block"));
	}

	@Test
	public void OneTestMultipleBuildsFalse() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.0432}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
		js.executeScript(javaScriptCommand);
		WebElement exclamation_mark = driver.findElement(By.xpath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assert(exclamation_mark.getAttribute("style").contains("none"));
	}

	@Test
	public void MultipleTestsMultipleBuildsFalse() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
		js.executeScript(javaScriptCommand);
		WebElement cs427_exclamation_mark = driver.findElement(By.xpath("//*[contains(@class, 'cs427')]//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assertTrue(cs427_exclamation_mark.getAttribute("style").contains("none"));
		WebElement cs512_exclamation_mark = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]"));
		assertTrue(cs512_exclamation_mark.getAttribute("style").contains("inline-block"));
	}

	// TESTS for filterTests()
	
	@Test
	public void OneTestFilter1() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"illinois\");filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs427')]"));
		assertTrue(row.getAttribute("style").contains("table-row"));
	}

	@Test
	public void OneTestFilter2() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"was\");filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs427')]"));
		assertTrue(row.getAttribute("style").contains("none"));
	}

	@Test
	public void OneTestFilter3() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"\");filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs427')]"));
		assertTrue(row.getAttribute("style").contains("table-row"));
	}

	@Test
	public void MultipleTestsFilter1() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"512\");filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("table-row"));
	}

	@Test
	public void MultipleTestsFilter2() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"meaning of life\");filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("none"));
	}

	@Test
	public void MultipleTestsFilter3() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"\");filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("table-row"));
	}

	@Test
	public void testFilterPass1() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalPASSED\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"PASSED\",\"FAILED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalPASSED\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"PASSED\",\"FAILED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", true);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("table-row"));
	}

	@Test
	public void testFilterPass2() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("none"));
	}

	@Test
	public void testFilterFail1() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", true);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("table-row"));
	}

	@Test
	public void testFilterFail2() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("none"));
	}

	@Test
	public void testFilterSkip1() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", true);filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("table-row"));
	}

	@Test
	public void testFilterSkip2() throws Exception {
		String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":true,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":true,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":true,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":true,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":true,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":true,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
		js.executeScript(javaScriptCommand);
		WebElement row = driver.findElement(By.xpath("//*[contains(@class, 'cs512')]"));
		assertTrue(row.getAttribute("style").contains("none"));
	}
}


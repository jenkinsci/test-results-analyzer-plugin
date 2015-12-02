package org.jenkinsci.plugins.testresultsanalyzer;

//import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
//import hudson.maven.MavenModuleSet;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.jvnet.hudson.test.JenkinsRule;
//import org.jvnet.hudson.test.recipes.LocalData;
//import org.xml.sax.SAXException;
//
//import java.io.IOException;
//
//import static junit.framework.TestCase.assertEquals;

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
import hudson.maven.MavenModuleSet;
import org.jvnet.hudson.test.recipes.LocalData;

public class AnalyzerLinkTest {
	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();
	//JenkinsRule.WebClient webClient;
	//HtmlPage page;
	private static WebDriver driver;
	private static JavascriptExecutor js;
	private static WebDriverWait wait;
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
		MavenModuleSet project = (MavenModuleSet) jenkinsRule.getInstance().getItem("test");
		String url = jenkinsRule.getURL() + project.getUrl();
		String query = url + "edu.illinois.cs427$mp3/test_results_analyzer";

		driver.get(query);
	}

	//Note: Can't make it @BeforeClass as it requires the method to be static, which requires the jenkinsRule to be static (java.lang.Exception: The @Rule 'jenkinsRule' must not be static.)
//	  @Before
//	  public void setup() {
//		  webClient = jenkinsRule.createWebClient();
//		  //webClient.setThrowExceptionOnFailingStatusCode(false);
//		  //webClient.setThrowExceptionOnScriptError(false);
//
//		  MavenModuleSet project = (MavenModuleSet) jenkinsRule.getInstance().getItem("test");
//		  try {
//			  page = webClient.getPage(project, "edu.illinois.cs427$mp3/test_results_analyzer");
//		  } catch (IOException e) {
//			  e.printStackTrace();
//		  } catch (SAXException e) {
//
//			  e.printStackTrace();
//		  }
//
//		  //wait for scripts to load
//		  JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();
//		  while (manager.getJobCount() > 0) {
//			  try {
//				  Thread.sleep(1000);
//			  } catch (InterruptedException e) {
//				  e.printStackTrace();
//			  }
//		  }
//	  }

	@Test
	@LocalData
	public void testLinkPackage() {
		//arrange
		String expectedLink = "/job/test/edu.illinois.cs427$mp3/3/testReport/edu.illinois.cs427.mp3";

		//act
		//HtmlAnchor packageLink = page.getFirstByXPath("//div[@class='table-row base-edu_illinois_cs427_mp3']//div[@class='table-cell build-result passed']//a");
		//String actualLink = packageLink.getHrefAttribute();
		WebElement packageLink = driver.findElement(By.xpath("//div[@class='table-row base-edu_illinois_cs427_mp3']//div[@class='table-cell build-result passed']//a"));
		String actualLink = packageLink.getAttribute("href");

		//assert
		//assertEquals(expectedLink, actualLink);
		assertTrue(actualLink.contains(expectedLink));
	}

	@Test
	@LocalData
	public void testLinkClass() {
		//arrange
		String expectedLink = "/job/test/edu.illinois.cs427$mp3/3/testReport/edu.illinois.cs427.mp3/BookTest";

		//act
		//HtmlAnchor packageLink = page.getFirstByXPath("//div[@class='table-row base-edu_illinois_cs427_mp3-BookTest']//div[@class='table-cell build-result passed']//a");
		//String actualLink = packageLink.getHrefAttribute();
		WebElement packageLink = driver.findElement(By.xpath("//div[@class='table-row base-edu_illinois_cs427_mp3-BookTest']//div[@class='table-cell build-result passed']//a"));
		String actualLink = packageLink.getAttribute("href");

		//assert
		//assertContains(expectedLink, actualLink);
		assertTrue(actualLink.contains(expectedLink));
	}

	@Test
	@LocalData
	public void testLinkTestCase() {
		//arrange
		String expectedLink = "/job/test/edu.illinois.cs427$mp3/3/testReport/edu.illinois.cs427.mp3/BookTest/testBookConstructor1";

		//act
		//HtmlAnchor packageLink = page.getFirstByXPath("//div[@class='table-row base-edu_illinois_cs427_mp3-BookTest-testBookConstructor1']//div[@class='table-cell build-result passed']//a");
		//String actualLink = packageLink.getHrefAttribute();
		WebElement packageLink = driver.findElement(By.xpath("//div[@class='table-row base-edu_illinois_cs427_mp3-BookTest-testBookConstructor1']//div[@class='table-cell build-result passed']//a"));
		String actualLink = packageLink.getAttribute("href");

		//assert
		//assertContains(expectedLink, actualLink);
		assertTrue(actualLink.contains(expectedLink));
	}
}

//package org.jenkinsci.plugins.testresultsanalyzer;
//
//import hudson.model.*;
//import org.jvnet.hudson.test.JenkinsRule;
//import org.jvnet.hudson.test.*;
//import org.junit.*;
//import static org.junit.Assert.*;
//import hudson.model.FreeStyleProject;
//import java.util.Locale;
//import java.io.File;
//import org.openqa.selenium.*;
//import org.openqa.selenium.remote.*;
//import org.openqa.selenium.chrome.*;
//import org.openqa.selenium.chrome.ChromeDriverService.Builder;
//import org.openqa.selenium.support.ui.*;
//import io.github.bonigarcia.wdm.*;
//
//public class AnalyzerLinkTest {
//	private static WebDriver driver;
//	private static JavascriptExecutor js;
//	private static WebDriverWait wait;
//
//	@Rule
//	public JenkinsRule j = new JenkinsRule();
//
//	@BeforeClass
//	public static void startDriver() throws Exception {
//		//Web Driver Manager handling the driver binaries for us
//		ChromeDriverManager.getInstance().setup();
//
//		//Selenium chrome driver
//		driver = new ChromeDriver();
//		js = (JavascriptExecutor) driver;
//		wait = new WebDriverWait(driver, 10);
//	}
//
//	@AfterClass
//	public static void stopDriver() throws Exception {
//		driver.quit();
//	}
//
//	@Before
//	public void refreshDriver() throws Exception {
//		FreeStyleProject p = j.createFreeStyleProject();
//
//		//we don't actually care about the build at all, but it is necessary for the url system
//		p.getBuildersList().add(new FailureBuilder());
//		FreeStyleBuild build = p.scheduleBuild2(0).get();
//		j.assertBuildStatus(Result.FAILURE,build);
//
//		//nice naming consistency by the folks working on jenkins
//		//this also handles localhost:xxxx/jenkins/job/ vs site.com/job (no /jenkins)
//		String url = j.getURL() + p.getUrl();
//		String query = url + "test_results_analyzer";
//		System.out.println("url: " + query);
//
//		driver.get(query);
//	}
//
//	//mock a json data result from the server
//	private static String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};" +
//		"treeMarkup = analyzerTemplate(Obj);" +
//		"$j(\".table\").html(treeMarkup);" +
//		"addEvents();" +
//		"newFailingTests();" +
//		"reevaluateChartData = true;" + 
//		"generateCharts();";
//
//	/**
//	 *	Selenium has prerequisites outside of maven.
//	 *	1. You must have google chrome installed on the machine.
//	 *	2. If your machine is 'headless' you need to install Xvfb and have it running in the background
//	 *	3. 
//	 */
//	@Test
//	public void optionsSectionExistsTest() throws Exception {
//		Exception ex = null;
//		WebElement settingsButton = null;
//
//		//basic assertion that we have a settings menu button
//		try {
//			settingsButton = driver.findElement(By.id("settingsmenubutton"));
//		}
//		catch(NoSuchElementException e) {
//			ex = e;
//		}
//		assertEquals(null, ex);
//		//assert that the options section is hidden
//		WebElement menu = driver.findElement(By.id("settingsmenu"));
//		assertFalse(menu.isDisplayed());
//		openSettingsMenu();
//		assertTrue(menu.isDisplayed());
//		assertTrue(driver.findElement(By.id("noofbuilds")).isDisplayed());
//		assertTrue(driver.findElement(By.id("showcompilefail")).isDisplayed());
//		assertTrue(driver.findElement(By.id("show-durations")).isDisplayed());
//		assertTrue(driver.findElement(By.id("chartDataType")).isDisplayed());
//		assertTrue(driver.findElement(By.id("getbuildreport")).isDisplayed());
//	}
//
//	@Test
//	public void optionsSectionGraphTest() {
//		openSettingsMenu();
//		//enable the 'linegraph' checkbox and select the 'Test Runtimes' option
//		//then mock a json data result from the server
//		js.executeScript("$j(\"#linegraph\").prop(\"checked\",true);" + 
//						"$j(\"#bargraph\").prop(\"checked\",false);" + 
//						"$j(\"#piegraph\").prop(\"checked\",false);" + 
//						"$j(\"#chartDataType\").val(\"runtime\");");
//
//		//assert that the bar and pie checkboxes are disabled, while linegraph is enabled
//		assertTrue(driver.findElement(By.id("linegraph")).isEnabled());
//
//		//load mock data
//		js.executeScript(OptionsTest.javaScriptCommand);
//
//		//verify that the table has been updated
//		//this jQuery script looks for the string "edu.illinois.cs427.mp3" within the element with id "tree"
//		assertNotEquals(0, js.executeScript("return jQuery('#tree:contains(\"edu.illinois.cs427.mp3\")').size();"));
//
//		//verify that the linechart element is not empty and contains the string "runtime"
//		WebElement lineChart = driver.findElement(By.id("linechart"));
//		WebElement barChart = driver.findElement(By.id("barchart"));
//		WebElement pieChart = driver.findElement(By.id("piechart"));
//		assertNotEquals(null, lineChart.getAttribute("data-highcharts-chart"));
//		assertNotEquals("", lineChart.getAttribute("innerHTML"));
//		assertNotEquals(0, js.executeScript("return jQuery('#linechart:contains(\"Runtime\")').size();"));
//
//		//verify that other graphs are not generated
//		assertEquals("", barChart.getAttribute("innerHTML"));
//		assertEquals("", pieChart.getAttribute("innerHTML"));
//
//		//now uncheck 'line' and verify the graph is no longer present
//		js.executeScript("jQuery('#linegraph').prop('checked', false);");
//		js.executeScript(OptionsTest.javaScriptCommand);
//		assertEquals("", lineChart.getAttribute("innerHTML"));
//
//		//now check "bar" and "pie" and switch to "passfail"
//		js.executeScript("jQuery('#bargraph').prop('checked', true);" + 
//			"jQuery('#piegraph').prop('checked', true);" + 
//			"jQuery('#chartDataType').val('passfail');");
//		js.executeScript(OptionsTest.javaScriptCommand);
//		assertEquals("", lineChart.getAttribute("innerHTML"));
//		assertNotEquals("", barChart.getAttribute("innerHTML"));
//		assertNotEquals("", pieChart.getAttribute("innerHTML"));
//		assertNotEquals(null, barChart.getAttribute("data-highcharts-chart"));
//		assertNotEquals(null, pieChart.getAttribute("data-highcharts-chart"));
//		assertNotEquals(0, js.executeScript("return jQuery('#barchart:contains(\"Build Status\")').size();"));
//		assertNotEquals(0, js.executeScript("return jQuery('#piechart:contains(\"Build details\")').size();"));
//
//		//assert that checking the 'all' button disables the input box
//		WebElement noOfBuildsCheck = driver.findElement(By.id("allnoofbuilds"));
//		WebElement noOfBuildsBox = driver.findElement(By.id("noofbuilds"));
//
//		if(noOfBuildsCheck.isSelected()) {
//			noOfBuildsCheck.click();
//		}
//		assertTrue(noOfBuildsBox.isEnabled());
//		noOfBuildsCheck.click();
//		assertFalse(noOfBuildsBox.isEnabled());
//		noOfBuildsCheck.click();
//		assertTrue(noOfBuildsBox.isEnabled());
//	}
//
//	/**
//	 *	@brief Tests the filters just above the primary table
//	 *	this includes the filter search box and pass/fail/skip boxes
//	 */
//	@Test
//	public void optionsSectionFilterTest() {
//		//load mock data
//		js.executeScript(OptionsTest.javaScriptCommand);
//
//		//filter tests
//		WebElement searchbar = driver.findElement(By.id("filter"));
//		WebElement row = driver.findElement(By.className("base-edu_illinois_cs427_mp3"));
//		WebElement failFilter = driver.findElement(By.id("failFilter"));
//		assertTrue(row.isDisplayed());
//		searchbar.sendKeys("edu");
//		assertTrue(row.isDisplayed());
//		searchbar.sendKeys("q");
//		assertFalse(row.isDisplayed());
//		searchbar.sendKeys(Keys.BACK_SPACE);
//		assertTrue(row.isDisplayed());
//
//		if(failFilter.isSelected()) {
//			failFilter.click();
//		}
//		assertFalse(row.isDisplayed());
//		failFilter.click();
//		assertTrue(row.isDisplayed());
//	}
//
//	/**
//	 *	@brief helper method for above methods
//	 *	Blocks until the options menu is fully open
//	 */
//	public void openSettingsMenu() {
//		WebElement menu = driver.findElement(By.id("settingsmenu"));
//
//		if(!menu.isDisplayed()) {
//			//clicking the settings menu button displays the options menu
//			driver.findElement(By.id("settingsmenubutton")).click();
//		}
//		//wait 2 seconds for the options menu to fully open
//		wait.until(ExpectedConditions.elementToBeClickable(By.id("getbuildreport")));
//	}
//}


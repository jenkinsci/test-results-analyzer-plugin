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
import hudson.maven.MavenModuleSet;
import org.jvnet.hudson.test.recipes.LocalData;
import java.lang.Float;
import java.util.List;
import java.lang.Thread;

public class HighChartTest {
	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

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
		setWindowWidth(1000);
		MavenModuleSet project = (MavenModuleSet) jenkinsRule.getInstance().getItem("test");
		String url = jenkinsRule.getURL() + project.getUrl();
		String query = url + "edu.illinois.cs427$mp3/test_results_analyzer";

		driver.get(query);
		openSettingsMenu();

		//ensure the 'all' box is checked
		WebElement noOfBuildsCheck = driver.findElement(By.id("allnoofbuilds"));

		if(!noOfBuildsCheck.isSelected()) {
			noOfBuildsCheck.click();
		}
		setChartsShown(true, true, true);
	}

	/**
	 *  @brief Modifies check boxes on page
	 *  Note that this will throw an exception if the settings menu has not been opened
	 */
	public void setChartsShown(boolean line, boolean bar, boolean pie) {
		WebElement linegraph = driver.findElement(By.id("linegraph"));

		if(linegraph.isSelected() != line && linegraph.isEnabled()) {
			linegraph.click();
		}
		WebElement bargraph = driver.findElement(By.id("bargraph"));

		if(bargraph.isSelected() != bar && bargraph.isEnabled()) {
			bargraph.click();
		}
		WebElement piegraph = driver.findElement(By.id("piegraph"));

		if(piegraph.isSelected() != pie && piegraph.isEnabled()) {
			piegraph.click();
		}
		driver.findElement(By.id("getbuildreport")).click();
	}

	public void setPassFailCharts() {
		Select select = new Select(driver.findElement(By.id("chartDataType")));
		select.selectByVisibleText("Passes/Failures");
		driver.findElement(By.id("getbuildreport")).click();
		wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("#tree > .table"), "Chart"));
	}

	public void setRuntimeCharts() {
		Select select = new Select(driver.findElement(By.id("chartDataType")));
		select.selectByVisibleText("Test Runtimes");
		driver.findElement(By.id("getbuildreport")).click();
		wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("#tree > .table"), "Chart"));
	}

	/**
	 *  @brief helper method for below methods
	 *  Blocks until the options menu is fully open
	 */
	public void openSettingsMenu() {
		WebElement menu = driver.findElement(By.id("settingsmenu"));
		wait.until(ExpectedConditions.elementToBeClickable(By.id("settingsmenubutton")));

		if(!menu.isDisplayed()) {
			//clicking the settings menu button displays the options menu
			driver.findElement(By.id("settingsmenubutton")).click();
		}
		//wait 2 seconds for the options menu to fully open
		wait.until(ExpectedConditions.elementToBeClickable(By.id("getbuildreport")));
	}

	@Test
	@LocalData
	public void passFailGraphNumberingTest() throws Exception {
		setPassFailCharts();
		assertNoFloatsBy(By.cssSelector(".highcharts-yaxis-labels > text"));
		assertNoFloatsBy(By.cssSelector(".highcharts-xaxis-labels > text"));
		assertNoNegativesBy(By.cssSelector(".highcharts-yaxis-labels > text"));
		assertNoNegativesBy(By.cssSelector(".highcharts-xaxis-labels > text"));
	}

	@Test
	@LocalData
	public void runtimeGraphNumberingTest() throws Exception {
		setRuntimeCharts();
		assertNoFloatsBy(By.cssSelector(".highcharts-xaxis-labels > text"));
		assertNoNegativesBy(By.cssSelector(".highcharts-xaxis-labels > text"));
	}

	@Test
	@LocalData
	public void graphResizeTest() throws Exception {
		setPassFailCharts();
		By by = By.cssSelector(".highcharts-xaxis-labels > text");
		validateWindowWidth(1600, by);
		validateWindowWidth(1200, by);
		validateWindowWidth(800, by);
		validateWindowWidth(600, by);
		validateWindowWidth(500, by);
		validateWindowWidth(400, by);
	}

	@Test
	@LocalData
	public void tableColorTest() throws Exception {
		setConfigurationColor("passedStatusColor", "Bright Green");
		setPassFailCharts();
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".passed")));
		List<WebElement> elements = driver.findElements(By.cssSelector(".table-cell.build-result.passed"));
		boolean found = false;

		for(WebElement e : elements) {
			found = true;
			assertEquals("rgba(0, 255, 0, 1)", e.getCssValue("background-color"));
		}
		assertTrue(found);
		setConfigurationColor("passedStatusColor", "Light Green (Recommended)");
		setPassFailCharts();
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".passed")));
		List<WebElement> elements2 = driver.findElements(By.cssSelector(".table-cell.build-result.passed"));

		for(WebElement e : elements2) {
			assertEquals("rgba(146, 208, 80, 1)", e.getCssValue("background-color"));
		}
	}

	@Test
	@LocalData
	public void tableTextTest() throws Exception {
		setConfigurationText("passedStatusText", "HELLO");
		setPassFailCharts();
		showRuntimes(false);
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".passed")));
		List<WebElement> elements = driver.findElements(By.cssSelector(".passed"));
		boolean found = false;

		for(WebElement e : elements) {
			found = true;
			assertEquals("HELLO", js.executeScript("return arguments[0].textContent;", e));
		}
		assertTrue(found);
		setConfigurationText("passedStatusText", "PASSED");
		setPassFailCharts();
		showRuntimes(false);
		wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".passed"), "PASSED"));
		List<WebElement> elements2 = driver.findElements(By.cssSelector(".table-cell.build-result.passed"));

		for(WebElement e : elements2) {
			assertEquals("PASSED", js.executeScript("return arguments[0].textContent;", e));
		}
	}

	public void showRuntimes(boolean show) {
		WebElement durations = driver.findElement(By.id("show-durations"));

		if(durations.isSelected() != show) {
			durations.click();
		}
		driver.findElement(By.id("getbuildreport")).click();
	}

	/**
	 *  @brief Goes to configuration page and sets text associated with given input element
	 */
	public void setConfigurationText(String element, String text) throws Exception {
		driver.get(jenkinsRule.getURL() + "configure");
		wait.until(
			ExpectedConditions.invisibilityOfElementLocated(
				By.cssSelector(".behavior-loading")
			)
		);
		WebElement passedStatusText = driver.findElement(By.name(element));
		passedStatusText.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		passedStatusText.sendKeys(Keys.BACK_SPACE);
		passedStatusText.sendKeys(text);
		WebElement noOfBuilds = driver.findElement(By.name("noOfBuilds"));
		noOfBuilds.sendKeys(Keys.ENTER);
		refreshDriver();
	}

	/**
	 *  @brief Goes to configuration page and selects from a drop down menu associated with given input element and color
	 */
	public void setConfigurationColor(String element, String color) throws Exception {
		driver.get(jenkinsRule.getURL() + "configure");
		wait.until(
			ExpectedConditions.invisibilityOfElementLocated(
				By.cssSelector(".behavior-loading")
			)
		);
		WebElement passedColor = driver.findElement(By.name(element));

		Select select = new Select(passedColor);
		select.selectByVisibleText(color);

		WebElement noOfBuilds = driver.findElement(By.name("noOfBuilds"));
		noOfBuilds.sendKeys(Keys.ENTER);
		refreshDriver();
	}

	/**
	 *  @brief Helper method for graphResizeTest method
	 */
	public void validateWindowWidth(int w, By by) {
		setWindowWidth(w);
		assertNoFloatsBy(by);
		assertNoNegativesBy(by);
	}

	public void setWindowWidth(int w) {
		Dimension dim = driver.manage().window().getSize();
		int height = dim.getHeight();
		driver.manage().window().setSize(new Dimension(w, height));
	}

	public void assertNoFloatsBy(By by) {
		List<WebElement> elements = driver.findElements(by);

		for(WebElement e : elements) {
			float value = Float.parseFloat(e.getText());
			assertEquals(value, Math.ceil(value), 0.0);
		}
	}

	public void assertNoNegativesBy(By by) {
		List<WebElement> elements = driver.findElements(by);

		for(WebElement e : elements) {
			float value = Float.parseFloat(e.getText());
			assertTrue(value >= 0.0);
		}
	}
}


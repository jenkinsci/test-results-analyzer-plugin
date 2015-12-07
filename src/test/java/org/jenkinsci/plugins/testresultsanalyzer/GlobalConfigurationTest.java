package org.jenkinsci.plugins.testresultsanalyzer;

import com.gargoylesoftware.htmlunit.html.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.openqa.selenium.support.ui.*;
import io.github.bonigarcia.wdm.*;
import java.lang.Thread;
import com.google.common.base.Predicate;

public class GlobalConfigurationTest {
	private static WebDriver driver;
	private static JavascriptExecutor js;
	private static WebDriverWait wait;

	@Rule
	public JenkinsRule j = new JenkinsRule();

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
		driver.get(j.getURL() + "configure");
		//wait 2 seconds for the page to actually fill up
		waitForPageLoad();
	}

	/**
	 *  @brief assert that all required elements are present on the configuration page
	 */
	@Test
	public void ConfigurationSectionExistsTest() throws Exception {
		Exception ex = null;

		//assert that there is a section on the page for Test Results Analyzer
		jQueryInject();
		assertNotEquals(0, js.executeScript("return jQuery('.section-header:contains(\"Test Results Analyzer\")').size();"));

		try {
			assertTrue(driver.findElement(By.name("noOfBuilds")).isDisplayed());
		}
		catch(NoSuchElementException e) {
			ex = e;
		}
		assertEquals(null, ex);

		assertTrue(driver.findElement(By.name("showAllBuilds")).isDisplayed());
		assertTrue(driver.findElement(By.name("showBuildTime")).isDisplayed());
		assertTrue(driver.findElement(By.name("showLineGraph")).isDisplayed());
		assertTrue(driver.findElement(By.name("showBarGraph")).isDisplayed());
		assertTrue(driver.findElement(By.name("showPieGraph")).isDisplayed());
		assertTrue(driver.findElement(By.name("chartDataType")).isDisplayed());
		assertTrue(driver.findElement(By.name("runTimeLowThreshold")).isDisplayed());
		assertTrue(driver.findElement(By.name("runTimeHighThreshold")).isDisplayed());

		assertTrue(driver.findElement(By.name("passedStatusColor")).isDisplayed());
		assertTrue(driver.findElement(By.name("failedStatusColor")).isDisplayed());
		assertTrue(driver.findElement(By.name("skippedStatusColor")).isDisplayed());
		assertTrue(driver.findElement(By.name("totalStatusColor")).isDisplayed());
		assertTrue(driver.findElement(By.name("runtimeStatusColor")).isDisplayed());
		assertTrue(driver.findElement(By.name("naStatusColor")).isDisplayed());

		assertTrue(driver.findElement(By.name("passedStatusText")).isDisplayed());
		assertTrue(driver.findElement(By.name("failedStatusText")).isDisplayed());
		assertTrue(driver.findElement(By.name("skippedStatusText")).isDisplayed());
		assertTrue(driver.findElement(By.name("totalStatusText")).isDisplayed());
		assertTrue(driver.findElement(By.name("runtimeStatusText")).isDisplayed());
		assertTrue(driver.findElement(By.name("naStatusText")).isDisplayed());
	}

	@Test
	public void ConfigurationSectionWorksTest() throws Exception {
		//uncheck 'showAllBuilds' and enter '19' into 'noOfBuilds'
		WebElement showAllBuilds = driver.findElement(By.name("showAllBuilds"));

		if(showAllBuilds.getAttribute("checked") != null) {
			showAllBuilds.click();
		}
		assertEquals(null, driver.findElement(By.name("showAllBuilds")).getAttribute("checked"));
		WebElement noOfBuilds = driver.findElement(By.name("noOfBuilds"));
		noOfBuilds.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		noOfBuilds.sendKeys(Keys.BACK_SPACE);
		noOfBuilds.sendKeys("19");
		noOfBuilds.sendKeys(Keys.RETURN); //causes the page to reload
		waitForPageLoad();

		//page has reloaded, previous object no longer valid
		assertEquals("19", driver.findElement(By.name("noOfBuilds")).getAttribute("value"));
		assertNotEquals("true", driver.findElement(By.name("showAllBuilds")).getAttribute("checked"));
	}
	
	@Test
	public void TextSelectionTest() throws Exception {
		TextSelectionHelper("passedStatusText", "Good");
		TextSelectionHelper("passedStatusText", "PASSED");

		TextSelectionHelper("failedStatusText", "Bad");
		TextSelectionHelper("failedStatusText", "FAILED");

		TextSelectionHelper("skippedStatusText", "IDK");
		TextSelectionHelper("skippedStatusText", "SKIPPED");
	}

	public void TextSelectionHelper(String name, String text) {
		WebElement passedStatusText = driver.findElement(By.name(name));
		passedStatusText.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		passedStatusText.sendKeys(Keys.BACK_SPACE);
		passedStatusText.sendKeys(text);
		passedStatusText.sendKeys(Keys.RETURN); //causes the page to reload
		waitForPageLoad();

		//page has reloaded, previous object no longer valid
		assertEquals(text, driver.findElement(By.name(name)).getAttribute("value"));
	}

	@Test
	public void ColorSelectionTest() throws Exception {
		ColorSelectionHelper("passedStatusColor", "Light Red");
		ColorSelectionHelper("passedStatusColor", "Light Yellow");
		ColorSelectionHelper("passedStatusColor", "Light Blue");
		ColorSelectionHelper("passedStatusColor", "Light Green");

		ColorSelectionHelper("failedStatusColor", "Light Yellow");
		ColorSelectionHelper("failedStatusColor", "Light Blue");
		ColorSelectionHelper("failedStatusColor", "Light Green");
		ColorSelectionHelper("failedStatusColor", "Light Red");

		ColorSelectionHelper("skippedStatusColor", "Light Red");
		ColorSelectionHelper("skippedStatusColor", "Light Blue");
		ColorSelectionHelper("skippedStatusColor", "Light Green");
		ColorSelectionHelper("skippedStatusColor", "Light Yellow");

		ColorSelectionHelper("totalStatusColor", "Light Red");
		ColorSelectionHelper("totalStatusColor", "Light Green");
		ColorSelectionHelper("totalStatusColor", "Light Yellow");
		ColorSelectionHelper("totalStatusColor", "Light Blue");

		ColorSelectionHelper("runtimeStatusColor", "Light Red");
		ColorSelectionHelper("runtimeStatusColor", "Light Blue");
		ColorSelectionHelper("runtimeStatusColor", "Light Green");
		ColorSelectionHelper("runtimeStatusColor", "Light Yellow");
	}

	private void ColorSelectionHelper(String name, String text) {
		WebElement passedColor = driver.findElement(By.name(name));

		Select select = new Select(passedColor);
		select.selectByVisibleText(text);

		WebElement noOfBuilds = driver.findElement(By.name("noOfBuilds"));
		noOfBuilds.sendKeys(Keys.ENTER); //causes the page to reload
		waitForPageLoad();

		WebElement passedColor2 = driver.findElement(By.name(name));

		//page has reloaded, previous object no longer valid
		assertEquals(text, passedColor2.getText());
	}

	/**
	 *  @brief Waits for the loading overlay to go away
	 */
	public void waitForPageLoad() {
		wait.until(
			ExpectedConditions.invisibilityOfElementLocated(
				By.cssSelector(".behavior-loading")
			)
		);
	}

	/**
	 *  @brief Helper method that in loads jQuery, prevents conflicts, and blocks until jQuery is completely loaded.
	 */
	public void jQueryInject() {
		//http://stackoverflow.com/a/2011990/5627893
		js.executeScript("var temp = $;" +
			"var jq = document.createElement('script');" +
			"jq.src = '/'+'/ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js';" +
			"document.getElementsByTagName('head')[0].appendChild(jq);" +
			"function checkJQuery() {" +
			"	if(window.jQuery && jQuery.ui) {" +
			"		jQuery.noConflict();" +
			"		$ = temp;" +
			"	} else {" +
			"		window.setTimeout(checkJQuery, 100);" +
			"	}" +
			"}" +
			"checkJQuery();"
		);

		//http://stackoverflow.com/a/19433318/5627893
		//http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Predicate.html
		wait.until(
			new Predicate<WebDriver>() {
				public boolean apply(WebDriver driver) {
					return ((JavascriptExecutor)driver).executeScript(
						"if(window.jQuery) {" +
						"	return 'true';" +
						"}" +
						"return 'false';"
					).equals("true");
				}
			}
		);
	}
}


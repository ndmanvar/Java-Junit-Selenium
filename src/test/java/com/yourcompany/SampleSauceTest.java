package com.yourcompany;

import com.saucelabs.common.SauceOnDemandAuthentication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.saucelabs.junit.ConcurrentParameterized;
import com.saucelabs.junit.SauceOnDemandTestWatcher;

import java.net.URL;
import java.util.LinkedList;

import static org.junit.Assert.*;

import com.saucelabs.common.SauceOnDemandSessionIdProvider;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Demonstrates how to write a JUnit test that runs tests against Sauce Labs using multiple browsers in parallel.
 * <p/>
 * The test also includes the {@link SauceOnDemandTestWatcher} which will invoke the Sauce REST API to mark
 * the test as passed or failed.
 *
 * @author Neil Manvar
 */
@RunWith(ConcurrentParameterized.class)
public class SampleSauceTest implements SauceOnDemandSessionIdProvider {

    public String username = System.getenv("SAUCE_USERNAME");
    public String accesskey = System.getenv("SAUCE_ACCESS_KEY");

    /**
     * Constructs a {@link SauceOnDemandAuthentication} instance using the supplied user name/access key.  To use the authentication
     * supplied by environment variables or from an external file, use the no-arg {@link SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(username, accesskey);

    /**
     * JUnit Rule which will mark the Sauce Job as passed/failed when the test succeeds or fails.
     */
    @Rule
    public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    @Rule public TestName name = new TestName() {
        public String getMethodName() {
        		return String.format("%s : (%s %s %s)", super.getMethodName(), os, browser, version);
        };
    };

    /**
     * Represents the browser to be used as part of the test run.
     */
    private String browser;
    /**
     * Represents the operating system to be used as part of the test run.
     */
    private String os;
    /**
     * Represents the version of the browser to be used as part of the test run.
     */
    private String version;
    /**
     * Represents the deviceName of mobile device
     */
    private String deviceName;
    /**
     * Represents the device-orientation of mobile device
     */
    private String deviceOrientation;
    /**
     * Instance variable which contains the Sauce Job Id.
     */
    private String sessionId;

    /**
     * The {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private WebDriver driver;

    /**
     * Constructs a new instance of the test.  The constructor requires three string parameters, which represent the operating
     * system, version and browser to be used when launching a Sauce VM.  The order of the parameters should be the same
     * as that of the elements within the {@link #browsersStrings()} method.
     * @param os
     * @param version
     * @param browser
     * @param deviceName
     * @param deviceOrientation
     */

    public SampleSauceTest(String os, String version, String browser, String deviceName, String deviceOrientation) {
        super();
        this.os = os;
        this.version = version;
        this.browser = browser;
        this.deviceName = deviceName;
        this.deviceOrientation = deviceOrientation;
    }

    /**
     * @return a LinkedList containing String arrays representing the browser combinations the test should be run against. The values
     * in the String array are used as part of the invocation of the test constructor
     * @throws JSONException 
     */
    @ConcurrentParameterized.Parameters
    public static LinkedList browsersStrings() throws JSONException {
        LinkedList browsers = new LinkedList();
        String browsersJSONArrayString = System.getenv("SAUCE_ONDEMAND_BROWSERS");
        JSONArray browsersJSONArrayObj = new JSONArray(browsersJSONArrayString);
        
        for (int i=0; i<browsersJSONArrayObj.length(); i++) {
            JSONObject browserObj = (JSONObject)browsersJSONArrayObj.getJSONObject(i);
            browsers.add(new String[]{browserObj.getString("os"), browserObj.getString("browser-version"), browserObj.getString("browser"), null, null});
        }

        return browsers;
    }


    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the {@link #browser},
     * {@link #version} and {@link #os} instance variables, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @throws Exception if an error occurs during the creation of the {@link RemoteWebDriver} instance.
     */
    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        if (browser != null) capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
        if (version != null) capabilities.setCapability(CapabilityType.VERSION, version);
        if (deviceName != null) capabilities.setCapability("deviceName", deviceName);
        if (deviceOrientation != null) capabilities.setCapability("device-orientation", deviceOrientation);

        capabilities.setCapability(CapabilityType.PLATFORM, os);

        String methodName = name.getMethodName();
        capabilities.setCapability("name", methodName);

        this.driver = new RemoteWebDriver(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() +
                        "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities);
        this.sessionId = (((RemoteWebDriver) driver).getSessionId()).toString();

        String message = String.format("SauceOnDemandSessionID=%1$s job-name=%2$s", this.sessionId, methodName);
        System.out.println(message);
    }

    /**
     * Runs a simple test verifying the UI and title of the belk.com home page.
     * @throws Exception
     */
    @Test
    public void verifyBelkHompage() throws Exception {
        driver.get("http://www.belk.com");
        WebDriverWait wait = new WebDriverWait(driver, 10); // wait for a maximum of 5 seconds
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".primary-nav")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".promo-utility")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".logo")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#shoppingBagPlaceHolder")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#global_search_box")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".container_24")));

        assertTrue(driver.getTitle().equals("Home - belk.com - Belk.com"));
    }

    /**
     * Go to belk.com, click sigin/register in top bar, and verify UI
     * @throws Exception
     */
    @Test
    public void verifySignInRegisterPage() throws Exception {
        driver.get("http://www.belk.com");
        WebDriverWait wait = new WebDriverWait(driver, 10); // wait for a maximum of 5 seconds
        WebElement signInRegisterLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".hide-logged-in a")));
        signInRegisterLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("returningRadio")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[value='2']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("txt_email_address_n")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("txt_email_address_n")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("txt_password_n")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("forgot_Password")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("#signInButton")));

        assertTrue(driver.getTitle().equals("Sign In/Register - Belk.com"));
        assertTrue(driver.getCurrentUrl().equals("https://www.belk.com/AST/Misc/Belk_Stores/Global_Navigation/Sign_In_Register.jsp"));
    }

    /**
     * Closes the {@link WebDriver} session.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    /**
     *
     * @return the value of the Sauce Job id.
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }
}

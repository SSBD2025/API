package pl.lodz.p.it.ssbd2025.ssbd02.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MOK8Test {
    private WebDriver webDriver;
    private String username;
    private String password;
    private String url1;
    private String url2;
    private String url3;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.cache.disk.enable", false);
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.setProfile(profile);
        webDriver = new FirefoxDriver(options);
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        username = "kzachod";
        password = "P@ssw0rd!";
        url1 = "https://team-2.proj-sum.it.p.lodz.pl/login";
        url2 = "https://team-2.proj-sum.it.p.lodz.pl";
        url3 = "https://team-2.proj-sum.it.p.lodz.pl/me";
    }

    @Test
    public void changePasswordTest() {
        webDriver.get(url1);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='login']")));

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[1]/input")).sendKeys(username);

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[2]/input")).sendKeys(password);

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[3]/button")).click();

        By dashboardLink = By.xpath("//a[@href='/client/pyramids' and contains(text(), 'Diet Profiles')]");
        wait.until(ExpectedConditions.presenceOfElementLocated(dashboardLink));
        webDriver.get(url2);
        Assertions.assertTrue(webDriver.findElement(dashboardLink).isDisplayed());

        //przejscie na stronę własnego konta
        webDriver.findElement(By.xpath("/html/body/div/div/nav/div/div/div[3]/div[1]/button")).click();
        webDriver.findElement(By.xpath("/html/body/div[2]/div/a")).click();

        webDriver.get(url3);

        //zmiana wlasnego hasla
        By editButton = By.xpath("//button[contains(text(), 'Change password')]");
        wait.until(ExpectedConditions.elementToBeClickable(editButton)).click();
        By oldPasswordInput = By.xpath("//input[@placeholder='Old password']");
        webDriver.findElement(oldPasswordInput).sendKeys("P@ssw0rd!");
        By newPassword = By.xpath("//input[@placeholder='New password']");
        webDriver.findElement(newPassword).sendKeys("N0w3h4sl0!");
        By confirmPassword = By.xpath("//input[@placeholder='Confirm password']");
        webDriver.findElement(confirmPassword).sendKeys("N0w3h4sl0!");
        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div/div[2]/div/div/div[2]/div[4]/form/button")).click();
        webDriver.findElement(By.xpath("/html/body/div[3]/div[2]/button[2]")).click();

        //wylogowanie
        webDriver.findElement(By.xpath("/html/body/div/div/nav/div/div/div[3]/div[1]/button")).click();
        webDriver.findElement(By.xpath("/html/body/div[2]/div/div[4]")).click();

        //zalogowanie z uzyciem nowego hasla
        webDriver.get(url1);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='login']")));

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[1]/input")).sendKeys(username);

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[2]/input")).sendKeys("N0w3h4sl0!");

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[3]/button")).click();

        By dashboardLink2 = By.xpath("//a[@href='/client/pyramids' and contains(text(), 'Diet Profiles')]");
        wait.until(ExpectedConditions.presenceOfElementLocated(dashboardLink2));
        webDriver.get(url2);
        Assertions.assertTrue(webDriver.findElement(dashboardLink).isDisplayed());

        webDriver.findElement(By.xpath("/html/body/div/div/nav/div/div/div[3]/div[1]/button")).click();
        webDriver.findElement(By.xpath("/html/body/div[2]/div/div[4]")).click();
    }

    @AfterEach
    public void tearDown() {
        try {
            webDriver.quit();
        } catch (Exception ignored) {

        }
    }
}
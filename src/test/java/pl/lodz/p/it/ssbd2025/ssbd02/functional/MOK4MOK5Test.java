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

public class MOK4MOK5Test {
    private WebDriver webDriver;
    private String username;
    private String password;
    private String url1;
    private String url2;
    private String url3;
    private String url4;
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
        username = "jcheddar";
        password = "P@ssw0rd!";
        url1 = "https://team-2.proj-sum.it.p.lodz.pl/login";
        url2 = "https://team-2.proj-sum.it.p.lodz.pl";
        url3 = "https://team-2.proj-sum.it.p.lodz.pl/admin/dashboard";
        url4 = "https://team-2.proj-sum.it.p.lodz.pl/admin/dashboard/users/00000000-0000-0000-0000-000000001337";
    }

    @Test
    public void blockUnblockAccountTest() {
        webDriver.get(url1);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='login']")));

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[1]/input")).sendKeys(username);

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[2]/input")).sendKeys(password);

        webDriver.findElement(By.xpath("/html/body/div/div/div/div/div[2]/div/form/div[3]/button")).click();

        By dashboardLink = By.xpath("//a[@href='/admin/dashboard' and contains(text(), 'Dashboard')]");
        wait.until(ExpectedConditions.presenceOfElementLocated(dashboardLink));
        webDriver.get(url2);
        Assertions.assertTrue(webDriver.findElement(dashboardLink).isDisplayed());

        //przejscie na admin dashboard
        webDriver.findElement(By.xpath("/html/body/div/div/nav/div/div/a")).click();

        webDriver.get(url3);

        //wybór klienta z listy
        By search = By.xpath("/html/body/div/div/main/div[1]/div[1]/div/div/input");
        wait.until(ExpectedConditions.presenceOfElementLocated(search));
        webDriver.findElement(search).sendKeys("Kamil");
        webDriver.findElement(By.xpath("/html/body/div/div/main/div[1]/div[2]/div/div/table/tbody/tr[6]/td[6]/a/button")).click();

        //bolokowanie konta
        webDriver.get(url4);
        By button = By.xpath("/html/body/div/div/div/div/div/div[2]/div[2]/div[1]/div/div[2]/div/div[1]/div/button");
        wait.until(ExpectedConditions.presenceOfElementLocated(button));
        webDriver.findElement(button).click();
        By button2 = By.xpath("/html/body/div[3]/div[2]/button[2]");
        wait.until(ExpectedConditions.presenceOfElementLocated(button2));
        webDriver.findElement(button2).click();

        //sprawdzamy czy zablokowany
        By blockedText = By.xpath("//p[contains(@class, 'text-sm') and contains(@class, 'text-muted-foreground') and text()='Blocked']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(blockedText));
        Assertions.assertTrue(webDriver.findElement(blockedText).isDisplayed());

        //odblokowanie użytkownika
        By unblockButton = By.xpath("/html/body/div/div/div/div/div/div[2]/div[2]/div[1]/div/div[2]/div/div[1]/button");
        wait.until(ExpectedConditions.presenceOfElementLocated(unblockButton));
        webDriver.findElement(unblockButton).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(button2));
        webDriver.findElement(button2).click();

        //sprawdzenie czy odblokowany
        By unblockedText = By.xpath("//p[contains(@class, 'text-sm') and contains(@class, 'text-muted-foreground') and text()='Active']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(unblockedText));
        Assertions.assertTrue(webDriver.findElement(unblockedText).isDisplayed());

        //wylogowanie
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
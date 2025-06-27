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

    @AfterEach
    public void tearDown() {
        try {
            webDriver.quit();
        } catch (Exception ignored) {

        }
    }
}
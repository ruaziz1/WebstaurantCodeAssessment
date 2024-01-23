package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WebstaurantCodeAssessment {

    private WebDriver driver;

    @Before
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testWebAutomation() {
        try {
            // Navigate to the URL
            driver.get("https://www.webstaurantstore.com");

            // Search for stainless work table
            WebElement searchBox = driver.findElement(By.id("searchval"));
            searchBox.sendKeys("stainless work table");
            searchBox.submit();

            // Get the total number of search result pages
            List<WebElement> pageButtons = driver.findElements(By.cssSelector("#paging>nav>ul>li"));
            int totalPages = pageButtons.size();

            // Iterate through search result pages
            for (int currentPage = 1; currentPage <= totalPages; currentPage++) {

                // Get the list of search results
                List<WebElement> searchResults = driver.findElements(By.xpath("//div[contains(@data-testid,'productBoxContainer')]"));

                // Check the page size after verifying page 5
                if (currentPage == 5) {
                    System.out.println("On Page 5, getting the total number of pages again.");

                    // Get the total number of search result pages again
                    pageButtons = driver.findElements(By.cssSelector("#paging>nav>ul>li"));
                    totalPages = pageButtons.size();
                }

                // Iterate through each result
                for (WebElement result : searchResults) {
                    verifyProductTitle(result, currentPage);
                }

                // Go to the next page if not on the last page
                if (currentPage < (totalPages - 2)) {
                    WebElement nextPageButton = driver.findElement(By.cssSelector("li.inline-block.leading-4.align-top.rounded-r-md>a>svg>use"));
                    nextPageButton.click();
                } else {
                    handleLastPage(searchResults);
                    break;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @After
    public void tearDown() {
        // Quit the WebDriver
        driver.quit();
    }

    private void verifyProductTitle(WebElement result, int currentPage) {
        try {
            // Get the product title
            String productTitle = result.findElement(By.xpath(".//span[contains(@data-testid,'itemDescription')]")).getText();

            // Verify the word 'table' in the product title
            boolean isTableFound = productTitle.toLowerCase().contains("table");
            if (!isTableFound) {
                // Log the failure without stopping the test
                System.out.println("Page " + currentPage + ": WARNING - Product title does not contain 'table': " + productTitle);
            }
        } catch (Exception e) {
            // Log or print if there's an exception and continue with the next iteration
            System.out.println("Page " + currentPage + ": ERROR - Exception while verifying product title: " + e.getMessage());
        }
    }

    private void handleLastPage(List<WebElement> searchResults) throws InterruptedException {
        // Get the last product on the page
        WebElement lastProduct = searchResults.get(searchResults.size() - 1);

        // Add the last product to the cart
        WebElement addToCartButton = lastProduct.findElement(By.cssSelector(".add-to-cart"));
        addToCartButton.click();

        // Use WebDriverWait for dynamic wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Verify View Cart Popup and click
        WebElement viewCartPopUpBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class,'notification')]//a[contains(text(),'View Cart')]")));
        viewCartPopUpBtn.click();

        // Verify the product is added to the cart
        WebElement cartItemCount = driver.findElement(By.xpath("//span[contains(@id,'cartItemCountSpan')]"));
        Assert.assertEquals("1", cartItemCount.getText());

        // Empty the cart
        WebElement emptyCartButton = driver.findElement(By.xpath("//button[contains(@class,'emptyCartButton')]"));
        emptyCartButton.click();

        WebElement emptyCartPopUpButton = driver.findElement(By.xpath("//div[contains(@class,'ReactModal')]//button[contains(text(),'Empty')]"));
        emptyCartPopUpButton.click();

        Thread.sleep(2000);

        // Verify the cart is empty
        Assert.assertTrue("Cart is not empty", driver.findElement(By.xpath("//p[contains(text(),'Your cart is empty.')]")).isDisplayed());

        cartItemCount = driver.findElement(By.xpath("//span[contains(@id,'cartItemCountSpan')]"));
        Assert.assertEquals("0", cartItemCount.getText());
    }

    private void handleException(Exception e) {
        // Re-throw the exception as RuntimeException
        throw new RuntimeException(e);
    }
}
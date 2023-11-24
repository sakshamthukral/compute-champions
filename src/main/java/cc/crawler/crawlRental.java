package cc.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.main.*;
import cc.utils.config;
public class crawlRental {
    public static final String WEBSITE="https://rentals.ca";
//    public static final String HTMLFolderPath=config.HTMLFolderPathRental;
    private static void threadWait(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void driveCrawling(WebDriver driver, WebDriverWait wait, int numPages, String inputKeyword){
        driver.get(WEBSITE);
        driver.manage().window().maximize();
        Main.createFolderIfNotExists(config.HTMLFolderPathRental+"/"+inputKeyword);

        switch (inputKeyword) {
            case "Toronto", "toronto" ->
                    driver.get(WEBSITE+"/toronto"); // For searching rentals in Toronto
            case "Windsor", "windsor" -> driver.get(WEBSITE+"/windsor-on");
            case "Winnipeg", "winnipeg" -> driver.get(WEBSITE+"/winnipeg");
            case "Montreal", "montreal" -> driver.get(WEBSITE+"/montreal");
            case "Calgary", "calgary" -> driver.get(WEBSITE+"/calgary");
            case "Ottawa", "ottawa" -> driver.get(WEBSITE+"/ottawa");
            case "Edmonton", "edmonton" -> driver.get(WEBSITE+"/edmonton");
            case "Vancouver", "vancouver" -> driver.get(WEBSITE+"/vancouver");
            case "Brampton", "brampton" -> driver.get(WEBSITE+"/brampton");
            case "Hamilton", "hamilton" -> driver.get(WEBSITE+"/hamilton");
            case "Surrey", "surrey" -> driver.get(WEBSITE+"/surrey");
            case "Quebec-city", "quebec-city" -> driver.get(WEBSITE+"/quebec-city");
            case "Halifax", "halifax" -> driver.get(WEBSITE+"/halifax");
            case "Laval", "laval" -> driver.get(WEBSITE+"/laval");
            case "London", "london" -> driver.get(WEBSITE+"/london");
        }

        int page=1;
        while(page<=numPages){
            System.out.println("##############################################################################");
            System.out.println("Page Number: "+page);
            threadWait(15000);
            String currentPageUrl = driver.getCurrentUrl();
            System.out.println(currentPageUrl);

            List<String> links = new ArrayList<>();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.listing-card__details-link")));
            List<WebElement> leads = driver.findElements(By.cssSelector("a.listing-card__details-link")); // Getting links to all the leads visible on Page-1
            for(WebElement lead: leads){
                String link = lead.getAttribute("href");
                Pattern pattern = Pattern.compile(config.linkRegex);
                Matcher matcher = pattern.matcher(link);
                if(matcher.matches()){
                    links.add(link);
                }
            }

            int i=1;
            for(String link: links){
                driver.get(link);
                String htmlContent = driver.getPageSource();
                threadWait(5000);

                String fileName = config.HTMLFolderPathRental+"/"+inputKeyword+"/page_"+page+"_listing_" + i + ".html";
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    fileWriter.write(htmlContent);
                    System.out.println("HTML content of " + link + " saved to " + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//
                threadWait(5000);
                i++;
            }
            page++;
            driver.get(currentPageUrl);
            threadWait(5000);

            try{
                WebElement nextPage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[data-msgid='Next']")));
                nextPage.click();
            } catch (TimeoutException e) {
                System.err.println("Next page button not found within the specified timeout: " + e.getMessage());
                break;  // Break the loop if the button is not found
            } catch(NoSuchElementException e){
                System.err.println("Next page not found: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.err.println("An unexpected exception occurred: " + e.getMessage());
                e.printStackTrace();  // Print the full stack trace for debugging
                break;
            }
        }
        driver.quit();
        System.out.println("Crawling of Rental Leads Complete Successfully!!");

    }
    public static void main(String[] args){
//        Scanner sc = new Scanner(System.in);
//        System.out.print("Enter the number of pages(<5 in order to prevent longer running times) you want to scrape: ");
//        int numPages = sc.nextInt();
//        sc.nextLine();
//        System.out.print("Enter the city where you are looking for rentals: ");
//        String city = sc.nextLine();
////        List<List<String>> rentalLeads = new ArrayList<>();
//
//        System.setProperty("webdriver.chrome.driver","E:/Semester-1/ACC/RealEstateCrawler/chromedriver-win64/chromedriver-win64/chromedriver.exe");
//        ChromeOptions options = new ChromeOptions();
//        options.setBinary("E:/Semester-1/ACC/RealEstateCrawler/chrome-win64/chrome-win64/chrome.exe");
//        options.addArguments("--deny-permission-prompts");
////        options.addArguments("--headless");
//        WebDriver driver = new ChromeDriver(options);
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
//
//        driveCrawling(driver,wait, numPages, city);

    }
}

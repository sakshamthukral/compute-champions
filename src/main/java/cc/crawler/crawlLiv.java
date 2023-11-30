package cc.crawler;

import cc.utils.config;
import cc.utils.helper;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class crawlLiv {
    public static final String WEBSITE="https://liv.rent/";
//    public static final String HTMLFolderPath="crawled_liv.rent";
//    private static void createFolder(){
//        File folder = new File(HTMLFolderPath);
//        if (!folder.exists()) {
//            boolean folderCreated = folder.mkdirs();
//            if (folderCreated) {
//                System.out.println("Folder created: " + HTMLFolderPath);
//            } else {
//                System.err.println("Failed to create folder: " + HTMLFolderPath);
//            }
//        }
//    }
    private static void threadWait(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean driveCrawling(WebDriver driver, WebDriverWait wait, int numPages, String inputKeyword){
        boolean gotLeads=true;
        driver.get(WEBSITE);
        driver.manage().window().maximize();
        helper.createFolderIfNotExists(config.descriptionLiv+"/"+inputKeyword);

        driver.findElement(By.cssSelector("button.sc-9b749994-8.lhKUEI")).click(); // Clicking on Allowing cookies when pop-up comes
        switch (inputKeyword) {
            case "Toronto", "toronto" ->
                    driver.findElement(By.cssSelector("div.sc-ece85b1a-0.hHScca")).click(); // For searching rentals in Toronto
            case "Windsor", "windsor" -> driver.get("https://liv.rent/rental-listings/city/windsor-on");
            case "Winnipeg", "winnipeg" -> driver.get("https://liv.rent/rental-listings/city/winnipeg");
        }


        int page=1;
        while(page<=numPages){
            System.out.println("##############################################################################");
            System.out.println("Page Number: "+page);
            threadWait(6000);
            String currentPageUrl = driver.getCurrentUrl();
            System.out.println(currentPageUrl);

            List<String> links = new ArrayList<>();
            List<WebElement> leads;
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.sc-e20004cf-0.cgdhUn"))); // Waiting for the next page to appear
                leads = driver.findElements(By.cssSelector("a.sc-e20004cf-0.cgdhUn")); // Getting links to all the leads visible on Page-1
            } catch (TimeoutException e) {
                System.err.println("Leads not found!!");
                gotLeads=false;
                break;
            }

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
                threadWait(8000);
                try {
                    WebElement readMoreBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p.sc-d2c88f17-0.sc-d2c88f17-1.iybCNV.inA-dsu")));
                    readMoreBtn.click();
                } catch (NoSuchElementException e) {
                    // Handle the case when "Read More" button is not found
                    System.out.println("Read More button not found. Skipping...");
                } catch (TimeoutException e) {
                    System.out.println("Read More button not found. Skipping...");
                } catch (Exception e) {
                    System.out.println("Read More button not found. Skipping...");
                }
                threadWait(5000);
                WebElement description = driver.findElement(By.cssSelector("p.sc-d2c88f17-0.iybCNV"));
                String descriptionText = description.getText();
                descriptionText = descriptionText+"\n<<<"+link+">>>";

                String descriptionFile = config.descriptionLiv+"/"+inputKeyword +"/page_"+page+"_listing_" + i + ".txt";
                try (FileWriter fileWriter = new FileWriter(descriptionFile)) {
                    fileWriter.write(descriptionText);
                    System.out.println("Description of " + link + " saved to " + descriptionFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String fileName = config.HTMLFolderPathLiv+"/"+inputKeyword +"/page_"+page+"_listing_" + i + ".html";
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    fileWriter.write(htmlContent);
                    System.out.println("HTML content of " + link + " saved to " + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                threadWait(5000);
                i++;
            }
            page++;
            driver.get(currentPageUrl);
            threadWait(10000);

            try{
                WebElement nextPage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button#next-page")));
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

//        System.out.println("Crawling of Rental Leads Complete Successfully!!");
        return gotLeads;

    }
    public static void main(String[] args){
//        Scanner sc = new Scanner(System.in);
//        System.out.print("Enter the number of pages(<5 in order to prevent longer running times) you want to scrape: ");
//        int numPages = sc.nextInt();
//        System.out.print("Enter the city where you are looking for rentals: ");
//        String city = sc.nextLine();
//        List<List<String>> rentalLeads = new ArrayList<>();
//
//        System.setProperty("webdriver.chrome.driver","E:/Semester-1/ACC/RealEstateCrawler/chromedriver-win64/chromedriver-win64/chromedriver.exe");
//        ChromeOptions options = new ChromeOptions();
//        options.setBinary("E:/Semester-1/ACC/RealEstateCrawler/chrome-win64/chrome-win64/chrome.exe");
//        options.addArguments("--deny-permission-prompts");
//        options.addArguments("--headless");
//        WebDriver driver = new ChromeDriver(options);
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
//
//        driveCrawling(driver,wait, numPages, city);

    }

}

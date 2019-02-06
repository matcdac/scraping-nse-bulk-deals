package debryan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import debryan.util.ExceptionLogUtil;
import debryan.util.JsonUtil;

public class Start {

	private static final String MONEY_CONTROL_BULK_DEALS_URL = "https://www.moneycontrol.com/stocks/marketstats/blockdeals/";
	
	private static final int MARKET_CLOSING_TIME_IN_AGGREGATED_MINUTES = 931;

	private static final String WEBDRIVER_CHROME_PROPERTY_NAME = "webdriver.chrome.driver";
	private static final String WEBDRIVER_CHROME_PROPERTY_VALUE_PATH = "/space/tools/chromedriver";

	private static final String BASE_PATH = "/space/bulk-deal-stock-data/";
	private static String todaysDate = null;
	
	private static int fileCount = 0;

	private static Map<Integer, BulkDeal> bulkDeals = new HashMap<Integer, BulkDeal>();

	private static final String COMPANY_NAME = "Company Name";
	private static final String EXCHANGE = "Exchange";
	private static final String SECTOR = "Sector";
	private static final String QUANTITY = "Quantity";
	private static final String PRICE = "Price";
	private static final String VALUE = "Value (Cr)";
	private static final String TIME = "Time";

	private static final String DIV_CLASS = "hist_tbl";
	private static final String TABLE_BODY = "tbody";
	private static final String TABLE_ROW = "tr";
	private static final String TABLE_DATA = "td";
	private static final String ANCHOR = "a";

	private static JsonUtil<BulkDeal> bulkDealJsonUtil = new JsonUtil<BulkDeal>();

	private static String getTodaysFormattedDate() {
		Date date = new Date();
		int year = date.getYear() + 1900;
		int month = date.getMonth() + 1;
		int day = date.getDate();
		String formattedDate = year + "-";
		formattedDate += (month > 9) ? month : ("0" + month);
		formattedDate += "-";
		formattedDate += (day > 9) ? day : ("0" + day);
		return formattedDate;
	}
	
	private static void loadFile(int fileNumber, String fileName) throws Exception {
		File file = new File(fileName);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String json = new String();
		String line = null;
		while(null != (line = bufferedReader.readLine())) {
			json += line;
		}
		bufferedReader.close();
		fileReader.close();
		BulkDeal bulkDeal = bulkDealJsonUtil.convertJsonToObject(json, BulkDeal.class);
		bulkDeals.put(fileNumber, bulkDeal);
	}
	
	private static void loadBulkDealFile(int index) {
		++index;
		String fileName = BASE_PATH + todaysDate + "/" + index + ".json";
		try {
			loadFile(index, fileName);
		} catch (Exception e) {
			ExceptionLogUtil.logException(e, "Error in Loading JSON file " + index + " of Bulk Deal");
		}
	}

	private static void loadExistingBulkDealsData(File folder) {
		String[] fileNames = folder.list();
		IntStream intStream = IntStream.range(0, fileNames.length).sequential();
		intStream.forEach(index -> loadBulkDealFile(index));
		fileCount = fileNames.length;
	}

	private static void countFilesInPath() {
		File folder = new File(BASE_PATH + todaysDate);
		if (!folder.exists()) {
			folder.mkdirs();
		} else {
			loadExistingBulkDealsData(folder);
		}
	}

	private static void init() {
		System.setProperty(WEBDRIVER_CHROME_PROPERTY_NAME, WEBDRIVER_CHROME_PROPERTY_VALUE_PATH);
		todaysDate = getTodaysFormattedDate();
		countFilesInPath();
	}

	static {
		init();
	}

	private static void extractTransformLoadData(int index, WebElement webElementTableRow) {
		BulkDeal bulkDeal = new BulkDeal();
		
		++index;
		
		List<WebElement> webElementTableDatas = webElementTableRow.findElements(By.tagName(TABLE_DATA));
		WebElement webElementTableDataCompanyName = webElementTableDatas.get(0);
		List<WebElement> webElementTableDataCompanyNameAnchors = webElementTableDataCompanyName.findElements(By.tagName(ANCHOR));
		bulkDeal.setCompanyName(webElementTableDataCompanyNameAnchors.get(0).getText());
		WebElement webElementTableDataExchange = webElementTableDatas.get(1);
		bulkDeal.setExchange(webElementTableDataExchange.getText());
		WebElement webElementTableDataSector = webElementTableDatas.get(2);
		bulkDeal.setSector(webElementTableDataSector.findElement(By.tagName(ANCHOR)).getText());
		WebElement webElementTableDataQuantity = webElementTableDatas.get(3);
		bulkDeal.setQuantity(webElementTableDataQuantity.getText());
		WebElement webElementTableDataPrice = webElementTableDatas.get(4);
		bulkDeal.setPrice(webElementTableDataPrice.getText());
		WebElement webElementTableDataValue = webElementTableDatas.get(5);
		bulkDeal.setValue(webElementTableDataValue.getText());
		WebElement webElementTableDataTime = webElementTableDatas.get(6);
		bulkDeal.setTime(webElementTableDataTime.getText());
		
		bulkDeals.put(index, bulkDeal);
	}

	private static Map<Integer, BulkDeal> getNewBulkDealsData() {
		WebDriver webDriver = new ChromeDriver();
		
		webDriver.get(MONEY_CONTROL_BULK_DEALS_URL);
		webDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		
		WebElement webElementDiv = webDriver.findElement(By.className(DIV_CLASS));
		WebElement webElementTableBody = webElementDiv.findElement(By.tagName(TABLE_BODY));
		List<WebElement> webElementTableRows = webElementTableBody.findElements(By.tagName(TABLE_ROW));
		
		IntStream intStream = IntStream.range(fileCount, webElementTableRows.size()).parallel();
		intStream.forEach(index -> extractTransformLoadData(index, webElementTableRows.get(index)));

		webDriver.close();
		
		return bulkDeals;
	}

	private static void writeDataInFile(int i, BulkDeal bulkDeal) throws Exception {
		String fileName = BASE_PATH + todaysDate + "/" + i + ".json";
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		String json = bulkDealJsonUtil.convertObjectToJson(bulkDeal);
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(json);
		fileWriter.close();
	}

	private static void sendSMS(int i, BulkDeal bulkDeal) {
		// TODO Auto-generated method stub
		
	}

	private static void proceedToWriteAndNotify(int i, BulkDeal bulkDeal) {
		try {
			writeDataInFile(i, bulkDeal);
			fileCount++;
		} catch (Exception e) {
			ExceptionLogUtil.logException(e, "Error in Creating JSON file " + i + " of Bulk Deal");
		}
		sendSMS(i, bulkDeal);
	}

	private static void process() {
		getNewBulkDealsData();
		int entriesCount = bulkDeals.keySet().size();
		if (entriesCount > fileCount) {
			for (int i = fileCount + 1; i <= entriesCount; i++) {
				proceedToWriteAndNotify(i, bulkDeals.get(i));
			}
		}
	}

	private static int getAggregatedMinutesOfTodayTillNow() {
		Date date = new Date();
		return date.getHours()*60 + date.getMinutes();
	}

	public static void main(String[] args) {
		while ( getAggregatedMinutesOfTodayTillNow() < MARKET_CLOSING_TIME_IN_AGGREGATED_MINUTES ) {
			process();
		}
	}

}

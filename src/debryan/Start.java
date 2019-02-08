package debryan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
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
	
	private static final String[] TIME_ENTITIES = {"MilliSecond", "Second", "Minute", "Hour", "Day"};

	private static final String WEBDRIVER_CHROME_PROPERTY_NAME = "webdriver.chrome.driver";
	private static final String WEBDRIVER_CHROME_PROPERTY_VALUE_PATH = "/space/tools/chromedriver";

	private static final String BASE_PATH = "/space/bulk-deal-stock-data/";
	private static String todaysDate = null;
	
	private static int fileCount = 0;
	
	private static int pass = 0;
	
	private static final String SMS_URL = "http://www.160by2.com/";
	
	private static final String LOGIN_MOBILE_NUMBER = "";
	private static final String LOGIN_PASSWORD = "";
	
	private static final String[] SMS_RECEIVERS = {"8299237242", "7829400970"};

	private static Map<Integer, BulkDeal> bulkDeals = new HashMap<Integer, BulkDeal>();
	
	private static JsonUtil<BulkDeal> bulkDealJsonUtil = new JsonUtil<BulkDeal>();

	// MoneyControl

	private static final String DIV_CLASS = "hist_tbl";
	private static final String TABLE_BODY = "tbody";
	private static final String TABLE_ROW = "tr";
	private static final String TABLE_DATA = "td";
	private static final String ANCHOR = "a";
	
	// 160by2
	private static final String USERNAME_ID = "username";
	private static final String PASSWORD_ID = "password";
	private static final String LOGIN_BUTTON_CLASS = "ind-but-butns";
	
	private static final String CANCEL_POPUP_ID = "onesignal-popover-cancel-button";
	private static final String CLOSE_POPUP_CLASS = "pop-close";
	
	private static final String IFRAME_ID = "by2Frame";
	private static final String FORM_ID = "frm_sendsms";
	private static final String SMS_PANEL_DIV_CLASS = "sms-left";
	
	private static final String TO_DIV_CLASS = "sms-nu-row";
	private static final String INPUT = "input";
	private static final String CONTENT_ID = "sendSMSMsg";
	private static final String SEND_BUTTON_ID = "btnsendsms";
	
	private static final String LOGOUT_BUTTON = "top-log";

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
		webDriver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
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

	private static void loginToSMSPortal(WebDriver webDriver) {
		WebElement webElementInputUsername = webDriver.findElement(By.id(USERNAME_ID));
		webElementInputUsername.sendKeys(LOGIN_MOBILE_NUMBER);
		WebElement webElementInputPassword = webDriver.findElement(By.id(PASSWORD_ID));
		webElementInputPassword.sendKeys(LOGIN_PASSWORD);
		WebElement webElementLoginButton = webDriver.findElement(By.className(LOGIN_BUTTON_CLASS));
		webElementLoginButton.click();
	}
	
	private static void cancelPopup(WebDriver webDriver) throws Exception {
		WebElement webElementCancelPopupButton = webDriver.findElement(By.id(CANCEL_POPUP_ID));
		boolean isNull = null == webElementCancelPopupButton;
		boolean isDisplayed = webElementCancelPopupButton.isDisplayed();
		System.out.println("popup details -> (isNull) " + isNull + " (isDisplayed) " + isDisplayed);
		if (!isNull && isDisplayed) {
			webElementCancelPopupButton.click();
			System.out.println("popup canceled");
		} else {
			System.out.println("popup didn't cancel");
		}
	}

	private static void closePopup(WebDriver webDriver) throws Exception {
		WebElement webElementClosePopupButton = webDriver.findElement(By.className(CLOSE_POPUP_CLASS));
		boolean isNull = null == webElementClosePopupButton;
		boolean isDisplayed = webElementClosePopupButton.isDisplayed();
		System.out.println("popup details -> (isNull) " + isNull + " (isDisplayed) " + isDisplayed);
		if (!isNull && isDisplayed) {
			webElementClosePopupButton.click();
			System.out.println("popup closed");
		} else {
			System.out.println("popup didn't close");
		}
	}

	private static String getReadableMsgContent(BulkDeal bulkDeal) {
		String msgContent = bulkDealJsonUtil.convertObjectToJson(bulkDeal);
		//msgContent = msgContent.replaceAll("{", "");
		//msgContent = msgContent.replaceAll("}", "");
		//msgContent = msgContent.replaceAll("\"", "");
		return msgContent.replaceAll("\"", "");
	}

	private static WebElement getContextElement(WebDriver webDriver) {
		System.out.println("inside getContextElement");
		WebElement webElementIFrame = webDriver.findElement(By.id(IFRAME_ID));
		System.out.println("got iFrame");
		webDriver.switchTo().frame(webElementIFrame);
		System.out.println("switched to iFrame");
		WebElement webElementForm = webDriver.findElement(By.id(FORM_ID));
		System.out.println("got Form");
		WebElement webElementDiv = webElementForm.findElement(By.className(SMS_PANEL_DIV_CLASS));
		System.out.println("got Div");
		return webElementDiv;
	}

	private static void fillSMSContentAndSend(WebElement webElement, String smsReceiver, String msgContent) {
		System.out.println("fillSMSContentAndSend -> (smsReceiver) " + smsReceiver + " (msgContent) " + msgContent + " (msgContent.length) " + msgContent.length());
		WebElement webElementDiv = webElement.findElement(By.className(TO_DIV_CLASS));
		WebElement webElementInputReceiver = webElementDiv.findElement(By.tagName(INPUT));
		webElementInputReceiver.sendKeys(smsReceiver);
		WebElement webElementInputContent = webElement.findElement(By.id(CONTENT_ID));
		webElementInputContent.sendKeys(msgContent);
		WebElement webElementSendButton = webElement.findElement(By.id(SEND_BUTTON_ID));
		System.out.println("filled number & content");
		webElementSendButton.click();
		System.out.println("clicked send sms");
	}
	
	private static void logoutOfSMSPortal(WebDriver webDriver) {
		System.out.println("logoutOfSMSPortal");
		WebElement webElementLogoutButton = webDriver.findElement(By.className(LOGOUT_BUTTON));
		webElementLogoutButton.click();
	}

	private static void checkAndDiscardUnwantedPopups(WebDriver webDriver) {
		try {
			cancelPopup(webDriver);
		} catch (Exception e) {
			ExceptionLogUtil.logException(e, "Error in Canceling Popup");
		}
		try {
			closePopup(webDriver);
		} catch (Exception e) {
			ExceptionLogUtil.logException(e, "Error in Closing Popup");
		}
	}

	private static void sendSMS(String smsReceiver, String msgContent) {
		WebDriver webDriver = new ChromeDriver();
		webDriver.get(SMS_URL);
		webDriver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		loginToSMSPortal(webDriver);
		System.out.println("logged in");
		checkAndDiscardUnwantedPopups(webDriver);
		WebElement webElement = getContextElement(webDriver);
		//checkAndDiscardUnwantedPopups(webDriver);
		fillSMSContentAndSend(webElement, smsReceiver, msgContent);
		checkAndDiscardUnwantedPopups(webDriver);
		System.out.println("going to switch context to parent");
		webDriver.switchTo().defaultContent();
		System.out.println("switched context to parent");
		checkAndDiscardUnwantedPopups(webDriver);
		logoutOfSMSPortal(webDriver);
		System.out.println("logged out");
		//checkAndDiscardUnwantedPopups(webDriver);
		webDriver.close();
	}

	private static void sendSMSBasedUponCondition(BulkDeal bulkDeal) {
		double price = Double.parseDouble(bulkDeal.getPrice().replaceAll(",", ""));
		if (price >= 100 && price <= 1000) {
			String msgContent = getReadableMsgContent(bulkDeal);
			for (String smsReceiver : SMS_RECEIVERS) {
				sendSMS(smsReceiver, msgContent);
			}
		}
	}

	private static void proceedToWriteAndNotify(int i, BulkDeal bulkDeal) {
		try {
			writeDataInFile(i, bulkDeal);
			fileCount++;
		} catch (Exception e) {
			ExceptionLogUtil.logException(e, "Error in Creating JSON file " + i + " of Bulk Deal");
		}
		sendSMSBasedUponCondition(bulkDeal);
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

	private static String getTimeTakenReadableString(long timeTakenMilliSeconds) {
		String readable = new String();
		List<Long> timeRecords = new ArrayList<Long>();
		if (timeTakenMilliSeconds > 0) {
			long milliSec = timeTakenMilliSeconds % 1000;
			timeTakenMilliSeconds = timeTakenMilliSeconds / 1000;
			timeRecords.add(milliSec);
		}
		if (timeTakenMilliSeconds > 0) {
			long sec = timeTakenMilliSeconds % 60;
			timeTakenMilliSeconds = timeTakenMilliSeconds / 60;
			timeRecords.add(sec);
		}
		if (timeTakenMilliSeconds > 0) {
			long min = timeTakenMilliSeconds % 60;
			timeTakenMilliSeconds = timeTakenMilliSeconds / 60;
			timeRecords.add(min);
		}
		if (timeTakenMilliSeconds > 0) {
			long hr = timeTakenMilliSeconds % 24;
			timeTakenMilliSeconds = timeTakenMilliSeconds / 24;
			timeRecords.add(hr);
		}
		if (timeTakenMilliSeconds > 0) {
			long day = timeTakenMilliSeconds;
			timeRecords.add(day);
		}
		for (int i = timeRecords.size()-1; i >= 0; i--) {
			readable += timeRecords.get(i) + " " + TIME_ENTITIES[i];
			if (i > 0) {
				readable += " ";
			}
		}
		return readable;
	}

	private static void logInfo(Date start, Date end) {
		System.out.println("(Pass) -> " + pass);
		System.out.println("(StartDate) -> " + start);
		System.out.println("(EndDate) -> " + end);
		System.out.println("(StartDateMilliSeconds) -> " + start.getTime());
		System.out.println("(EndDateMilliSeconds) -> " + end.getTime());
		long timeTakenMilliSeconds = end.getTime() - start.getTime();
		System.out.println("(TimeTakenMilliSeconds) -> " + timeTakenMilliSeconds);
		System.out.println("(TimeTakenHumanReadable) -> " + getTimeTakenReadableString(timeTakenMilliSeconds));
		System.out.println();
	}

	public static void main(String[] args) {
		while ( getAggregatedMinutesOfTodayTillNow() < (MARKET_CLOSING_TIME_IN_AGGREGATED_MINUTES + 100) ) {
			++pass;
			Date start = new Date();
			process();
			Date end = new Date();
			logInfo(start, end);
		}
	}

}

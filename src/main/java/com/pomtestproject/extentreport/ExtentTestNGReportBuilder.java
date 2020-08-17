package com.pomtestproject.extentreport;

import java.io.IOException;
import java.lang.reflect.Method;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentTestNGReportBuilder {
	
	private static ExtentReports extent;
	private static ThreadLocal<ExtentTest> parentTest = new ThreadLocal<ExtentTest>();
	private static ThreadLocal<ExtentTest> test = new ThreadLocal<ExtentTest>();
	
	@BeforeSuite
	public void beforeSuite() {
		ExtentManager.getInstance();
		extent = ExtentManager.createInstance( Config.reportdir);
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter( Config.reportdir);
		extent.attachReporter(htmlReporter);
	}

	@BeforeClass
	public synchronized void beforeClass() {
		ExtentTest parent = extent.createTest(getClass().getName());
		parentTest.set(parent);
	}
	
	@BeforeMethod
	public synchronized void beforeMethod(Method method) {
		ExtentTest child = ((ExtentTest) parentTest.get()).createNode(method.getName());
		test.set(child);
	}

	@AfterMethod
	public synchronized void afterMethod(ITestResult result) throws IOException {
		if (result.getStatus() == ITestResult.FAILURE)
			((ExtentTest) test.get()).fail(result.getThrowable(),MediaEntityBuilder.createScreenCaptureFromPath("screenshot.png").build());
		else if (result.getStatus() == ITestResult.SKIP)
			((ExtentTest) test.get()).skip(result.getThrowable());
		else
			((ExtentTest) test.get()).pass("Test passed");
		extent.flush();
	}
}

class ExtentManager {

	private static ExtentReports extent;

	public static ExtentReports getInstance() {
		if (extent == null)
			createInstance( Config.reportdir);
		return extent;
	}

	public static ExtentReports createInstance(String fileName) {
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(fileName);
		htmlReporter.config().setTestViewChartLocation(ChartLocation.BOTTOM);
		htmlReporter.config().setChartVisibilityOnOpen(true);
		htmlReporter.config().setTheme(Theme.STANDARD);
		htmlReporter.config().setDocumentTitle(fileName);
		htmlReporter.config().setEncoding("utf-8");
		htmlReporter.config().setReportName(fileName);
		extent = new ExtentReports();
		extent.attachReporter(htmlReporter);

		return extent;
	}
	
}
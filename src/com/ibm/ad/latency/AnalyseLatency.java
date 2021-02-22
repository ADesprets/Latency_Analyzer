package com.ibm.ad.latency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// handle size of input and generate multiple files
// handle file listener mode
// add cvs mode

public class AnalyseLatency {
	// private final String pattern =
	// "(\\w{3}\\s\\w{3}\\s\\d{2}\\s\\d{4}\\s)(\\d{2}:\\d{2}:\\d{2}\\s)(\\[.*\\]\\[.*\\]\\[.*\\]\\s)(mpgw|xmlfirewall|wsgw|web-application-firewall)(.*)(:\\stid.*)Latency:\\s+((?:\\d+\\s+){16})(.*)";
	private final String pattern = "(\\d{8}T\\d{6}.\\d{3}Z\\s)(\\[.*\\]\\[.*\\]\\[.*\\]\\s)(mpgw|wsgw|xmlfirewall|wsproxy)(.*)(:\\stid.*)Latency:\\s+((?:\\d+\\s+){16})(.*)";

	public AnalyseLatency() {

	}

	public void analyse(File latencyInput) {
		// String l2 = "Wed Jan 11 2017 15:30:48 [0x80e00073][latency][info]
		// mpgw(ACC_MpgPreProcessValidationAT): tid(18895136)[10.200.14.5]
		// gtid(18895136): Latency: 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
		// [https://10.200.15.172:443/wsfacteo/tmc/synchro/tournees/v7]";

		// Only takes log line containing latency information, assumes that
		// latency contains [latency][info]. Use regex instead of contains to
		// tune it if needed later
		Pattern p = Pattern.compile(pattern);

		// System.out.println("It takes about 20 seconds to process 100 000
		// records");

		XSSFWorkbook workbook = new XSSFWorkbook();

		// Formats and style
		XSSFFont boldFontx = null;
		boldFontx = workbook.createFont();
		boldFontx.setFontHeightInPoints((short) 10);
		boldFontx.setBold(true);
		XSSFCellStyle boldStylex = null;
		boldStylex = workbook.createCellStyle();
		boldStylex.setFont(boldFontx);

		// Vertical style
		XSSFCellStyle verticalstyle = (XSSFCellStyle) workbook.createCellStyle();
		verticalstyle.setFont(boldFontx);
		verticalstyle.setRotation((short) 180);
		verticalstyle.setVerticalAlignment(VerticalAlignment.TOP);

		// Create Workbook
		XSSFSheet sheet = workbook.createSheet("Latencies");

		// Date style
		XSSFCreationHelper createHelper = workbook.getCreationHelper();
		XSSFCellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("ddd MMM dd yyyy"));

		// Create title row
		createFirstRow(sheet, boldStylex, verticalstyle);

		long start = System.currentTimeMillis();
		int nb = 0;

		try {
			// Read input file
			FileInputStream fis = new FileInputStream(latencyInput);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while ((line = br.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.matches()) {
					nb++;
					if ((nb % 20000) == 0) {
						System.out.println(nb + " records treated");
					}
					XSSFRow dataRow = sheet.createRow(nb);
					parseLatencyLine(m, dataRow, nb);
				}
			}

			sheet.setAutoFilter(new CellRangeAddress(0, nb, 0, 21));

			addFormulas(sheet, boldStylex);

			String startDate = sheet.getRow(1).getCell(1).getStringCellValue().trim().replace(' ', '_');
			String endDate = sheet.getRow(nb).getCell(1).getStringCellValue().trim().replace(' ', '_');
			String startTime = sheet.getRow(1).getCell(2).getStringCellValue().trim().replaceAll(":", "");
			String endTime = sheet.getRow(nb).getCell(2).getStringCellValue().trim().replaceAll(":", "");
			String fileName = "";

			if (startDate.equals(endDate)) {
				fileName = "DPLatencies_" + startDate + "_" + startTime + "_" + endTime + ".xlsx";
			} else {
				fileName = "DPLatencies_" + startDate + "_" + startTime + "_" + endDate + "_" + endTime + ".xlsx";
			}

			// Get the path of the input file, so we create the output in the same
			// directory.
			String parent = latencyInput.getCanonicalFile().getParent();

			fileName = parent + File.separator + fileName;
			System.out.println("Creating the file " + fileName);
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			workbook.write(fos);
			br.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error when creating " + e.getMessage());
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long duration = System.currentTimeMillis() - start;
		System.out.println("Duration for " + nb + " matches: " + duration + " ms.");
	}

	/**
	 * group[1]: Wed Jan 11 2017 group[2]: 15:30:54 group[3]:
	 * [0x80e00073][latency][info] group[4]: mpgw group[5]:
	 * (ACC_MpgPostProcessValidationAT) group[6]: : tid(18919888) gtid(19509362):
	 * group[7]: 0 26 0 6 6 0 0 26 51 26 51 51 0 26 6 26 group[8]:
	 * [http://127.0.0.1:8080/tmc/synchro/articles/v7]
	 */

	/**
	 * group[1]: 20210211T131129.661Z group[2]: [0x80e00073][latency][info]
	 * group[3]: wsgw group[4]: (BranchesBMX) group[5]: : tid(18385)[192.168.246.1]
	 * gtid(ab5f79ff60252cfe000047d1): group[6]: 0 329 0 295 403 295 0 3174 3241
	 * 3174 3241 3241 3240 3174 295 329 group[7]:
	 * [http://192.168.246.150:9080/branches/Branches]
	 */
	private void parseLatencyLine(Matcher m, XSSFRow aRow, int nb) {
		// need to extract date and time from zulu definition

//		String dateL = m.group(1);
//		String timeL = m.group(2);
//		String serviceType = m.group(4);
//		String serviceName = m.group(5);
//		String latencies = m.group(7);
//		String url = m.group(8);

		String dateL = m.group(1).substring(0, 7);
		String timeL = m.group(1).substring(9);
		;
		String serviceType = m.group(3);
		String serviceName = m.group(4);
		String latencies = m.group(6);
		String url = m.group(7);

		String[] latencyTime = latencies.trim().split("\\s+");
		// The latency times are listed in the wrong order. The correct order is
		// described at
		// http://www-01.ibm.com/support/docview.wss?uid=swg21239328.
		// Very simple implementation, longer to write, but very fast to
		// execute.
		String temp = latencyTime[1];
		latencyTime[1] = latencyTime[2];
		latencyTime[2] = latencyTime[6];
		latencyTime[6] = latencyTime[15];
		latencyTime[15] = latencyTime[11];
		latencyTime[11] = latencyTime[13];
		latencyTime[13] = latencyTime[10];
		latencyTime[10] = latencyTime[9];
		latencyTime[9] = latencyTime[7];
		latencyTime[7] = temp;
		temp = latencyTime[5];
		latencyTime[5] = latencyTime[14];
		latencyTime[14] = latencyTime[8];
		latencyTime[8] = latencyTime[4];
		latencyTime[4] = latencyTime[3];
		latencyTime[3] = temp;

		// First column - ID
		XSSFCell cell = aRow.createCell(0);
		cell.setCellValue(nb);

		// Second column - date
		cell = aRow.createCell(1);
		cell.setCellValue(dateL);

		// Third column - time
		cell = aRow.createCell(2);
		cell.setCellValue(timeL);

		for (int i = 0; i < latencyTime.length; i++) {
			cell = aRow.createCell(i + 3);
			cell.setCellValue(Integer.parseInt(latencyTime[i]));
		}
		cell = aRow.createCell(19);
		cell.setCellValue(serviceType);
		cell = aRow.createCell(20);
		cell.setCellValue(serviceName.substring(1, serviceName.length() - 1));
		cell = aRow.createCell(21);
		cell.setCellValue(url);

		// return null;
	}

	private void createFirstRow(XSSFSheet sheet, XSSFCellStyle boldStyle, XSSFCellStyle verticalStyle) {
		XSSFRow titleRow = sheet.createRow(0);
		String[] titleValues = { "#", "Date", "Time", "request header read", "front transform begun",
				"front parsing complete", "front style-sheet ready", "front transform complete",
				"back connection attempted", "back connection completed", "request header sent",
				"entire request transmitted", "response headers received", "back side transform begun",
				"back side parsing complete", "back side style-sheet read", "back side transform complete",
				"response headers sent", "response transmitted", "Service type", "Service name", "URL" };
		for (int i = 0; i < titleValues.length; i++) {
			XSSFCell cell = titleRow.createCell(i);
			cell.setCellValue(new XSSFRichTextString(titleValues[i]));
			if (i > 2 && i < 19) {
				cell.setCellStyle(verticalStyle);
			} else {
				cell.setCellStyle(boldStyle);
			}
		}
	}

	/**
	 * Add some formulas
	 * 
	 * @param sheet
	 * @param boldStylex
	 */
	private void addFormulas(XSSFSheet sheet, XSSFCellStyle boldStylex) {
		String[][] calculatedValues = { { "Calculations", "" }, { "Average", "Average(M:M)" }, { "Max", "Max(M:M)" },
				{ "Min", "Min(M:M)" }, { "Count", "Count(M:M)" } };
		for (int i = 0; i < calculatedValues.length; i++) {
			XSSFCell cellT = sheet.getRow(i).createCell(23);
			XSSFCell cellV = sheet.getRow(i).createCell(24);
			cellT.setCellValue(calculatedValues[i][0]);
			cellT.setCellStyle(boldStylex);
			if (calculatedValues[i][1] != "") {
				cellV.setCellFormula(calculatedValues[i][1]);
			}
		}
	}

}

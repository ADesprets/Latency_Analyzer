package com.ibm.ad.latency;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	public static void main(String[] args) {
		String l1 = "Wed Jan 11 2017 15:30:54 [0x80e00073][latency][info] mpgw(ACC_MpgPostProcessValidationAT): tid(18919888) gtid(19509362): Latency:   0  26   0   6   6   0   0  26  51  26  51  51   0  26   6  26 [http://127.0.0.1:8080/tmc/synchro/articles/v7]";
		String l2 = "20210211T131129.661Z [0x80e00073][latency][info] wsgw(BranchesBMX): tid(18385)[192.168.246.1] gtid(ab5f79ff60252cfe000047d1): Latency:   0 329   0 295 403 295   0 3174 3241 3174 3241 3241 3240 3174 295 329 [http://192.168.246.150:9080/branches/Branches]";
// ([.*][.*][.*]\\s) (.*)(\\(.*\\))
		String pattern1 = "(\\w{3}\\s\\w{3}\\s\\d{2}\\s\\d{4}\\s)(\\d{2}:\\d{2}:\\d{2}\\s)(\\[.*\\]\\[.*\\]\\[.*\\]\\s)(mpgw|wsgw|xmlfirewall|wsproxy)(.*)(:\\stid.*)Latency:\\s+((?:\\d+\\s+){16})(.*)";
		String pattern2 = "(\\d{8}T\\d{6}.\\d{3}Z\\s)(\\[.*\\]\\[.*\\]\\[.*\\]\\s)(mpgw|wsgw|xmlfirewall|wsproxy)(.*)(:\\stid.*)Latency:\\s+((?:\\d+\\s+){16})(.*)";
		Pattern p = Pattern.compile(pattern1);
		Matcher m = p.matcher(l1);
		if (m.matches()) {
			for (int i = 1; i < m.groupCount()+1; i++) {
				System.out.println("group[" + i + "]: "+ m.group(i));
			}
		}
	}

}

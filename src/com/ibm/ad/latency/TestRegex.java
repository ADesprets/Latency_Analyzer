package com.ibm.ad.latency;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	public static void main(String[] args) {
		String l2 = "Wed Jan 11 2017 15:30:54 [0x80e00073][latency][info] mpgw(ACC_MpgPostProcessValidationAT): tid(18919888) gtid(19509362): Latency:   0  26   0   6   6   0   0  26  51  26  51  51   0  26   6  26 [http://127.0.0.1:8080/tmc/synchro/articles/v7]";
// ([.*][.*][.*]\\s) (.*)(\\(.*\\))
		String pattern = "(\\w{3}\\s\\w{3}\\s\\d{2}\\s\\d{4}\\s)(\\d{2}:\\d{2}:\\d{2}\\s)(\\[.*\\]\\[.*\\]\\[.*\\]\\s)(mpgw|xmlfirewall|wsproxy)(.*)(:\\stid.*)Latency:\\s+((?:\\d+\\s+){16})(.*)";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(l2);
		if (m.matches()) {
			for (int i = 1; i < m.groupCount()+1; i++) {
				System.out.println("group[" + i + "]: "+ m.group(i));
			}
		}
	}

}

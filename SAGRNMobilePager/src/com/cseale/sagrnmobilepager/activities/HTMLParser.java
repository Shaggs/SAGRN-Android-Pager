package com.cseale.sagrnmobilepager.activities;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.cseale.sagrnmobilepager.objects.Page;
import com.cseale.sagrnmobilepager.objects.PagesList;

public class HTMLParser {


	public static PagesList getPagesList(String url) throws SocketTimeoutException
	{
		PagesList pages = new PagesList();

		try {
			Document doc = Jsoup.connect(url).get();
			Elements dateEls = doc.select("td.date");
			Elements messageEls = doc.select("td.message");
			for (int i=0; i<dateEls.size(); i++){
				String name = dateEls.get(i).text();
				String desc = messageEls.get(i).text();
				Page p = new Page(name, desc);
				pages.addPage(p);
			}
		} catch (SocketTimeoutException e) {
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pages;

	}

}

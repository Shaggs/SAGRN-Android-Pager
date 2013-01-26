package com.cseale.sagrnmobilepager.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class PagesList implements Serializable{

	private static final long serialVersionUID = 5692832568545372467L;
	private List<Page> pages = new ArrayList<Page>();
	//private List<Map<String, String>> pages = new ArrayList<Map<String, String>>();
	private final String dateStr= "date";
	private final String detailsStr = "details";
	public PagesList(){
	}

	public PagesList(PagesList another){
		this.pages = another.getPages();
	}
	public void addPage(Page p){
		pages.add(p);
	}

	public int size(){
		return pages.size();
	}
	public List<Map<String, String>> getListViewMap() {
		List<Map<String, String>> l = new ArrayList<Map<String, String>>();
		for(Page p : pages){
			Map<String, String> datum = new HashMap<String, String>(2);
			datum.put(dateStr, p.getName());
			datum.put(detailsStr, p.getDescription()); 
			l.add(datum);
		}
		return l;
	}

	public List<Map<String, String>> getListViewMap(String s) {
		List<Map<String, String>> l = new ArrayList<Map<String, String>>();
		for(Page p : pages){
			if(p.getDescription().toLowerCase().contains(s.toLowerCase())){
				Map<String, String> datum = new HashMap<String, String>(2);
				datum.put(dateStr, p.getName());
				datum.put(detailsStr, p.getDescription()); 
				l.add(datum);
			}
		}
		return l;
	}
	public List<Page> getPages(){
		return pages;
	}

	public boolean pagesAreSame(PagesList P) {
		// TODO Auto-generated method stub
		return pages.equals(P.getPages());

	}

	public void filterPages(String filter) {
		List<Page> filteredPages = new ArrayList<Page>();
		StringTokenizer st = new StringTokenizer(filter, ";");
		if(filter.length() > 0){
			while (st.hasMoreTokens()){
				String s = st.nextToken().trim();
				for(Page p : pages){
					if(p.getDescription().toLowerCase().contains(s.toLowerCase())){
						filteredPages.add(p);
					}
				}
			}
			pages = filteredPages;
		}
		//else return all pages
	}

	public Page get(int i) {
		return 	pages.get(i);
	}
}


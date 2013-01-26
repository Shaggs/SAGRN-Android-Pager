package com.cseale.sagrnmobilepager.objects;

public class Page {
	private String title;
	private String description;

	public Page(String title, String description){
		this.title = title;
		this.description = description;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return title;
	}
	public String getDescription(){
		return description;
	}
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Page)
			return (title.equals(((Page) obj).getName()) &&
					description.equals(((Page) obj).getDescription())); 
		else
			return false;
	}

}

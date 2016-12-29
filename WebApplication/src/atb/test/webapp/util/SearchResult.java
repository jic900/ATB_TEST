package atb.test.webapp.util;

import java.io.Serializable;

public class SearchResult implements Serializable  {
	
	/**
	 * The entity class for storing google search results
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private String link;
	private String snippet;

	/**
	 * 
	 * @param title
	 * @param link
	 * @param snippet
	 */
	public SearchResult( String title, String link, String snippet){
		this.title = title;
		this.link = link;
		this.snippet = snippet;
		
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

}

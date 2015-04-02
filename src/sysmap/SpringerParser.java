package sysmap;

import java.util.ArrayList;
import java.util.List;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * TODO: document this type.
 *
 * @author Pedro Negri
 * @version 1.0
 */
public class SpringerParser {
	private static final String START_URL = "http://link.springer.com/search/page/1?facet-discipline=%22Computer+Science%22&query=%28%28%22requirements+model%22+OR+%22requirements+reflection%22+OR+%22requirements+engineering%22+OR+%22requirements+analysis%22+OR+%22gore%22+OR+%22goal+model%22+OR+%22goal+models%22+OR+%22goal+analysis%22+OR+%22goal+reasoning%22+OR+%22softgoals%22+OR+%22specification+of+goals%22%29+AND+%28%22runtime%22+OR+%22run+time%22+OR+%22monitoring%22%29%29&facet-content-type=%22Article%22";


	/** Main method. */
	public static void main(String[] args) throws Exception {
		// Extracts the base address of the URL.
		String baseUrl = START_URL.substring(0, START_URL.indexOf('/', 7));
		// Processes all pages, following "next" links.
		String url = START_URL;
		List<String> papers = new ArrayList<String>();
		int i = 0;
		
		
		while (url != null && i!=20) {
			// Opens the page and extracts the HTML DOM structure into Jsoup.
			Document doc = Jsoup.connect(url).timeout(10000*10000).get();
			url = null;
			// Looks for the first table in the document, where the names of the
			// students are supposed to be.
			Element resultsList = doc.select("#results-list").first();
			Elements lis = resultsList.select("li");
			String paperUrl;
			Document paperDoc;
			Elements keyLis;
			
			String paper;
			for (Element li : lis){
			//	Element li = lis.first();
				paperUrl = "http://link.springer.com" + li.select("h2").first().select("a").first().attr("href");
				//System.out.println(paperUrl);
				
				paperDoc = Jsoup.connect(paperUrl).timeout(10000*10000).get();
				
				//System.out.println(paperDoc.select("#title").html());
				//System.out.println(paperDoc.select(".abstract-content.formatted").select(".a-plus-plus").first().html());
				keyLis = paperDoc.select("ul.abstract-keywords").select("li");
				//for(Element keyLi : keyLis){
				//	System.out.print(keyLi.html() + ", ");
				//}
				//System.out.println("\n");
				
				paper = paperDoc.select("#title").html() + "\n";
				if(!paperDoc.select(".abstract-content.formatted").isEmpty()){
					if(!paperDoc.select(".abstract-content.formatted").select(".a-plus-plus").isEmpty()){
						paper = paper + paperDoc.select(".abstract-content.formatted").select(".a-plus-plus").first().html() + "\n";	
					}					
				}
				for(Element keyLi : keyLis){
					paper = paper + keyLi.html() + ", ";
				}
				paper = paper + "\n\n";
					
				papers.add(paper);


				
			}

			i = i+1;
			System.out.println(i);

			Elements nextLinks = doc.select("a.next");
			if (!nextLinks.isEmpty())
				url = nextLinks.first().attr("href");
			if (url != null && url.startsWith("/"))
				url = baseUrl + url;
			
			
		}
		String pl;
		for(String p : papers){
			
			System.out.println(p);
			
			pl = p.toLowerCase();
			
			
			
			if((pl.contains("requirements model") |
					pl.contains("requirements reflection") |
					pl.contains("requirements engineering") |
					pl.contains("requirements analysis") |
					pl.contains("gore") |
					pl.contains("goal model") |
					pl.contains("goal models") |
					pl.contains("goal analysis") |
					pl.contains("goal reasoning") |
					pl.contains("softgoals") |
					pl.contains("specification of goals")) 
					&&
					(pl.contains("runtime") |
					pl.contains("run time") |
					pl.contains("monitoring"))){
				//System.out.println(p);	
			}
			
			
		}
		
		
		
	}
}
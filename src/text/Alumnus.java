package text;

import java.io.PrintWriter;
import java.util.Map;

public class Alumnus implements Comparable<Alumnus> {
	private static final String ALUMNI_TABLE_LINE_TEMPLATE = 
			"<tr%s>%n" + 
				"\t<td><a href=\"%s\">%s</a></td>%n" + 
				"\t<td>%s</td>%n" +
				"\t<td><a href=\"%s\">%s</a></td>%n" +
				"\t<td>%s</td>%n" +
				"\t<td>%s</td>%n" +
			"</tr>%n";

	private String name;
	private String defenseDate;
	private String level;
	private String url;
	private String supervisor;
	private String workTitle;
	
	Alumnus(String name, String defenseDate, String level, String url) {
		this.name = name;
		this.defenseDate = defenseDate;
		this.level = level;
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}
	
	public void setWorkTitle(String workTitle) {
		this.workTitle = workTitle;
	}
	
	public void printTableLine(PrintWriter out, boolean odd, Map<String, String> homepageMap) {
		String supervisorHomepage = homepageMap.get(supervisor);
		out.printf(ALUMNI_TABLE_LINE_TEMPLATE, (odd ? " class=\"odd\"" : ""), url, name, level, supervisorHomepage, supervisor, defenseDate, workTitle);
	}

	@Override
	public int compareTo(Alumnus o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public String toString() {
		return name + " (" + level + ")";
	}
}

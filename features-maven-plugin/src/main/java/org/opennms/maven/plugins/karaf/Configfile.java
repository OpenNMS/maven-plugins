package org.opennms.maven.plugins.karaf;

public class Configfile {
	private String location;
	private String finalname;
	
	public String getLocation() {
		return this.location;
	}
	
	public void setLocation(final String location) {
		this.location = location;
	}
	
	public String getFinalname() {
		return this.finalname;
	}
	
	public void setFinalname(final String finalname) {
		this.finalname = finalname;
	}
	
	@Override
	public String toString() {
		return "<configfile finalname=\"" + this.finalname + "\">" + this.location + "</configfile>";
	}
}

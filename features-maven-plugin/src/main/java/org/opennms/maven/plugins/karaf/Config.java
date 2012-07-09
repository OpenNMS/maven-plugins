package org.opennms.maven.plugins.karaf;

public class Config {
	private String name;
	private String contents;
	
	public String getName() {
		return this.name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public String getContents() {
		return this.contents;
	}
	
	public void setContents(final String contents) {
		this.contents = contents;
	}

	@Override
	public String toString() {
		return "<config name=\"" + this.name + "\">" + this.contents + "</config>";
	}
}

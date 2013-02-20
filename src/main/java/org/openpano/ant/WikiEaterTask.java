package org.openpano.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openpano.wikieater.WikiEater;
import org.openpano.wikieater.data.CliData;

/**
 * @author mstandio
 */
public class WikiEaterTask extends Task {

	private String cachedir;
	private String outputdir;
	private String menufile;
	private Boolean refreshcache;

	@Override
	public void execute() throws BuildException {
		try {
			CliData cliData = new CliData();
			cliData.cacheDir = cachedir;
			cliData.outputDir = outputdir;
			cliData.menuFile = menufile;
			cliData.refreshCache = refreshcache;

			WikiEater wikiEater = new WikiEater(cliData);
			wikiEater.processMenuFile();

		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	public void setCachedir(String cachedir) {
		this.cachedir = cachedir;
	}

	public void setOutputdir(String outputdir) {
		this.outputdir = outputdir;
	}

	public void setMenufile(String menufile) {
		this.menufile = menufile;
	}

	public Boolean getRefreshcache() {
		return refreshcache;
	}
}

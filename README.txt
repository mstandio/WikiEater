  _      __  _   __     _   ____       __            
 | | /| / / (_) / /__  (_) / __/___ _ / /_ ___   ____
 | |/ |/ / / / /  '_/ / / / _/ / _ `// __// -_) / __/
 |__/|__/ /_/ /_/\_\ /_/ /___/ \_,_/ \__/ \__/ /_/   

WikiEater is a tool for exporting and processing selected MediaWiki articles into set of clean *.html files
that can be easily browsed offline. It can be used in project build cycle for generating per-release docs.

usage (type -h for help): 

	java -jar MediaWiki.jar -menufile ./menu.txt -output ./output -cache ./cache
	
ant task:

	<project name="MediaWikiDemo" default="makeDocs">
		<taskdef name="wikieater" classpath="WikiEater.jar" classname="org.openapno.ant.WikiEater"/>
		<task name="makeDocs">
			<wikieater>
				<menufile value="./menu.txt"/>
				<output value="./output"/>
				<cache value="./cache"/>
				<refreshcache value="false"/>				
			</wikieater>			
		</task>		
 	 </project>

features:

	* matching and replacing urls between articles
	* saving images and replacing their urls
	* compiling used styling into single *.css file
	* cleaning and formatting *.html output
	* generating simple navigation menu
	* removing selected DOM elements and their children
	* caching of all downloaded files 
	
  _      __  _   __     _   ____       __            
 | | /| / / (_) / /__  (_) / __/___ _ / /_ ___   ____
 | |/ |/ / / / /  '_/ / / / _/ / _ `// __// -_) / __/
 |__/|__/ /_/ /_/\_\ /_/ /___/ \_,_/ \__/ \__/ /_/   

WikiEater is a tool for exporting and processing selected MediaWiki articles 
into set of clean *.html files that can be easily browsed offline. It can be
used in project build cycle for generating per-release documentation.

usage: 

  java -jar WikiEater-1.0.jar -help
  java -jar WikiEater-1.0.jar -menufile example/menu.txt

ant task:

  <project name="WikiEaterDemo" default="makeDocs">
      <taskdef name="wikieater" 
          classpath="WikiEater-1.0.jar"
          classname="org.openpano.ant.WikiEaterTask"/>
      <task name="makeDocs">
          <wikieater
              menufile="./menu.txt"
              outputdir="./output"
              cachedir="./cache"/>
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
  
build:

  mvn clean install
  

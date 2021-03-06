forge-boottheme-plugin
======================

Wanna use [Bootstrap](http://twitter.github.io/bootstrap/) but wanna have an &raquo;own look&laquo;? This plugin helps you building your own Bootstrap Theme.
It's inspired by [Bootswatch](https://github.com/thomaspark/bootswatch/) 
but based on [JBoss Forge](http://forge.jboss.org/) and [Apache Maven](http://maven.apache.org/). 

Install Boottheme Plugin
------------------------

Install and start jboss forge. In the forge shell type:

	forge install-plugin boottheme

or

	forge git-plugin git://github.com/lefloh/forge-boottheme-plugin.git

Bootstrap 2.3 vs 3.0
--------------------

The plugin is using Bootstrap 3.0.x now. There are a lot of changes so [learn how to upgrade from 2.3.x](http://getbootstrap.com/getting-started/#migration).
If you wanna stay with bootstrap 2.3.x checkout branch ```bootstrap-2.3.2```. This branch is pinned to bootstrap 2.3.2 and won't update bootstrap any more.

    forge git-plugin git://github.com/lefloh/forge-boottheme-plugin.git --ref bootstrap-2.3.2 


Setup your theme
----------------

Create a new project and setup the theme:
	
	new-project --named mytheme --type war
	boottheme setup
	
After that your project should look like this:

	└── src
	    └── main
	        └── webapp
	            ├── index.html
	            └── resources
	                └── theme
	                    ├── fonts
	                    │   ├── glyphicons-halflings-regular.eot
	                    │   ├── glyphicons-halflings-regular.svg
	                    │   ├── glyphicons-halflings-regular.ttf
	                    │   └── glyphicons-halflings-regular.woff
	                    ├── js
	                    │   ├── bootstrap.js
	                    │   ├── bootstrap.min.js
	                    │   ├── html5shiv.js
	                    │   ├── jquery.min.js
	                    │   └── less.min.js
	                    └── less
	                        ├── bootstrap
	                        │   ├── alert.less
	     					│	...
	     					├── bootstrap-customization.less
    	                    ├── main.less
     	                    └── variables.less
     	 
Customize your theme
--------------------

Open the index.html and customize the variables.less and bootstrap-customization.less. Add new styles to the main.less.
Don't touch the less-files in src/main/webapp/resources/theme/less/bootstrap. They will be overwritten on the next update.
While you're developing the less files will be compiled by [less.js](http://www.lesscss.de/). 
Once you've finished build the war by maven. The less-files will be compiled by the [lesscss-maven-plugin](https://github.com/marceloverdijk/lesscss-maven-plugin).
After all include your theme in your application as a [war-overlay](http://maven.apache.org/plugins/maven-war-plugin/overlays.html).

Update your theme
-----------------

A new Version of Bootstrap is out?

	boottheme update -- resource [bootstrap|jquery|html5shiv]
	
Show the used versions:

	boottheme versions
	
Notes
-----

* less.js is not working in Chrome using the file:// protocol. So use mvn tomcat:run or configure -allow-file-access-from-files.
* If you update the plugin just will download the newest version of the resource regardless of which version is used. No VersionManagement by now!
* The generated war will not include the less-files and the index.html. They are only needed for development.
 
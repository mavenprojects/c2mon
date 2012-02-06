#!/usr/bin/perl -wT
use lib '/user/alaser/perllib/Config-Properties-1.71/blib/lib';
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
##
my $jardir = "../lib";
my $appdir = "dmn2-viewer-pro";
my $codebase = "http://bewww/~diamonop";
my $c2monClientPropertiesFile = "../conf/client.properties";
my $c2monClientPropertiesURL = "${codebase}/${appdir}/conf/client.properties";

##
# Reading version number from ../version.txt
#
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

##
# Reading the C2MON client properties file #
#
open PROPS, "< $c2monClientPropertiesFile"
  or die "Unable to open configuration file $c2monClientPropertiesFile";


my $c2monProperties = new Config::Properties();
$c2monProperties->load(*PROPS);

my $jdbcDriver           = $c2monProperties->getProperty("c2mon.jdbc.driver");

my $jdbcRoUrl            = $c2monProperties->getProperty("c2mon.jdbc.ro.url");
my $jdbcRoUser           = $c2monProperties->getProperty("c2mon.jdbc.ro.user");
my $jdbcRoPassword       = $c2monProperties->getProperty("c2mon.jdbc.ro.password");
close PROPS;

##
# Procedure to generate for each library defined in the ../lib directory
# an entry in the jnlp file.
##
sub jarlist {
	my $dir = shift;
	opendir DIR, $dir or return;
	my @contents = 
    		map "$dir/$_", 
    		sort grep !/^\.\.?$/,
    		readdir DIR;
  	closedir DIR;
  	foreach (@contents) {
              my $htmldir = $appdir."/".substr($_, 3, length($_));
    		if (!-l && -d) {
			&jarlist($_);
		}
		else {
 			if (/tim-jviews-viewer.jar$/) {
    				print "		<jar href=\"", $htmldir, "\" main=\"true\" download=\"eager\"/>\n";
			}
 			elsif (/jar$/) {
    				print "		<jar href=\"", $htmldir, "\" main=\"false\" download=\"eager\"/>\n";
			}
		}
    		next
  	}
}



##########################################
#         Generating JNLP file           #
##########################################

print "Content-type: application/x-java-jnlp-file" , "\n\n";
print "<?xml version = '1.0' encoding = 'utf-8'?>
	<jnlp spec=\"1.0+\"
	codebase=\"$codebase\"
	>
	<information>
		<title>DMN2 Viewer [PRO]</title>
	        <vendor>BE/CO-IN DIAMON2 Team</vendor>
	        <homepage href=\"tim-viewer/index.html\"/>
	        <description>The synoptic viewer</description>
	        <icon kind=\"splash\" href=\"http://timweb.cern.ch/img/tim-animated-320x200.gif\"/>
	        <offline-allowed />
	</information>
	<security> 
		<all-permissions/> 
	</security> 
	<resources>
		<j2se version=\"1.6+\"  initial-heap-size=\"512M\" max-heap-size=\"512M\"/>" , "\n";

jarlist ("$jardir");

# Defines the version number that is shown in the DMN2 Viewer about dialog
print "		<property name=\"tim.version\" value=\"$viewerVersion\"/>\n";
# JMS configuration parameters needed by C2MON client API
print "		<property name=\"c2mon.client.conf.url\" value=\"$c2monClientPropertiesURL\"/>\n";

# C2MON read-only credentials to STL database, needed for the history player and charts
print "		<property name=\"c2mon.jdbc.driver\" value=\"$jdbcDriver\"/>\n";
print "		<property name=\"c2mon.jdbc.ro.url\" value=\"$jdbcRoUrl\"/>\n";
print "		<property name=\"c2mon.jdbc.ro.user\" value=\"$jdbcRoUser\"/>\n";
print "		<property name=\"c2mon.jdbc.ro.password\" value=\"$jdbcRoPassword\"/>\n";

if (param('configurl')) {
	print "		<property name=\"tim.conf.url\" value=\"", param('configurl'), "\"/>", "\n";
}

print "	</resources>
	<resources os=\"Windows\" > 
		<property name=\"tim.log.file\" value=\"c:\\temp\\dmn2-viewer.log\"/>
	</resources> 
	<application-desc main-class=\"ch.cern.tim.client.jviews.Main\">
	</application-desc>
</jnlp>" , "\n";

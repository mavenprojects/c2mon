#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

my $configsubdir = "/test/javaws/tim2-video-viewer/conf";
my $configdir = "/user/timtest/dist/public/test/html/javaws/tim2-video-viewer/conf";
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

# Determine the client operating system
my $os_= "linux";
if($ENV{'HTTP_USER_AGENT'} =~ /Windows/) {
  $os_="windows";
}

print header;
print start_html(-title=>'TIM Video Viewer', -style=>"/css/tim.css");
if ($os_ eq "linux") {
  print h1('TIM Video Viewer Configurations (Linux) for VLC version 1.1.4');
}
else {
  print h1('TIM Video Viewer Configurations (Windows)');
}

# Browsing the configuration files
opendir DIR, $configdir . "/" . $os_;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;

if ($os_ eq "linux") {
  print p("To launch the TIM Video Viewer for Linux, choose one of the configurations below. Please note, that you have to install first VLC v1.1.4 on your client machine and be a trusted client host.",font({-color=>"#FF0000"},"(For security reasons, the TIM Video Viewer only works on the Technical Network)"),":");
}
else {
  print p("To launch the TIM Video Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: For security reasons, the TIM Video Viewer only works on the Technical Network)"),":");
}
print "<UL>";
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  print "<LI>";
  print a({-href=>"jnlpgenerator.cgi?configurl=$configurl/$os_/$_"}, $fn);
  print "</LI>";
  
  next;
}
print "</UL>";
print p("If you need an additional configuration, please contact ", 
       a({-href=>"mailto:tim.support\@cern.ch"}, "tim.support\@cern.ch"),
       ".");
	   
print end_html;

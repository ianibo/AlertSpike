# MonitorLocalUX
Angular Based Web interface for Monitor

## Prerequisites
You need to install the following tools onto your system in order to build the web frontend app.

Install ruby
* https://www.ruby-lang.org/en/ -- Consider RVM https://rvm.io/

Install compass, and sass-globbing as a ruby gem
```
gem install compass sass-globbing
```

Install node.js and npm
* see: https://www.npmjs.com/ -- Consider nvm -- I currently have "nvm use v0.12.5" at the bottom of my .bashrc

Install dependancies including bower and grunt using npm:
```
npm install -g bower grunt-cli karma phantomjs jasmine-core sass-globber
```
__note:__ If you see errors regarding permissions take a look at this script (https://github.com/glenpike/npm-g_nosudo/blob/master/npm-g-nosudo.sh) to allow global npm packages without sudo. Alternatively, use nvm and -g does something different and more sensible.


## Building
Change into root directory of this project.
```
bower install & npm install
```

## Running
Running the application also watches files for changes and updates the application live.
```
grunt serve
```
__note:__ If the app does not launch in your browser automatically you should navigate to: 
http://localhost:9090/

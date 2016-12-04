#!/usr/bin/env groovy
//
// The MIT License (MIT)
//
// Copyright (c) 2015, Edgars Simanis.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
// This is free software with ABSOLUTELY NO WARRANTY
//
//
// INSTALL
// 
// Get sqlite-jdbc -*.jar from https://bitbucket.org/xerial/sqlite-jdbc/downloads
// This location works on Ubuntu
// cp sqlite-jdbc-3.15.1.jar /usr/share/groovy2/lib


import org.apache.commons.cli.Option
import java.sql.*
import groovy.sql.Sql
import groovy.json.*

class mtrcbackup {
	/** Main script class that calls other classes to perform heavy liftig
	*/
	static void main(args) {
		/** Menu and default vars 
		*/
		
		// Get full path to script
		//def pwd = new File("mtrcbackup.groovy").getAbsolutePath()
	
				// init syslog object to log and print on screen
			//	Syslog = Syslog()
			
		// init cli object
		def cli = new CliBuilder()
			
			// define options
			cli.with {
				h longOpt: 'help', 'Help';
				a longOpt: 'add', 'Add';
				_ longOpt: 'delete', 'Delete'; 
				c longOpt: 'change', 'Change'; 
				e longOpt: 'edit', 'Edit';
				l longOpt: 'list', 'List';
				_ longOpt: 'setup', 'Setup';
				_ longOpt: 'auto', 'Auto';
				b longOpt: 'backup', 'Backup'; 
				_ longOpt: 'check', 'Check';
				_ longOpt: 'license', 'License'; 
			}
	
		def options = cli.parse(args)
		
		// if chain of option parsing
		if (options.h) {
			helpFile()
		}
		
		else if (options.a) {
			new Actions().addRouter(args)
		}
		
		
		else if (options.'delete') {
			new Actions().deleteRouter(args)
		}
		
		else if (options.c) {
			new Actions().changeHostname(args)
		}
		
		else if (options.e) {
			new Actions().editRouter(args)
		}
		
		else if (options.l) {
			new Actions().listRouters()
		}
		
		else if (options.'setup') {
			new Actions().Setup()
		}
		
	
		else if (options.'auto') {
			new Actions().autoBackup(args)
			
		}
		
		else if (options.b) {
			def condition = new mtrcbackup().sqlConn().rows("SELECT * FROM inventory where router=?", args[1])[0]
			def router = (condition['usekey'] == 0) ? new SSHPass(condition) : new SSHConn(condition)
			router.runBackup()
		}
		
		else if (options.'check') {
			def condition = new mtrcbackup().sqlConn().rows("SELECT * FROM inventory where router=?", args[1])[0]
			def router = (condition['usekey'] == 0) ? new SSHPass(condition) : new SSHConn(condition)
			println router.checkRouter()
		}
		
		else if (options.'license') {
			def condition = new mtrcbackup().sqlConn().rows("SELECT * FROM inventory where router=?", args[1])[0]
			def router = (condition['usekey'] == 0) ? new SSHPass(condition) : new SSHConn(condition)
			router.License()
		}
		
		else {
			// TEST GO HERE

		}
		
	}
	
	static helpFile() {
		/** help file */
		
		println """mtrcbackup 1.0
This is a small Python3 utility for backing up Mikrotik router configuration
This is free software with ABSOLUTELY NO WARRANTY

Usage: mtrcbackup [-OPTION] [Arguments]

Option list with arguments:

-h, --help		Print this help message

-a, --add		Add router to data base. Arguments:

				router=STRING	required
					Router hostname. Must be unique. IP or resolvable hostname

				user=STRING		[admin]
					User name default Mikrotik - admin

				passwd=STRING	[]
					Set password default Mikrotik - blank

				usekey=(0|1)	[0]
					Define ssh access method
					0 = password access using sshpass utility
					1 = key based authentication

				interval=(hourly|daily|weekly|monthly)	[hourly]
					Specify automatic backup frequency

				sshport=INTEGER	[22]
					Specify custom ssh port

				online=(yes|'')	[]
					Specify if router must be reachable at this stage
					If test fails it will not be saved in data base 

--delete		Delete router from database. Argument: hostname

-c, --change		Change router name. Arguments: current_hostname new_hostname

-e, --edit		Edit router settings in data base. For argument reference check
				-a, --add option above. Allows change of following settings:
				'user', 'passwd', 'usekey', 'interval', 'sshport'

-l, --list		Print JSON format list of routers and their settings in database

--setup			Set up mtrcbackup. Run once at initial program setup

--auto			Run auto backup. Argument: interval (hourly, daily, weekly, monthly)
				Intended for cron use 

-b, --backup		Run backup. Arguments: hostname

--check			Check if host reachable. Arguments: hostname
				Returns code. 0 = reachable

--license		Back up router license file. Arguments: hostname

Example usage:

mtrcbackup --add router=10.0.0.1 user=ed passwd=password123
	Add router with custom user name and password. Other arguments use defaults.

mtrcbackup --delete 10.0.0.1

mtrcbackup --change 10.0.0.1 10.0.0.100
	Change router hostname to 10.0.0.100

mtrcbackup --edit router=10.0.0.1 sshport=10022
	Change ssh port to 10022 for router 10.0.0.1

mtrcbackup --auto daily
	Run backup for all routers that have interval set to daily

mtrcbackup --check 10.0.0.1

mtrcbackup --license 10.0.0.1

Backups:
	Program is using Git for storage so use any operating system Git tools to view config changes."""
	
	}

	static sqlConn () {
		//* init database object to access data. Replaced Database class in python with this function */
		
		return Sql.newInstance( 'jdbc:sqlite:database.sqlite', 'org.sqlite.JDBC' )
	}
	
}

/*

class Syslog {}
	"""Logging methods""" 


		TO DO
		
///////////////////////////////////////////////////////////////////////////////////////////////////////////
*/
class Router {
	/* Router object and methods
	When connecting to router use router subclasses methods
	for either ssh-key or sshpass */
	
	String router, user, passwd, interval, arch, board
	Integer sshport, usekey
	

	String ping() {
		/* Ping method for cases when router does not respond to cli calls 
		but you want to confirm that it is reachable by ping*/
		
		def ping = "ping -c 4 -W 3 ${router}".execute()
		ping.waitFor()
		if (ping.exitValue() == 0) {
			return(router+"::Ping working")}
		else {
			return(router+"::Ping failed")}
	}
	

	void License() {
		// Back up router license"""
	
	
		// get license information on router
		this.callSSH("/system license output")
		
		// extract license number that looks like this: MRJQ-ZW7F
		def text = this.checkOutput("/system license print")
		def matcher = ( text =~ /(?<=software-id:\s)(.{4}-.{4})/ )
		if (!matcher.find()) { return }
		def lic_info = matcher[0][0]

		// scp file over from router
		def file_name = lic_info+".key"
		this.callSCP(file_name)

		// Add to version control
		"git -C inventory add ${router}/${file_name}".execute().waitFor()
		"git -C inventory commit -m  license_${router}".execute().waitFor()


	}
	
		
	Integer checkRouter() {
		// Execute dummy command on router to check connectivity. Return code
		
		try {
			return this.callSSH("nothing") }
		catch(Exception ex) {
			return 99 }
	}
			
	void boardInfo() {
		// Get arch and board info from router. Update data base.
		
		
		// Get information from router into var 'text'
		def text = this.checkOutput("system resource print")
		
		// Try to find match for info regex
		def matcher_arch = ( text =~ /(?<=architecture-name:\s)(.*)/ )
		def matcher_board = ( text =~ /(?<=board-name:\s)(.*)/ )
		
		// If none match not found return
		if (!matcher_arch.find() || !matcher_board.find() ) { return }
		
		// else define result strings
		def arch_result = matcher_arch[0][0]
		def board_result = matcher_board[0][0]
		
		// Update data base
		new mtrcbackup().sqlConn().execute("UPDATE inventory SET arch=?, board=? WHERE router=?", [arch_result, board_result, router])

	}
	
	

	void runBackup() {
		// Method that runs actual backup

		// Set temporary file		
		def tmpfile = new File("inventory/${router}/.export.tmp")
		
		
		// Execute export on router. If result is empty break out with message
		// Else result is still written to temp file and usable later
		def backup = this.checkOutput("export")
		if (!backup) {
			println(router+"::Backup run. Empty Result.")
		
			// Diagnose why
			if (!this.checkRouter() == 0) {
				println(router+"::Failure to communicate")
				println(ping())
			}
			return
		}
		
		//Else writ it to 'tmpfile'
		else { tmpfile.newWriter().withWriter { w ->
  				w << backup
			}
		}

		// If permanent file already exists. Else continue to writing backup below	
		if (new File("inventory/${router}/export.src").exists()) {
		
			// Open both files and compare contents
			def xportfile = new File("inventory/${router}/export.src")
			def diff = tmpfile.text.tokenize( '\n' ) - xportfile.text.tokenize( '\n' )
			
			// if size of diff is 1 return. This means only 'export' timestamp changed.
			if (diff.size() == 1) {
			println(router+"::Backup run. No changes to config.")
			return
			}
			
		}
	
		// Set permanent backup file
		def xportfile = new File("inventory/${router}/export.src")
		xportfile.newWriter().withWriter { w ->
  				w << backup
			}

		
		// Add to version control
		"git -C inventory add ${router}/export.src".execute().waitFor()
		"git -C inventory commit -m backup_${router}".execute().waitFor()
		println(router+"::New config backup.")
		
		// Check if license backed up
		if (!new File("inventory/${router}/*.key").exists()) {
			this.License()
		}
			
		// Update board arch if empty
		if (arch == null || board == null) {
			this.boardInfo()
		}
		
		return
	}
}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////
@groovy.transform.InheritConstructors
class SSHConn extends Router {
	//Subclass implements direct ssh calls if key auth used

	Integer callSSH(command) {
		//subprocess.call execute command via ssh+keys
		
		def call = "ssh -p ${sshport} ${user}@${router} -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${command}".execute()
		call.waitFor()
		return call.exitValue()
	}	


	void  callSCP(file_name) {
		//scp file using ssh + keys
		
		"scp -P ${sshport} -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${user}@${router}:/${file_name}  inventory/${router}".execute().waitFor()
	}

	String checkOutput(command) {
		//check_output from subprocess via ssh+keys
		
		def call = "ssh -p ${sshport} ${user}@${router} -o StrictHostKeyChecking=no -o PasswordAuthentication=no ${command}".execute()
		call.waitFor()
		return call.text
	}
}		
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////
	

@groovy.transform.InheritConstructors
class SSHPass extends Router {
	/*Subclass uses sshpass to call ssh if passwd auth used*/



	Integer callSSH(command) {
		//subprocess.call execute command via sshpass
		
		def call = "sshpass -p ${passwd} ssh -p ${sshport} ${user}@${router} -o StrictHostKeyChecking=no ${command}".execute()
		call.waitFor()
		return call.exitValue()
	}


	void  callSCP(file_name) {
		//scp file using sshpass
		
		"sshpass -p ${passwd} scp -P ${sshport} -o StrictHostKeyChecking=no ${user}@${router}:/${file_name}  inventory/${router}".execute().waitFor()
	}

	String checkOutput(command) {
		//check_output from subprocess via sshpass
		
		def call = "sshpass -p ${passwd} ssh -p ${sshport} ${user}@${router} -o StrictHostKeyChecking=no ${command}".execute()
		call.waitFor()
		return call.text
	}
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////

// Functions

///////////

class Actions {



	static addRouter(args) {
		//* Add router with optional args. Router=hostname required*/

		// define map		
		def map = [:]
		
		// for each argument starting from second, split on "=" and convert to map
		args[1..-1].each {param ->
    		def nameAndValue = param.split("=")
    		map[nameAndValue[0]] = nameAndValue[1]
		}

	
		// Variables to override with default values
		def router = map['router']
		def user = map.get('user', 'admin')
		def passwd = map.get('passwd', '')
		def usekey = map.get('usekey', 0)
		def interval = map.get('interval', 'hourly')
		def sshport = map.get('sshport', 22)
		def online = map.get('online', '')

		// Create directory and database entry											
		new File('inventory/'+router).mkdir()
		new mtrcbackup().sqlConn().execute("INSERT INTO inventory (router, user, passwd, usekey, interval, sshport) VALUES (?, ?, ?, ?, ?, ?)", [router, user, passwd, usekey, interval, sshport])
	
	
		// Based on usekey variable init router subclass. Also confirms database operation
		def condition = new mtrcbackup().sqlConn().rows("SELECT * FROM inventory where router=?", router)[0]
		def new_router = (condition['usekey'] == 0) ? new SSHPass(condition) : new SSHConn(condition)


		// If router passes cli dummy check
		if (new_router.checkRouter() == 0) {
			println(router+"::Added successfully")
			return 
		}
		
		
		// Else throw warning and try to ping it
		else {
			println(router+"::Failure to communicate")
			println(new_router.ping())
		
			// If online argument set to yes remove from database
			// Else it is added, but currently unreachable
			if (online == 'yes') {
				deleteRouter(['dummy', router])
				println(router+"::Removed from DataBase")
			}	
			return
		}
	}
	
	
	
/////////////////

	static deleteRouter(args) {
	//*Delete router from database and remove subdirectory*/
	
	// get router from vars
	def router = args[1]
	
	// Remove from database
	new mtrcbackup().sqlConn().execute("DELETE FROM inventory WHERE router=?", router)
	
	// Delete file. If committed to git before: first will remove second will silently fail
	"git -C inventory rm ${router}".execute().waitFor()
	
	// If not previous command will silently fail and this will remove
	new File('inventory/'+router).deleteDir()
	}


//////////////


	static  editRouter(args) {
		//*Update router information. This can not change hostname*/
	
		// define map		
		def map = [:]
		
		// for each argument starting from second, split on "=" and convert to map
		args[1..-1].each {param ->
    		def nameAndValue = param.split("=")
    		map[nameAndValue[0]] = nameAndValue[1]
		}
	
		// Loop through keys in dictionary
		map.each() {
	
			// If key is one of following
			if ( it.key in ['user', 'passwd', 'usekey', 'interval', 'sshport']) {
				// Update data base
				new mtrcbackup().sqlConn().execute("UPDATE inventory SET ${it.key}=? WHERE router=?", [it.value, map.router])
			}	
		}
	}

/////////////////	



	static changeHostname(args) {
	//*Change hostname for router*/
	
	def router = args[1]
	def newname = args[2]
	
	// Change router name in  data base
	new mtrcbackup().sqlConn().execute("UPDATE inventory SET router=? WHERE router=?",[newname, router])
	
	// Use one of commands below. Git preferred. One that won't work will fail silently
	"git -C inventory mv ${router} ${newname}".execute().waitFor()
	new File('inventory/'+router).renameTo 'inventory/'+newname
	
	}	
/////////////////	
	
	
	static listRouters() {
	"""Generate json dump of routers"""
	
	// Create empty list
	def info = [:]
	
	// Get list of router names
	def list = new mtrcbackup().sqlConn().rows("SELECT * FROM inventory")
	
	// Use groovy json module to print
	def json = JsonOutput.toJson(list)
	println (JsonOutput.prettyPrint(json))
	}
	
/////////////////	
	
	static Setup() {
	/** Setup mtrcbackup for first time */

		// Check if ".setup" file exists. If not proceed.
		if (!new File('.setup').exists()) {
		
			// Create router inventory dir
			new File('inventory').mkdir()
		
			//Create database schema
			new mtrcbackup().sqlConn().execute("""CREATE TABLE inventory(router TEXT PRIMARY KEY, user TEXT, passwd TEXT, usekey INTEGER check("usekey" in (0, 1)), interval TEXT check("interval" in ('hourly', 'daily', 'weekly', 'monthly')), sshport INTEGER check("sshport" BETWEEN 0 AND 65536), arch TEXT, board TEXT)""")
			
			//Set up cron for automatic backups
			SetupCron()

			// Init git repo in inventory dir
			'git -C inventory init'.execute().waitFor()
		
			// Touch ".setup" file to mark that setup has been completed
			'touch .setup'.execute().waitFor()
		}
		
		// Else ".setup" file exists so setup has been run before
		else { println('Looks like setup is done. If you wish to re-run delete .setup file')
		}

		// TO DO: include rollback if setup fails at any point
	}
		
	
////////////////

	static SetupCron() {
		// define script path
		def pwdir = new File("mtrcbackup.groovy").getAbsolutePath()	
			
		// cron command base that forms => @hourly /home/irina/Python/mtrcbackup.py --auto hourly
		def cron_command = pwdir+' --auto'

		// create list of current cron commands
		def crontab_l = []
		'crontab -l'.execute().text.split("\n").each{crontab_l.add(it)}
		
		// map for cro options aligned with --auto option
		def map = ['hourly':'@hourly', 'daily':'@daily', 'weekly':'@weekly', 'monthly':'@monthly']
		
		// create list of commands to add
		def commands = []			
		map.each{ k, v -> def entry = "${v} " + cron_command + " ${k}";
			commands.add(entry)}

		// merge lists and remove duplicates
		commands.addAll(crontab_l)
		String commands_new = commands.unique().join("\n")
			
		//Add to crontab
		['bash', '-c', "echo \"${commands_new}\" | crontab -"].execute().waitFor()
	
	}	

////////////////////

	static autoBackup(args) {
		/*Execute backups automatically based on interval(freq) passed as arg*/
	
		// Check if interval(freq) valid
		if (!['hourly', 'daily', 'weekly', 'monthly'].contains(args[1])) { return }
		def freq = args[1]
		
		// Select all router names where router has corresponding freq
		def all_rows = new mtrcbackup().sqlConn().rows("SELECT router FROM inventory WHERE interval=?", freq)
		all_rows.each() { 
			def condition = new mtrcbackup().sqlConn().rows("SELECT * FROM inventory where router=?", it[0].value.toString())[0]
			def new_router = (condition['usekey'] == 0) ? new SSHPass(condition) : new SSHConn(condition)
			
			try {
				new_router.runBackup()
			}
			catch(Exception ex) {
				println(it[0].value+"::Auto backup fail")
			}
		} 

	}

}

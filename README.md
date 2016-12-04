# mtrcbackup
**M**ikro**T**ik **R**outer **C**onfig **Backup**  Version: 1.0

mtrcbackup is Python3 script for managing Mikrotik router config files.

There is also experimental rewrite in Groovy that seems to be work ok.

It uses flat files under git version control and SQLite database.

Idea behind this script is that it's easier to set up than things like ansible or rancid for noobs.

Also it don't involve any setups or scripts on router other than allowing ssh from server.

You can add, delete, edit and list hosts from cli or edit database directly. 

Remember that hostname must be unique and same both in database and hosts subfolder.

If domain name is used as host name make sure it's reachable: either it's public or in /etc/hosts.

Automatic backups are run by cron. Frequency can be specified as hourly, daily, weekly or monthly.

Backup uses either ssh key based login without passwords or password based login using sshpass.

It's recommended to use ssh keys and not to store password for security.

Also make sure to secure linux host as you will store some passwords in config files probably.

It should run on Debian and RHEL derivatives.

```
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

-c, --change	Change router name. Arguments: current_hostname new_hostname

-e, --edit		Edit router settings in data base. For argument reference check
				-a, --add option above. Allows change of following settings:
				'user', 'passwd', 'usekey', 'interval', 'sshport'

-l, --list		Print JSON format list of routers and their settings in database

--setup			Set up mtrcbackup. Run once at initial program setup

--auto			Run auto backup. Argument: interval (hourly, daily, weekly, monthly)
				Intended for cron use 

-b, --backup	Run backup. Arguments: hostname

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
	Program is using Git for storage so use any operating system Git tools to view config changes. 
```

Copyright (c) 2015, Edgars Simanis

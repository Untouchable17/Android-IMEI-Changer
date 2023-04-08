#!/bin/bash

RED="\033[1;32m\033[1;32m"
WHITE="\033[1;37m\033[1;37m"
RESET="\033[0m" 	
PURPLE="\033[0;35m"

MAKEFILE_PATH=$(pwd)/Makefile
JAVA_MAKEFILE_PATH=$(pwd)/Makefile[Java]
CPP_FILE="IMEI-AndroidChanger.cpp"
TARGET="IMEI-AndroidChanger.o"


function show_banner(){

echo -e "																																									 "
echo -e "	\t███████${RED}╗${RESET}███████${RED}╗${RESET} ██████${RED}╗${RESET}██████${RED}╗ ${RESET}███████${RED}╗${RESET}████████${RED}╗${RESET} 							 "
echo -e "	\t██${RED}╔════╝${RESET}██${RED}╔════╝${RESET}██${RED}╔════╝${RESET}██${RED}╔══${RESET}██${RED}╗${RESET}██${RED}╔════╝${RESET}${RED}╚══${RESET}██${RED}╔══╝${RESET}"
echo -e "	\t███████${RED}╗${RESET}█████${RED}╗${RESET}  ██${RED}║     ${RESET}██${RED}║  ${RESET}██${RED}║${RESET}█████${RED}╗     ${RESET}██${RED}║   ${RESET}				 "
echo -e "	\t${RED}╚════${RESET}██${RED}║${RESET}██${RED}╔══╝  ${RESET}██${RED}║     ${RESET}██${RED}║  ${RESET}██${RED}║${RESET}██${RED}╔══╝     ${RESET}██${RED}║   ${RESET}"
echo -e "	\t███████${RED}║${RESET}███████${RED}╗${RESET}${RED}╚${RESET}██████${RED}╗${RESET}██████${RED}╔╝${RESET}███████${RED}╗   ${RESET}██${RED}║   ${RESET}				 "
echo -e "	\t${RED}╚══════╝╚══════╝ ╚═════╝╚═════╝ ╚══════╝   ╚═╝   ${RESET}																									 "
echo -e "																																									 "
echo -e "${RESET}.______________________________________________________${RED}|_._._._._._._._._._.${RESET}																	 "
echo -e "${RESET} \_____________________________________________________${RED}|_#_#_#_#_#_#_#_#_#_|${RESET}																	 "
echo -e "                                                       ${RED}l          		   ${RESET}																			 "
echo -e "    ${RED}Mobile Equipment Identity IMEI - ${RESET}identify and authenticate the device																			 "
echo -e "   																																								 "
echo -e "           ${WHITE}\t\tCreated by ${RED}SecDet Samurai${RESET}   																									 "
echo -e "   																																								 "

}


function has_make_file() {
    if [ ! -f "$MAKEFILE_PATH" ]; then
        echo -e "${RED}[ERROR]${RESET} Makefile not found\n"
        exit 1
    fi
}

function is_compiled_file(){
	if [[ -f $TARGET ]]; then
	    echo -e "${PURPLE}[*]${RESET} $TARGET is already complited"
	else
	    echo -e "${PURPLE}[*]${RESET} Compiling $TARGET..."
	    make
	    echo -e "${PURPLE}[*]${RESET} $TARGET has been successfully compiled"
	fi
}

function set_package_manager(){
	if command -v apt &> /dev/null; then
        apt update && apt upgrade -y && apt install -y openjdk-17 android-sdk
    elif command -v pkg &> /dev/null; then
        pkg update && pkg upgrade -y && pkg install -y openjdk-17 android-sdk
    else
        echo "${RED}[ERROR]${RESET} Package manager not found"
        exit 1
    fi
}

function java_compile_program(){
	echo -e "${PURPLE}[*]${RESET} Compiling Java IMEI Changer..."
	javac IMEI-AndroidChanger.java
	if [ $? -eq 0 ]; then
		echo -e "${PURPLE}[*]${RESET} Compilation done"
	else
		echo -e "${RED}[ERROR]${RESET} Compiling failed"
		exit 1
	fi
}

function set_java_makefile(){
	 if [ ! -f "$JAVA_MAKEFILE_PATH" ]; then
        echo -e "${RED}[ERROR]${RESET} Java Makefile not found\n"
        exit 1
    fi
}

clear
show_banner

if [[ $EUID -ne 0 ]]; then
   echo -e "${RED}[ERROR]${RESET} Run script as root\n" 
   exit 1
else
	if [ $(uname -o) == "Android" ]; then
		set_package_manager
		java_compile_program
		echo -e "${PURPLE}[*]${RESET} Running Java IMEI Changer..."
		java IMEI-AndroidChanger
	else
		has_make_file
		is_compiled_file
		if [ $? -eq 0 ]; then
			if [ -x ./IMEI-AndroidChanger.o ]; then
				echo -e "${PURPLE}[*]${RESET} Running the program..." && sleep 1
				./IMEI-AndroidChanger.o
			else
				echo -e "${PURPLE}[*]${RESET} Adding executable permission..."
				chmod +x ./IMEI-AndroidChanger.o && ./IMEI-AndroidChanger.o
			fi
		fi
	fi
fi
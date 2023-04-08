<h1 align="center">
    <a href="https://github.com/Untouchable17/Android-IMEI-Changer">
        <img src="https://i.ibb.co/4g8FdtN/SOSI.png" width="700">
    </a>
</h1>

<p align="center">
<a href="https://github.com/Untouchable17/Android-IMEI-Changer"><img src="https://img.shields.io/static/v1?label=version&message=1.0.0&color=green"></a>
<a href="https://github.com/Untouchable17/Android-IMEI-Changer/issues?q=is:issue+is:closed"><img src="https://img.shields.io/github/issues-closed/Untouchable17/Android-IMEI-Changer?color=orange"></a>
</p>

<h1 align="center">Android IMEI Changer</h1>

<b>IMEI </b>stands for International Mobile Equipment Identity. It is a unique identification number that is assigned to each mobile device. The IMEI number is usually 15 digits long and is used to identify the device and to track it if it is lost or stolen. It is an important piece of information that is used by mobile carriers, manufacturers, and law enforcement agencies

<h2 align="center">Installation</h2>

<p align="center">Installing and using program process:</p>

<p align="center">Execute all commands on behalf of the superuser</p>

1. Download or clone this repository.
```
git clone https://github.com/Untouchable17/Android IMEI Changer
```
2. Make the file executable with the chmod +x command
```
chmod +x install.sh
```
3. Run the script
```
./install.sh
```
<p>This script will automatically determine the architecture of your device and depend on it, will download and install all the necessary dependencies, compile programs in Java or C ++</p>

> Other Method: Manual Installation
1. Downloading or cloning this GitHub repository.
```
git clone https://github.com/Untouchable17/Android IMEI Changer
```
2. Update system packages and install requirements

Ubuntu / Debian: 
```
sudo apt update && sudo upgrade -y
sudo apt-get install g++
```
Fedora, CentOS or Red Hat: 
```
sudo yum update && sudo yum upgrade -y
sudo yum install gcc-c++
```
Android:
```
NetHunter: apt update && apt upgrade -y && apt install -y openjdk-17 android-sdk
```
```
Termux: pkg update && pkg upgrade -y && pkg install -y openjdk-17 android-sdk
```
3. Compile files
```
g++ -std=c++11 -Wall -c IMEI-AndroidChanger.cpp -o IMEI-AndroidChanger.o
```
```
javac -cp ".:$ANDROID_HOME/platforms/android-11/android.jar" ImeiManager.java Main.java
```
4. Set +x permission and run one of the program
```
chmod +x {program}
```
<br>You can add the correct path to global like this: `export PATH="$PATH:$(pwd)"` and then you can run the program by just entering their name

<h2 align="center">How to use</h2>
<p>After launching the program automatically will change the IMEI identifiers of the slots of your SIM card. Your task is just to run one of the programs</p>


<h2 align="center">A little explanation of the code and a teaser</h2>

<p>Changing the <b>IMEI</b> (<b>International Mobile Equipment Identity</b>) is illegal in many countries, including the US, Canada, the UK and the EU.</p>

<p>In general, changing the IMEI may violate laws related to consumer protection and/or communications and may lead to legal consequences. In addition, changing the IMEI can cause problems with the operation of the device, as it can lead to the blocking of the device by the network or to incompatibility with software and updates.</p>

<p>The ban on changing the IMEI is due to the fact that the IMEI is used to identify a mobile device in a mobile network. Each device has a unique IMEI that serves to identify and track the device on the network. Changing the IMEI can be due to illegal activities, such as theft or fraud, where a device with a modified IMEI can be sold or used for unauthorized access to the network. In some countries, changing the IMEI is a crime and may be punishable by law.</p>

> Root privileges and access to the kernel boot loader are required for use. Use at your own risk


<h2 align="center">Contact Developer</h2>


    Telegram:           @secdet17
    Group:              t.me/secdet_team
    Email:              tylerblackout17@gmail.com


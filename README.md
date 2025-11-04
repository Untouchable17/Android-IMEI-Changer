<h1 align="center">
    <a href="https://github.com/Untouchable17/Android-IMEI-Changer">
        <img src="https://i.ibb.co/4g8FdtN/SOSI.png" width="700">
    </a>
</h1>

<p align="center">
<a href="https://github.com/Untouchable17/Android-IMEI-Changer"><img src="https://img.shields.io/static/v1?label=version&message=7.0.0&color=green"></a>
<a href="https://github.com/Untouchable17/Android-IMEI-Changer/issues?q=is:issue+is:closed"><img src="https://img.shields.io/github/issues-closed/Untouchable17/Android-IMEI-Changer?color=orange"></a>
</p>

<h1 align="center">Android IMEI Changer</h1>

The **IMEI** (International Mobile Equipment Identity) is a unique 15-digit identifier assigned to each mobile device during manufacturing. It serves critical functions in telecommunications, including device authentication on cellular networks, tracking for security purposes (e.g., in cases of loss or theft), and compliance with regulatory standards. Mobile carriers, device manufacturers, and law enforcement agencies rely on IMEI for network management, fraud prevention, and legal investigations.

**Critical Warning:** Modifying the IMEI is strictly illegal in numerous jurisdictions, including but not limited to the United States, Canada, the United Kingdom, and member states of the European Union. Such modifications may contravene laws pertaining to telecommunications, consumer protection, and fraud prevention, potentially resulting in severe legal penalties, including fines or imprisonment. Furthermore, IMEI alterations can lead to irreversible device malfunctions, permanent network blacklisting, incompatibility with over-the-air (OTA) software updates, or complete device bricking (rendering it inoperable). This repository and its contents are provided solely for educational and research purposes in controlled environments. The developer disclaims all liability for any misuse. Users must independently verify the legality and risks in their jurisdiction before proceeding.

**Device-Specific Dependencies and Limitations:** The effectiveness of this tool is highly contingent on several factors, including:
- **Chipset and Hardware:** Primarily compatible with MediaTek (MTK) chipsets via AT commands; limited or non-functional on Qualcomm Snapdragon, Samsung Exynos, or other architectures without significant modifications.
- **Firmware and Kernel:** Requires a compatible custom or stock firmware that allows access to protected partitions (e.g., NVRAM or EFS). SELinux enforcement in modern kernels (Android 10+) may block changes; permissive mode or custom kernels may be necessary.
- **Android Version:** More feasible on older versions (Android 9–11); increasingly restricted on Android 12+ due to enhanced security features like verified boot and hardware-backed attestation.
- **Root and Bootloader Status:** Mandatory root access (e.g., via Magisk) and an unlocked bootloader. Without these, modifications are impossible.
- **Device Model and Vendor Customizations:** Vendor-specific implementations (e.g., Samsung's EFS partition) introduce variability; success rates vary widely, and testing on identical hardware is recommended.
- **Persistence and Risks:** Changes may not persist after reboots, factory resets, or firmware flashes. Incorrect operations can corrupt modem firmware, leading to loss of cellular connectivity.

Users are strongly advised to perform extensive research on their specific device model, consult community forums (e.g., XDA Developers), and create full system backups before attempting any modifications. If compatibility issues arise, alternative tools or professional services may be required.

<h2 align="center">Requirements</h2>

- Root privileges (recommended: Magisk v25+ for stability and module support).
- Unlocked bootloader (enables root and low-level access; note: this voids warranty on most devices).
- ARM-based Android device (verified on MTK; test others at your own risk).
- Development tools:
  - C++ compiler (e.g., g++ for GNU/Linux or Clang on Android).
  - Java Development Kit (JDK 17 or later).
  - Android SDK (required for Java compilation; specifically, android.jar from API level 11 or higher).
- Environment setup: Ensure `ANDROID_HOME` is configured correctly for Android-specific builds.
- Backup tools: Utilities like `dd` for partitioning backups, accessible via root shell.

<h2 align="center">Installation</h2>

This tool requires manual installation due to the variability in environments and dependencies. Automated scripts are not provided to avoid compatibility issues. Execute commands in a terminal with appropriate privileges (e.g., via ADB shell or Termux with root).

1. Clone the repository:
   ```
   git clone https://github.com/Untouchable17/Android-IMEI-Changer
   ```
2. Navigate to the repository directory:
   ```
   cd Android-IMEI-Changer
   ```
3. Update system packages and install dependencies:

   - **Ubuntu/Debian-based systems**:
     ```
     sudo apt update && sudo apt upgrade -y
     sudo apt install g++ openjdk-17-jdk android-sdk
     ```
   - **Fedora/CentOS/Red Hat-based systems**:
     ```
     sudo dnf update && sudo dnf upgrade -y  # Use yum for older versions
     sudo dnf install gcc-c++ java-17-openjdk-devel android-sdk
     ```
   - **Android (via Termux or Kali NetHunter)**:
     ```
     pkg update && pkg upgrade -y  # Or apt for NetHunter
     pkg install openjdk-17 android-sdk clang  # Clang as alternative to g++
     ```
     Note: Configure `ANDROID_HOME` (e.g., `export ANDROID_HOME=/path/to/android-sdk`) and ensure SDK platforms are installed.

4. Compile the source files:
   - For C++ components:
     ```
     g++ -std=c++11 -Wall -c IMEI-AndroidChanger.cpp -o IMEI-AndroidChanger.o
     ```
     (Link additional objects if required for a full executable.)
   - For Java components:
     ```
     javac -cp ".:$ANDROID_HOME/platforms/android-11/android.jar" ImeiManager.java Main.java ImeiChanger.java
     ```
     (Include all relevant .java files; adjust API level if needed for compatibility.)

5. Grant execute permissions:
   ```
   chmod +x IMEI-AndroidChanger.o Main.class  # Adjust based on compiled outputs
   ```

To make executables globally accessible:
```
export PATH="$PATH:$(pwd)"
```
This allows invocation by name from any directory.

<h2 align="center">Usage</h2>

Prior to execution:
- Verify root access: Run `su` in a shell and confirm UID 0.
- Backup critical partitions: E.g., `su -c "dd if=/dev/block/bootdevice/by-name/nvram of=/sdcard/nvram_backup.img"` (adapt partition paths to your device).
- Check current IMEI: Dial `*#06#` or use `adb shell service call iphonesubinfo 1`.

Launch the compiled program (e.g., `java ImeiChanger` for Java or `./IMEI-AndroidChanger.o` for C++). The tool attempts to modify IMEI values for SIM slots using low-level interfaces. No interactive input is required, but monitor logs for errors.

Post-execution:
- Reboot the device: `reboot`.
- Verify changes: Redial `*#06#`.
- If changes fail or revert, inspect device logs (`adb logcat`) for issues related to modem or SELinux.

Note: Success is not guaranteed; if unsuccessful, consider device-specific alternatives like QPST for Qualcomm or MTK Engineering Mode.

<h2 align="center">Technical Details and Legal Considerations</h2>

### Technical Overview
IMEI is stored in non-volatile memory (NVRAM) or encrypted file systems (EFS) on the device's baseband processor (modem). This storage is protected to prevent tampering, ensuring network integrity. Modification typically involves:
- **Low-Level Access:** Root privileges grant access to block devices (e.g., `/dev/block/...`) or modem interfaces (e.g., `/dev/radio/pttycmd1` for MTK).
- **Command Execution:** For MTK, AT commands (e.g., `AT+EGMR=1,7,"NEW_IMEI"`) are echoed to serial ports, altering NVRAM entries. Qualcomm devices may require QCN file editing via specialized tools like QPST, involving hex manipulation of NV items.
- **Validation and Generation:** IMEI follows the Luhn algorithm for checksum validation. Generated IMEIs must include a valid Type Allocation Code (TAC) to avoid immediate network rejection.
- **Persistence Mechanisms:** Changes interact with the baseband firmware, but modern Android's hardware abstraction layers (HAL) and security policies (e.g., dm-verity) can revert or block them.
- **Limitations and Failure Modes:** Incompatibilities arise from vendor-locked bootloaders, encrypted partitions, or firmware signatures. Post-modification, devices may fail carrier authentication, leading to "No Service" errors or IMEI nullification.

This tool implements these operations in C++ (for native performance and low-level I/O) and Java (for Android API integration). However, due to the heterogeneous nature of Android ecosystems, universal compatibility is unattainable. Users should reference device-specific documentation (e.g., from XDA or kernel sources) for partition mappings and command variants.

### Legal and Ethical Considerations
The IMEI system's integrity is fundamental to global telecommunications security. Tampering undermines efforts to combat device theft, counterfeit hardware, and network fraud. International bodies like GSMA maintain IMEI databases (e.g., for blacklisting), and alterations can trigger automated bans. Ethically, such modifications may enable illicit activities, eroding trust in mobile networks.

Always prioritize legal compliance and consider alternatives like official IMEI repair services through authorized centers.

<h2 align="center">Contact the Developer</h2>

- Group & Contact: t.me/secdet_team
- Email: tylerblackout17@gmail.com

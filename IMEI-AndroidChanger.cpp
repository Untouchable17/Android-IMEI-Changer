#include <iostream>
#include <cstdlib>
#include <unistd.h>
#include <random>
#include <sstream>
#include <vector>
#include <algorithm>
#include <regex>
#include <string>


class ImeiManager {
    public: explicit ImeiManager(
        std::vector < std::string > imeis): imeis_(std::move(imeis)
    ) {}

    void GenerateNewImeis() {
        std::random_device rd;
        std::mt19937 gen(rd());
        std::uniform_int_distribution<long long> dis(100000000000000, 999999999999999);

        for (std::vector<std::string>::size_type i = 0; i < imeis_.size(); i++) {
            std::string new_imei = std::to_string(dis(gen));
            while (!IsValidImei(new_imei) || std::find(
                new_imeis_.begin(), new_imeis_.end(), new_imei) != new_imeis_.end()) {
                new_imei = std::to_string(dis(gen));
            }
            new_imeis_.push_back(new_imei);
            std::cout << "[*] New IMEI for slot " << i + 1 << ": " << new_imei << std::endl;

            if (!WriteImeiToDevice(i, new_imei)) {
                std::cerr << "[*] Failed to write new IMEI for slot " << i + 1 << std::endl;
            }
        }

    }
    const std::vector < std::string > & GetNewImeis() const {
        return new_imeis_;
    }

    private: std::vector < std::string > imeis_;
    std::vector < std::string > new_imeis_;

    bool IsValidImei(const std::string & imei) const {
        if (imei.length() != 15 && imei.length() != 16) {
            return false;
        }

        std::regex pattern("^[0-9]{15,16}$");
        return std::regex_match(imei, pattern);
    }

    bool WriteImeiToDevice(int slot,
        const std::string & imei) const {
        std::stringstream ss;
        ss << "service call iphonesubinfo 1 i32 " << slot << " s16 " << imei;
        return system(ss.str().c_str()) == 0;
    }
};

int main() {

    if (getuid() != 0) {
        if (system("su") != 0) {
            std::cerr << "[*] Failed to get root access" << std::endl;
            return 1;
        }
    }

    FILE* pipe = popen("service call iphonesubinfo 1 | awk -F\"'\" '/^  gsm/{print $2}'", "r");
    if (!pipe) {
        std::cerr << "[*] Failed to read IMEI." << std::endl;
        return 1;
    }

    char buffer[1024];
    std::vector<std::string> imeis;
    while (fgets(buffer, sizeof(buffer), pipe)) {
        std::string imei = buffer;
        imei.erase(std::remove(imei.begin(), imei.end(), '\n'), imei.end());
        imeis.push_back(imei);
    }
    pclose(pipe);

    ImeiManager imei_manager(std::move(imeis));
    for (const auto& imei : imei_manager.GetNewImeis()) {
        std::cout << "[*] IMEI: " << imei << std::endl;
    }

    imei_manager.GenerateNewImeis();

    std::cout << "[*] New IMEI:" << std::endl;
    const std::vector<std::string>& new_imeis = imei_manager.GetNewImeis();
    for (const auto& imei : new_imeis) {
        std::cout << imei << std::endl;
    }

    return 0;
}
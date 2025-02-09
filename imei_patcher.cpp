#include <iostream>
#include <string>
#include <cstdlib>
#include <unistd.h>
#include <regex>
#include <sys/stat.h>
#include <cstdio>

using namespace std;

#define RESET   "\033[0m"
#define RED     "\033[31m"
#define GREEN   "\033[32m"
#define YELLOW  "\033[33m"
#define CYAN    "\033[36m"

bool checkOperatorAccess() {
    return (getuid() == 0);
}

bool verifyNumericImei(const string &strImei) {
    return strImei.find_first_not_of("0123456789") == string::npos &&
           (strImei.size() == 14 || strImei.size() == 15);
}

int calcLuhnDigit(const string &coreImei) {
    int sum = 0;
    int len = coreImei.size();
    for (int i = len - 1; i >= 0; i--) {
        int digit = coreImei[i] - '0';
        if (((len - i) % 2) == 0) {
            digit *= 2;
            if (digit > 9)
                digit -= 9;
        }
        sum += digit;
    }
    int check = (10 - (sum % 10)) % 10;
    return check;
}

bool validateLuhn(const string &fullImei) {
    int sum = 0;
    int len = fullImei.size();
    for (int i = len - 1; i >= 0; i--) {
        int digit = fullImei[i] - '0';
        if (((len - i) % 2) == 0) {
            digit *= 2;
            if (digit > 9)
                digit -= 9;
        }
        sum += digit;
    }
    return (sum % 10 == 0);
}

/*
    Формирование поддельного IMEI:
    1. Если 14 цифр — вычисляем контрольную цифру
    2. Если 15 — валидируем по алгоритму
*/
string forgeImei(const string &rawImei) {
    if (!verifyNumericImei(rawImei)) {
        return "";
    }
    if (rawImei.size() == 15) {
        if (!validateLuhn(rawImei))
            return "";
        return rawImei;
    } else {
        int checkDigit = calcLuhnDigit(rawImei);
        return rawImei + to_string(checkDigit);
    }
}


bool isFilePresent(const string &filePath) {
    struct stat buffer;
    return (stat(filePath.c_str(), &buffer) == 0);
}

// Метод 1: Патчинг IMEI через системный вызов
bool overrideImeiViaSvc(const string &imei) {
    string cmd = "service call iphonesubinfo 8 i32 1 s16 " + imei;
    int res = system(cmd.c_str());
    return (res == 0);
}

// Метод 2: Патчинг IMEI через запись в системный файл (для устройств Qualcomm)
bool overrideImeiViaFile(const string &imei, const string &filePath = "/persist/radio/imei") {
    FILE* file = fopen(filePath.c_str(), "w");
    if (!file) return false;

    fprintf(file, "%s\n", imei.c_str());
    fclose(file);

    chmod(filePath.c_str(), 0644);

    system("setprop sys.radio.restart 1");
    return true;
}

int main(int argc, char* argv[]) {
    if (!checkOperatorAccess()) {
        cerr << RED << "[*] Запуск возможен только наличием ROOT прав. Используйте оболочку рута или запускайте через sudo или su" << RESET << "\n";
        return EXIT_FAILURE;
    }

    if (argc < 2 || argc > 3) {
        cerr << YELLOW << "[*] Использование: " << argv[0] << " <Новый IMEI> [--method=auto|service|file]" << RESET << "\n";
        return EXIT_FAILURE;
    }

    string rawImei = argv[1];
    string forgedImei = forgeImei(rawImei);
    if (forgedImei.empty()) {
        cerr << RED << "[*] Неверный формат IMEI или ошибка проверки по алгоритму Луна. Ожидается 14 или 15 цифр." << RESET << "\n";
        return EXIT_FAILURE;
    }

    // Определение режима патчинга: auto, service или file
    string mode = "auto";
    if (argc == 3) {
        string modeArg = argv[2];
        string modePrefix = "--method=";
        if (modeArg.find(modePrefix) == 0) {
            mode = modeArg.substr(modePrefix.size());
        } else {
            mode = modeArg;
        }
        if (mode != "auto" && mode != "service" && mode != "file") {
            cerr << RED << "[*] Неверный метод. Используйте auto, service или file." << RESET << "\n";
            return EXIT_FAILURE;
        }
    }

    bool patchSuccess = false;
    if (mode == "service") {
        cout << CYAN << "[*] Применяем системный вызов..." << RESET << "\n";
        patchSuccess = overrideImeiViaSvc(forgedImei);
    } else if (mode == "file") {
        cout << CYAN << "[*] Патчим IMEI через запись в файл..." << RESET << "\n";
        patchSuccess = overrideImeiViaFile(forgedImei);
    } else {
        const string persistPath = "/persist/radio/imei";
        if (isFilePresent(persistPath)) {
            cout << CYAN << "[*] Обнаружен " << persistPath << ". Патчим через запись в файл..." << RESET << "\n";
            patchSuccess = overrideImeiViaFile(forgedImei, persistPath);
        } else {
            cout << CYAN << "[*] " << persistPath << " не найден. Применяем системный вызов..." << RESET << "\n";
            patchSuccess = overrideImeiViaSvc(forgedImei);
        }
    }

    if (!patchSuccess) {
        cerr << RED << "[!] Ошибка патча IMEI. Проверьте устройство и выбранный метод" << RESET << "\n";
        return EXIT_FAILURE;
    }

    cout << GREEN << "[+] IMEI успешно переписан: " << forgedImei << ". Необходимо перезагрузить устройство" << RESET << "\n";
    return EXIT_SUCCESS;
}

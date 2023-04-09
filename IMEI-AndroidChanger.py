import os
import re
import random


class ImeiManager:
    def __init__(self, imeis):
        self.imeis = imeis
        self.new_imeis = []

    def generate_new_imeis(self):
        for i, imei in enumerate(self.imeis):
            while True:
                new_imei = str(random.randint(100000000000000, 999999999999999))
                if self.is_valid_imei(new_imei) and new_imei not in self.new_imeis:
                    break
            self.new_imeis.append(new_imei)
            print(f"[*] New IMEI for slot {i + 1}: {new_imei}")
            if not self.write_imei_to_device(i, new_imei):
                print(f"[*] Failed to write new IMEI for slot {i + 1}")

    def get_new_imeis(self):
        return self.new_imeis

    @staticmethod
    def is_valid_imei(imei):
        return bool(re.match("^[0-9]{15,16}$", imei))

    @staticmethod
    def write_imei_to_device(slot, imei):
        command = f"service call iphonesubinfo 1 i32 {slot} s16 {imei}"
        return os.system(command) == 0


if __name__ == "__main__":
    if os.geteuid() != 0:
        if os.system("su") != 0:
            print("[*] Failed to get root access")
            exit(1)

    pipe = os.popen("service call iphonesubinfo 1 | awk -F\"'\" '/^  gsm/{{print $2}}'")
    imeis = [imei.strip() for imei in pipe.readlines()]
    pipe.close()

    imei_manager = ImeiManager(imeis)
    for imei in imei_manager.get_new_imeis():
        print(f"[*] IMEI: {imei}")

    imei_manager.generate_new_imeis()

    print("[*] New IMEI:")
    for imei in imei_manager.get_new_imeis():
        print(imei)
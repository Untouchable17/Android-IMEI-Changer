import os
import re
import random
from typing import List


class IMEIChanger:
    def __init__(self, imeis: List[str]):
        self.imeis = imeis
        self.new_imeis = []

    def generate_new_imeis(self) -> None:
        for i, imei in enumerate(self.imeis):
            while True:
                new_imei = str(random.randint(100000000000000, 999999999999999))
                if self.is_valid_imei(new_imei) and new_imei not in self.new_imeis:
                    break
            self.new_imeis.append(new_imei)
            print(f"[*] New IMEI for slot {i + 1}: {new_imei}")
            if not self.write_imei_to_device(i, new_imei):
                print(f"[*] Failed to write new IMEI for slot {i + 1}")

    def get_new_imeis(self) -> List[str]:
        return self.new_imeis

    @staticmethod
    def is_valid_imei(imei: str) -> bool:
        if len(imei) not in (15, 16):
            return False
        pattern = re.compile("^[0-9]{15,16}$")
        return pattern.match(imei) is not None

    @staticmethod
    def write_imei_to_device(slot: int, imei: str) -> bool:
        cmd = f"service call iphonesubinfo 1 i32 {slot} s16 {imei}"
        return os.system(cmd) == 0


def has_root() -> bool:
    if os.getuid() != 0:
        if os.system("su") != 0:
            print("[*] Failed to get root access")
            return False
    return True


def get_imeis() -> List[str]:
    pipe = os.popen("service call iphonesubinfo 1 | awk -F\"'\" '/^  gsm/{{print $2}}'")
    output = pipe.read()
    pipe.close()

    imeis = [imei.strip() for imei in output.split('\n') if imei.strip()]
    return imeis


def main() -> None:
    if not has_root():
        exit(1)

    imeis = get_imeis()
    imei_manager = IMEIChanger(imeis)

    print("[*] IMEI:")
    for imei in imei_manager.get_new_imeis():
        print(imei)

    imei_manager.generate_new_imeis()

    print("[*] New IMEI:")
    for imei in imei_manager.get_new_imeis():
        print(imei)


if __name__ == "__main__":
  main()

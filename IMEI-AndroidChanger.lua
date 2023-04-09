local function is_valid_imei(imei)
    if #imei ~= 15 and #imei ~= 16 then
        return false
    end

    local pattern = "^[0-9]{15,16}$"
    return imei:match(pattern) ~= nil
end

local function write_imei_to_device(slot, imei)
    local cmd = string.format("service call iphonesubinfo 1 i32 %d s16 %s", slot, imei)
    return os.execute(cmd) == 0
end

local function read_imeis_from_device()
    local pipe = io.popen("service call iphonesubinfo 1 | awk -F\"'\" '/^  gsm/{print $2}'")
    if not pipe then
        return nil
    end

    local imeis = {}
    for imei in pipe:lines() do
        imei = imei:gsub("\n", "")
        table.insert(imeis, imei)
    end
    pipe:close()

    return imeis
end

local function generate_new_imeis(imeis)
    math.randomseed(os.time())

    local new_imeis = {}
    for i, imei in ipairs(imeis) do
        local new_imei = tostring(math.random(100000000000000, 999999999999999))
        while not is_valid_imei(new_imei) or table.contains(new_imeis, new_imei) do
            new_imei = tostring(math.random(100000000000000, 999999999999999))
        end

        table.insert(new_imeis, new_imei)
        print(string.format("[*] New IMEI for slot %d: %s", i, new_imei))

        if not write_imei_to_device(i, new_imei) then
            io.stderr:write(string.format("[*] Failed to write new IMEI for slot %d\n", i))
        end
    end

    return new_imeis
end

local function main()
    if os.geteuid() ~= 0 then
        if os.execute("su") ~= 0 then
            io.stderr:write("[*] Failed to get root access\n")
            return 1
        end
    end

    local imeis = read_imeis_from_device()
    if not imeis then
        io.stderr:write("[*] Failed to read IMEI\n")
        return 1
    end

    local imei_manager = {
        imeis = imeis,
        new_imeis = {},
        generate_new_imeis = generate_new_imeis,
        get_new_imeis = function(self)
            return self.new_imeis
        end,
    }

    for _, imei in ipairs(imei_manager:get_new_imeis()) do
        print(string.format("[*] IMEI: %s", imei))
    end

    imei_manager:generate_new_imeis()

    print("[*] New IMEI:")
    for _, imei in ipairs(imei_manager.new_imeis) do
        print(imei)
    end

    return 0
end

os.exit(main())
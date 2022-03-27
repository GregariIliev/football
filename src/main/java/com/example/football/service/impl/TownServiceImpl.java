package com.example.football.service.impl;

import com.example.football.models.dto.TownSeedDto;
import com.example.football.models.entity.Town;
import com.example.football.repository.TownRepository;
import com.example.football.service.TownService;
import com.example.football.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class TownServiceImpl implements TownService {
    private static final String TOWN_FILE_PATH = "src/main/resources/files/json/towns.json";

    private final TownRepository townRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;

    public TownServiceImpl(TownRepository townRepository, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson) {
        this.townRepository = townRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
    }


    @Override
    public boolean areImported() {
        return townRepository.count() > 0;
    }

    @Override
    public String readTownsFileContent() throws IOException {
        return Files.readString(Path.of(TOWN_FILE_PATH));
    }

    @Override
    public String importTowns() throws FileNotFoundException {
        StringBuffer stringBuffer = new StringBuffer();

        Arrays.stream(gson.fromJson(new FileReader(TOWN_FILE_PATH), TownSeedDto[].class))
                .filter(dto -> {
                    boolean valid = validationUtil.isValid(dto);

                    if (valid){
                        stringBuffer.append(String.format("Successfully imported Town %s - %d"
                                ,dto.getName() ,dto.getPopulation())).append(System.lineSeparator());

                        return true;
                    }

                    stringBuffer.append("Invalid Town").append(System.lineSeparator());
                    return false;
                }).map(validDto -> modelMapper.map(validDto, Town.class))
                .forEach(townRepository::save);

        return stringBuffer.toString();
    }

    @Override
    public boolean existTownByName(String name) {
        return townRepository.existsTownByName(name);
    }

    @Override
    public Town getTownByName(String townName) {
        return townRepository.getTownByName(townName);
    }
}

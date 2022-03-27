package com.example.football.service.impl;

import com.example.football.models.dto.StatSeedDto;
import com.example.football.models.entity.Stat;
import com.example.football.repository.StatRepository;
import com.example.football.service.StatService;
import com.example.football.util.ValidationUtil;
import com.example.football.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StatServiceImpl implements StatService {
    private static final String STAT_FILE_PATH = "src/main/resources/files/xml/stats.xml";

    private final StatRepository statRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;

    public StatServiceImpl(StatRepository statRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser) {
        this.statRepository = statRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
    }

    @Override
    public boolean areImported() {
        return statRepository.count() > 0;
    }

    @Override
    public String readStatsFileContent() throws IOException {
        return Files.readString(Path.of(STAT_FILE_PATH));
    }

    @Override
    public String importStats() throws JAXBException, FileNotFoundException {
        StringBuffer stringBuffer = new StringBuffer();

        xmlParser.fromFile(STAT_FILE_PATH, StatSeedDto.class).getStat()
                .stream().filter(dto -> {
                    boolean valid = validationUtil.isValid(dto);

                    if (valid && !statRepository.existsStatByPassingAndShootingAndEndurance(dto.getPassing(), dto.getShooting(), dto.getEndurance())){
                        stringBuffer.append(String.format("Successfully imported Stat %.2f - %.2f - %.2f"
                        , dto.getShooting(),dto.getPassing(), dto.getEndurance())).append(System.lineSeparator());

                        return true;
                    }

                    stringBuffer.append("Invalid Stat").append(System.lineSeparator());
                    return false;

                }).map(validDto -> modelMapper.map(validDto, Stat.class))
                .forEach(statRepository::save);

        return stringBuffer.toString();
    }

    @Override
    public Stat getStatById(Long id) {
        return statRepository.getById(id);
    }
}

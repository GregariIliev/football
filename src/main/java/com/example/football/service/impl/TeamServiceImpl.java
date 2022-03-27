package com.example.football.service.impl;

import com.example.football.models.dto.TeamSeedDto;
import com.example.football.models.entity.Team;
import com.example.football.models.entity.Town;
import com.example.football.service.TeamService;
import com.example.football.service.TownService;
import com.example.football.util.ValidationUtil;
import com.google.gson.Gson;
import com.example.football.repository.TeamRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class TeamServiceImpl implements TeamService {
    private static final String TEAM_FILE_PATH = "src/main/resources/files/json/teams.json";

    private final TeamRepository teamRepository;
    private final TownService townService;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;

    public TeamServiceImpl(TeamRepository teamRepository, TownService townService, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson) {
        this.teamRepository = teamRepository;
        this.townService = townService;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return teamRepository.count() > 0;
    }

    @Override
    public String readTeamsFileContent() throws IOException {
        return Files.readString(Path.of(TEAM_FILE_PATH));
    }

    @Override
    public String importTeams() throws FileNotFoundException {
        StringBuffer stringBuffer = new StringBuffer();

       Arrays.stream(gson.fromJson(new FileReader(TEAM_FILE_PATH), TeamSeedDto[].class))
               .filter(dto -> {
                   boolean valid = validationUtil.isValid(dto);

                   boolean townExist = townService.existTownByName(dto.getTownName());

                   if (valid && !teamRepository.existsTeamByName(dto.getName()) && townExist){
                       stringBuffer.append(String.format("Successfully imported Team %s - %d"
                       ,dto.getName(), dto.getFanBase())).append(System.lineSeparator());

                       return true;
                   }

                   stringBuffer.append("Invalid Team").append(System.lineSeparator());
                   return false;
               }).map(validDto -> {
                   Team team = modelMapper.map(validDto, Team.class);
                   Town town = townService.getTownByName(validDto.getTownName());

                   team.setTown(town);

                   return team;
               }).forEach(teamRepository::save);

        return stringBuffer.toString();
    }

    @Override
    public Team getTeamByName(String name) {
        return teamRepository.getTeamByName(name);
    }
}

package com.example.football.service.impl;

import com.example.football.models.dto.PlayerSeedDto;
import com.example.football.models.entity.Player;
import com.example.football.models.entity.Stat;
import com.example.football.models.entity.Team;
import com.example.football.models.entity.Town;
import com.example.football.repository.PlayerRepository;
import com.example.football.service.PlayerService;
import com.example.football.service.StatService;
import com.example.football.service.TeamService;
import com.example.football.service.TownService;
import com.example.football.util.ValidationUtil;
import com.example.football.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {
    private static final String PLAYER_FILE_PATH = "src/main/resources/files/xml/players.xml";

    private final PlayerRepository playerRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final TownService townService;
    private final TeamService teamService;
    private final StatService statService;

    public PlayerServiceImpl(PlayerRepository playerRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, TownService townService, TeamService teamService, StatService statService) {
        this.playerRepository = playerRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.townService = townService;
        this.teamService = teamService;
        this.statService = statService;
    }

    @Override
    public boolean areImported() {
        return playerRepository.count() > 0;
    }

    @Override
    public String readPlayersFileContent() throws IOException {
        return Files.readString(Path.of(PLAYER_FILE_PATH));
    }

    @Override
    public String importPlayers() throws JAXBException, FileNotFoundException {
        StringBuffer stringBuffer = new StringBuffer();

        xmlParser.fromFile(PLAYER_FILE_PATH, PlayerSeedDto.class).getPlayers()
                .stream().filter(dto -> {
                    boolean valid = validationUtil.isValid(dto);

                    if (valid && !playerRepository.existsPlayerByEmail(dto.getEmail())){
                        stringBuffer.append(String.format("Successfully imported Player %s %s - %s"
                        ,dto.getFirstName(), dto.getLastName(), dto.getPosition()))
                                .append(System.lineSeparator());

                        return true;
                    }
                    stringBuffer.append("Invalid Player").append(System.lineSeparator());
                    return false;
                }).map(validDto -> {

                    Player player = modelMapper.map(validDto, Player.class);
                    Team team = teamService.getTeamByName(validDto.getTeam().getName());
                    Town town = townService.getTownByName(validDto.getTown().getName());
                    Stat stat = statService.getStatById(validDto.getStat().getId());

                    player.setTeam(team);
                    player.setTown(town);
                    player.setStat(stat);

                    return player;
                }).forEach(playerRepository::save);

        return stringBuffer.toString();
    }

    @Override
    public String exportBestPlayers() {
        List<Player> bestPlayers = playerRepository.getBestPlayers();

        StringBuffer stringBuffer = new StringBuffer();

        bestPlayers.forEach(player -> {
            stringBuffer.append(String.format("""
                    Player - %s %s
                    \tPosition - %s
                    \tTeam - %s
                    \tStadium - %s
                    """,player.getFirstName(), player.getLastName(), player.getPosition()
            ,player.getTeam().getName(), player.getTeam().getStadiumName()));
        });

        return stringBuffer.toString();
    }
}

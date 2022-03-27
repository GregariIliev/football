package com.example.football.repository;


import com.example.football.models.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsPlayerByEmail(String email);


    @Query("select p from Player p join fetch p.stat " +
            "where p.birthDate > '1995-01-01' or  p.birthDate < '2003-01-01'" +
            "order by p.stat.shooting desc , p.stat.passing desc , p.stat.endurance desc , p.lastName")
    List<Player> getBestPlayers();
}
//select p.first_name, p.last_name, s.shooting, s.passing, s.endurance
//from players p
//         join stats s on s.id = p.stat_id
//where p.birth_date > '1995-01-01'
//  or p.birth_date < '2003-01-01'
//order by s.shooting desc, s.passing desc, s.endurance desc, p.last_name;
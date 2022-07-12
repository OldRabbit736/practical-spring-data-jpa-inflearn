package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TeamRepositoryTest {

    @Autowired
    TeamRepository teamRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    void teamSetup() {
        Team team1 = new Team("Tiger", "Korea");
        Team team2 = new Team("Bear", "Korea");
        Team team3 = new Team("Dragon", "Korea");
        Team team4 = new Team("Phoenix", "England");
        Team team5 = new Team("Ranger", "England");
        teamRepository.save(team1);
        teamRepository.save(team2);
        teamRepository.save(team3);
        teamRepository.save(team4);
        teamRepository.save(team5);


        Member member1 = new Member("member1", 20, team1);
        Member member2 = new Member("member2", 20, team2);
        Member member3 = new Member("member3", 20, team3);
        Member member4 = new Member("member4", 20, team4);
        Member member5 = new Member("member5", 20, team5);
        Member member6 = new Member("member6", 20, team2);
        Member member7 = new Member("member7", 20, team4);
        Member member8 = new Member("member8", 20, team5);
        Member member9 = new Member("member9", 20, team1);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        memberRepository.save(member5);
        memberRepository.save(member6);
        memberRepository.save(member7);
        memberRepository.save(member8);
        memberRepository.save(member9);

        em.flush();
        em.clear();
    }

    // application.yml 파일에서 default_batch_fetch_size: 100 값을 주고 테스트
    // Data JPA Repository에서도 batch fetch가 작동하는지 확인하기 위한 테스트
    // 결론: 잘 작동함
    // 먼저 Team을 페이징과 정렬 쿼리로 잘 가져옴
    // 그 후 Team에 연관된 Member를 "in 절" 사용하여 한꺼번에 쿼리함)
    @Test
    void findByNationalityTest() {
        teamSetup();

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "name"));

        Page<Team> korea = teamRepository.findByNationality("Korea", pageRequest);

        List<Team> content = korea.getContent();

        List<TeamDto> collect = content.stream().map(TeamDto::new).toList();

        collect.forEach(System.out::println);

    }

    static class TeamDto {

        private String name;
        private String nationality;
        private List<MemberDto> members;

        public TeamDto(Team team) {
            this.name = team.getName();
            this.nationality = team.getNationality();
            this.members = team.getMembers().stream()
                    .map(MemberDto::new)
                    .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return "TeamDto{" +
                    "name='" + name + '\'' +
                    ", nationality='" + nationality + '\'' +
                    ", members=" + members +
                    '}';
        }
    }

    static class MemberDto {

        private String name;
        private int age;

        public MemberDto(Member member) {
            this.name = member.getUsername();
            this.age = member.getAge();
        }

        @Override
        public String toString() {
            return "MemberDto{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

}
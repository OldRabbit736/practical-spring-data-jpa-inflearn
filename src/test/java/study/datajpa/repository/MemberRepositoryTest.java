package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.NonUniqueResultException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    @Test
    public void testMember() {
        System.out.println("memberRepository = " + memberRepository.getClass());

        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertEquals(member.getId(), findMember.getId());
        assertEquals(member.getUsername(), findMember.getUsername());
        assertEquals(member, findMember);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertEquals(member1, findMember1);
        assertEquals(member2, findMember2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertEquals(2, all.size());

        // 카운트 검증
        long count = memberRepository.count();
        assertEquals(2, count);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long afterDeleteCount = memberRepository.count();
        assertEquals(0, afterDeleteCount);
    }

    @Test
    void findByUsernameAndAgeGreaterThan() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertSame(member2, members.get(0));
        assertEquals(1, members.size());
    }

    @Test
    void test() {
        List<Member> top3By = memberRepository.findTop3By();
        top3By.forEach(System.out::println);
    }

    @Test
    void testNamedQuery() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsername("AAA");
        assertSame(member1, members.get(0));
        assertEquals(1, members.size());
    }

    @Test
    void testQuery() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findUser("AAA", 10);
        assertSame(member1, members.get(0));
        assertEquals(1, members.size());
    }

    @Test
    void findUsernameList() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();

        assertEquals(List.of("AAA", "BBB"), usernameList);
    }

    @Test
    void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member1 = new Member("AAA", 10);
        member1.setTeam(team);
        memberRepository.save(member1);

        List<MemberDto> memberDtoList = memberRepository.findMemberDto();

        assertEquals(1, memberDtoList.size());
        assertEquals(new MemberDto(member1.getId(), "AAA", "teamA"), memberDtoList.get(0));
    }

    @Test
    void findByNames() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        assertEquals(List.of(member1, member2), members);
    }

    @Test
    void returnType() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);


        /* 찾은 멤버 갯수가 리턴타입에 맞을 때 */
        List<Member> listAAA = memberRepository.findListByUsername("AAA");
        Member oneAAA = memberRepository.findMemberByUsername("AAA");
        Optional<Member> optionalAAA = memberRepository.findOptionalByUsername("AAA");

        assertEquals(List.of(member1), listAAA);
        assertEquals(member1, oneAAA);
        assertEquals(Optional.of(member1), optionalAAA);

        System.out.println(listAAA);
        System.out.println(oneAAA);
        System.out.println(optionalAAA);


        /* 멤버를 못 찾았을 때 */
        List<Member> listGhostMember = memberRepository.findListByUsername("ghost name");
        Member ghostMember = memberRepository.findMemberByUsername("ghost name");
        Optional<Member> optionalGhostMember = memberRepository.findOptionalByUsername("ghost name");

        assertEquals(List.of(), listGhostMember);
        assertNull(ghostMember);
        assertEquals(Optional.empty(), optionalGhostMember);

        System.out.println(listGhostMember);        // empty list
        System.out.println(ghostMember);            // null
        System.out.println(optionalGhostMember);    // Optional empty


        /* 찾은 멤버 갯수가 리포지토리 리턴타입과 맞지 않을 때 */
        Member member3 = new Member("AAA", 10); // AAA 이름을 갖는 멤버 추가
        memberRepository.save(member3);

        // AAA 이름을 갖는 멤버가 2명이다. 아래 메서드들은 단건을 반환하므로 실제 멤버 개수와 리턴타입이 맞지 않는다.
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> memberRepository.findMemberByUsername("AAA"));
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> memberRepository.findOptionalByUsername("AAA"));

    }



}
package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

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

    @Test
    void pagingWithPage() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));


        // Page //
        // 리턴 타입이 Page일 경우, 조건대로 member를 가져오는 쿼리, 총 member 수를 세는 쿼리, 총 2개의 쿼리가 나간다.
        // 반환타입에 따라서 총 요소 갯수를 세는 쿼리가 나갈 수도, 안 나갈 수도 있다.
        // Page 타입이 반환타입이라면 총 요소 갯수를 세는 쿼리도 같이 나간다.
        Page<Member> page = memberRepository.findPageByAge(age, pageRequest);

        assertEquals(3, page.getContent().size());  // 요청한 페이지 내용(getContent)의 크기
        assertEquals(5, page.getTotalElements());   // 총 요소 갯수
        assertEquals(0, page.getNumber());          // 현재 페이지
        assertEquals(2, page.getTotalPages());      // 총 페이지 갯수
        assertTrue(page.isFirst());                         // 첫 페이지인가?
        assertTrue(page.hasNext());                         // 다음 페이지 존재하는가?

        List<Member> content = page.getContent();
        content.forEach(System.out::println);


        // map을 이용한 변환
        Page<MemberDto> dtoPage = page.map(member -> new MemberDto(member.getId(), member.getUsername(), "abc"));
        dtoPage.getContent().forEach(System.out::println);

    }

    @Test
    void pagingWithSlice() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));


        // Slice //
        // 리턴 타입이 Slice일 경우, repository는 요소를 size + 1개 요청한다. 즉 3 + 1 = 4개를 요청한다.
        // Page와는 달리 총 요소 갯수를 세는 쿼리는 나가지 않는다.
        Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);

        assertEquals(3, slice.getContent().size()); // 요청한 slice 내용(getContent)의 크기
        //assertEquals(5, slice.getTotalElements());        // Slice에는 총 요소 갯수를 반환하는 메서드가 존재하지 않는다.
        assertEquals(0, slice.getNumber());         // 현재 페이지
        //assertEquals(2, slice.getTotalPages());           // slice에는 총 페이지 갯수를 반환하는 메서드가 존재하니 않는다.
        assertTrue(slice.isFirst());                        // 첫 페이지인가?
        assertTrue(slice.hasNext());                        // 다음 페이지 존재하는가?


        // map을 이용한 변환
        Slice<MemberDto> dtoSlice = slice.map(member -> new MemberDto(member.getId(), member.getUsername(), "abc"));
        dtoSlice.getContent().forEach(System.out::println);

    }

    @Test
    void pagingWithList() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));


        // List //
        // 단순하게 해당 조건에 부합하는 요소들만 리턴
        // limit 수를 늘리지도, 총 요소 갯수를 구하려하지도 않는다.
        List<Member> members = memberRepository.findListByAge(age, pageRequest);

        members.forEach(System.out::println);

    }

    @Test
    void pagingWithPageAndCustomQuery() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        //memberRepository.save(new Member("member6", 11));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));


        // query가 수동 지정한대로 생성된다. count query는 query가 지정한 left join을 적용하여 자동 생성된다. --> 최적화가 필요하다.
        // 그런데... age where 절이 적용 안되네...
        Page<Member> page = memberRepository.findPageWithCustomQueryByAge(age, pageRequest);

        assertEquals(3, page.getContent().size());  // 요청한 페이지 내용(getContent)의 크기
        assertEquals(5, page.getTotalElements());   // 총 요소 갯수
        assertEquals(0, page.getNumber());          // 현재 페이지
        assertEquals(2, page.getTotalPages());      // 총 페이지 갯수
        assertTrue(page.isFirst());                         // 첫 페이지인가?
        assertTrue(page.hasNext());                         // 다음 페이지 존재하는가?

        List<Member> content = page.getContent();
        content.forEach(System.out::println);

    }

    @Test
    void pagingWithPageAndCustomQueryCountQuery() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        //memberRepository.save(new Member("member6", 11));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));


        // query, count query가 수동 지정한대로 생성된다.
        // 그런데... age where 절이 적용 안되네...
        Page<Member> page = memberRepository.findPageWithCustomQueryCountQueryByAge(age, pageRequest);

        assertEquals(3, page.getContent().size());  // 요청한 페이지 내용(getContent)의 크기
        assertEquals(5, page.getTotalElements());   // 총 요소 갯수
        assertEquals(0, page.getNumber());          // 현재 페이지
        assertEquals(2, page.getTotalPages());      // 총 페이지 갯수
        assertTrue(page.isFirst());                         // 첫 페이지인가?
        assertTrue(page.hasNext());                         // 다음 페이지 존재하는가?

        List<Member> content = page.getContent();
        content.forEach(System.out::println);

    }


}
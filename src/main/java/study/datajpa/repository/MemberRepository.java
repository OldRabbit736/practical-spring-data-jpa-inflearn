package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 메소드 이름으로 쿼리 생성
    // 간단한 쿼리를 생성하는 경우라면 간간히 쓰인다.
    // 그러나 복잡한 쿼리를 만드려 하면, 이름이 매우 길어지기 때문에 그 때는 @Query 방식을 사용한다.
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // 메소드 이름으로 쿼리 생성
    List<Member> findTop3By();

    // 실무에서 NamedQuery는 거의 사용되지 않는다.
    // 엔티티에 query문이 기록되어 있다.
    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    // 실무에서 많이 쓰는 방법!
    // 메소드 이름에 제약이 없다. (즉 메소드 이름으로 쿼리 생성하는 경우처럼 이름을 복잡하게 짓지 않아도 된다.)
    // 애플리케이션이 로드 되는 시점에 쿼리의 유효성을 체크해 주기 때문에 오류를 찾아내기 용이하다는 장점이 있다!
    // 이름 없는 NamedQuery라고 할 수 있다.
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // QueryDSL에서는 더 편한 방법이 있다고 한다.
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // 쿼리에 in 절을 넣어준다.
    // 현업에서 많이 쓰인다고 한다.
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);


    /*
    스프링 데이터 JPA는 유연한 반환 타입 지원
    컬렉션, 단건, 단건 Optional 등등 지원
    https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types
    조회 결과가 많거나 없으면?
        - 컬렉션 (Collection<T>, List<T> 등)
            - 결과 없음: 빈 컬렉션 반환
        - 단건 조회 (T, Optional<T> 등)
            - 결과 없음: null 반환
            - 결과가 2건 이상: javax.persistence.NonUniqueResultException 예외 발생
                             Spring Data JPA는 해당 exception을
                             org.springframework.dao.IncorrectResultSizeDataAccessException
                             으로 변경해서 exception을 발생시킨다.
    아래 3개의 메서드는 같은 쿼리를 날린다. (find 다음 단어는 아무것이나 해도 상관없다. 즉 쿼리에 영향을 미치지 않는다.)
    단건 또는 0건이 예상될 때 T와 Optional<T> 중 어느 것이 나을까?
    Optional이 더 나은 선택이다. 왜냐하면 Optional이라는 타입 자체가 empty의 가능성을 명시적으로 표현하며
    empty checking을 강제하기 때문에 더욱 type-safe 하다고 볼 수 있기 때문이다.
     */
    List<Member> findListByUsername(String username);   // 컬렉션

    Member findMemberByUsername(String username);   // 단건

    Optional<Member> findOptionalByUsername(String username);   // 단건 Optional


    /*
    페이징을 위해 리턴 값을 Page, Slice 등으로 설정하고, Pageable 파라미터를 추가하자.
    예시: Page<Member> findPageByAge(int age, Pageable pageable);
    --> age로 1차로 filtering, 이후 결과를 pageable 조건으로 sorting, paging 기준 적용
    https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.special-parameters
    https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result

    스프링 데이터 JPA 페이징과 정렬
    - 페이징과 정렬 파라미터
        - org.springframework.data.domain.Sort: 정렬 기능
        - org.springframework.data.domain.Pageable: 페이지 기능 (내부에 Sort 포함)
    - 특별한 반환 타입
        - org.springframework.data.domain.Page : 추가 count 쿼리 결과를 포함하는 페이징
        - org.springframework.data.domain.Pageable : 추가 count 쿼리 없이 다음 페이지만 확인 가능 (내부적으로 limit + 1 조회)
        - List (자바 컬렉션) : 추가 count 쿼리 없이 결과만 반환

    - Page, Slice, List 스타일
        - Page 스타일 : 총 페이지 갯수, 총 요소 갯수 등을 포함하여 전체적인 페이지 구조를 보여줄 필요가 있을 때 사용
        - Slice 스타일 : 전체적인 느낌보다는 "더 보기" 등을 통해 필요할 때마다 다음 페이지를 요청하는 구조를 보여줄 필요가 있을 때 사용
        - List 스타일 : 단순히 해당 조건에 부합하는 요소들만 보여줄 필요가 있을 때 사용. Page나 Slice 인터페이스를 전혀 사용하지 않는다.

    - DTO 쉬운 변환 (Page, Slice의 map 인터페이스 사용) --> 외부 API에 대한 DTO 반환 시 유용
        - Page<Member> page = memberRepository.findByAge(10, pageRequest);
        - Page<MemberDto> dtoPage = page.map(m -> new MemberDto());
        - Page<DTO>, Slice<DTO> 등을 Controller에서 반환하면 json body에 Page, Slice 구조 그대로 적절히 변환되어 탑재된다.
    */
    // 리턴 타입이 Page일 경우, 2개의 쿼리 발생 : 조건에 맞는 Member page를 가져오는 쿼리 + 조건에 맞는 총 Member를 구하는 쿼리
    // 총 요소 갯수를 구하는 쿼리(카운트 쿼리) 때문에 데이터가 많은 경우 성능이 안 나오는 경우가 많다.
    // 따라서 카운트 쿼리를 잘 짜는 것이 중요한다.
    // 예를들어 Member와 Team의 엔티티가 있을 때, Member의 데이터를 가져오는 쿼리, Member의 카운트 쿼리 2개가 있다.
    // Member의 데이터를 가져오는 쿼리가 Member와 Team의 조인 시 left join을 쓴다고 한다면,
    // Member의 카운트 쿼리는 join을 사용할 필요가 없다. 그냥 Member의 카운트만 세면 되는 것이다. (team과 관련한 where 절이 없다는 가정하에)
    // 사용자는 원하는 카운트 쿼리를 직접 제공할 수 있다.
    Page<Member> findPageByAge(int age, Pageable pageable);

    // Member page를 가져오는 쿼리에 left outer join을 적용한다.
    // Member 카운트 쿼리에도 "자동으로" left outer join을 적용한다.
    // 쓸데없이 team을 조인하므로, 이 때는 수동으로 count 쿼리를 재 지정해 주면 된다. 바로 아래 메서드에서 계속...
    @Query(value = "select m from Member m left join m.team t")
    Page<Member> findPageWithCustomQueryByAge(int age, Pageable pageable);

    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")
    Page<Member> findPageWithCustomQueryCountQueryByAge(int age, Pageable pageable);

    // 리턴 타입이 Slice일 경우, 1개의 쿼리 발생 : 조건에 맞는 Member page를 가져오는 쿼리. 즉, 총 요소의 갯수를 구하는 쿼리를 발생시키지 않는다.
    // 단 page에 해당하는 요소를 가지고 올 때, limit + 1개의 요소를 가져오게 된다.
    // (모바일 페이지에서 "더 보기" 등을 클릭했을 때 보여주는 추가 컨텐츠 용도)
    Slice<Member> findSliceByAge(int age, Pageable pageable);

    // 딱히 페이지 인터페이스가 필요하지 않고, 해당 조건에 부합하는 페이지 요소들만 가져오고 싶을 때 List를 리턴 타입으로 사용하면 된다.
    List<Member> findListByAge(int age, Pageable pageable);

}

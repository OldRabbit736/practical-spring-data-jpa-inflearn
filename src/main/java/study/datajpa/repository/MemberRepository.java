package study.datajpa.repository;

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


    // 스프링 데이터 JPA는 유연한 반환 타입 지원
    // 컬렉션, 단건, 단건 Optional 등등 지원
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types
    // 조회 결과가 많거나 없으면?
    // - 컬렉션 (Collection<T>, List<T> 등)
    //      - 결과 없음: 빈 컬렉션 반환
    // - 단건 조회 (T, Optional<T> 등)
    //      - 결과 없음: null 반환
    //      - 결과가 2건 이상: javax.persistence.NonUniqueResultException 예외 발생
    //                       Spring Data JPA는 해당 exception을
    //                       org.springframework.dao.IncorrectResultSizeDataAccessException
    //                       으로 변경해서 exception을 발생시킨다.
    // 아래 3개의 메서드는 같은 쿼리를 날린다. (find 다음 단어는 아무것이나 해도 상관없다. 즉 쿼리에 영향을 미치지 않는다.)
    // 단건 또는 0건이 예상될 때 T와 Optional<T> 중 어느 것이 나을까?
    // Optional이 더 나은 선택이다. 왜냐하면 Optional이라는 타입 자체가 empty의 가능성을 명시적으로 표현하며
    // empty checking을 강제하기 때문에 더욱 type-safe 하다고 볼 수 있기 때문이다.
    List<Member> findListByUsername(String username);   // 컬렉션
    Member findMemberByUsername(String username);   // 단건
    Optional<Member> findOptionalByUsername(String username);   // 단건 Optional
}

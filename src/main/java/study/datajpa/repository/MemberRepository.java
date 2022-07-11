package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.Collection;
import java.util.List;

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
}

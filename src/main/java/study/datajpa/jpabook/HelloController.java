//package study.datajpa.jpabook;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.stereotype.Repository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//@Controller
//public class HelloController {
//
//    @Autowired HelloService helloService;
//
//    public void hello() {
//        // 반환된 member 엔티티는 준영속 상태이다 --- 4
//        MyMember member = helloService.logic();
//    }
//}
//
//@Service
//class HelloService {
//
//    @PersistenceContext // 엔티티 매니저 주입
//    EntityManager em;
//
//    @Autowired
//    Repository1 repository1;
//
//    @Autowired
//    Repository2 repository2;
//
//    // 트랜잭션 시작 --- 1
//    @Transactional
//    public MyMember logic() {
//
//        repository1.hello();
//
//        // member는 영속 상태다. --- 2
//        MyMember member = repository2.findMember();
//
//        return member;
//    }
//    // 트랜잭션 종료 --- 3
//}
//
//@Repository
//class Repository1 {
//
//    @PersistenceContext
//    EntityManager em;
//
//    public void hello() {
//        //em.xxx(); // A. 영속성 콘텍스트 접근
//    }
//}
//
//@Repository
//class Repository2 {
//
//    @PersistenceContext
//    EntityManager em;
//
//    public MyMember findMember() {
//        return em.find(MyMember.class, "id1");  // B. 영속성 컨텍스트 접근
//    }
//}
//
//class MyMember {}
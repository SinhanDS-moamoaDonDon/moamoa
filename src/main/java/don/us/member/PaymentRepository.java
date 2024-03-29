package don.us.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer>{

	Optional<PaymentEntity> findByMembernoAndAccount(int member_no, String account);
	
	List<PaymentEntity> findByMemberno(int member_no);
	PaymentEntity findByNo(int no);
	
	@Modifying
	@Query("DELETE FROM PaymentEntity WHERE no = ?1 AND memberno = ?2")
	int deletePay(int no, int member_no);
}

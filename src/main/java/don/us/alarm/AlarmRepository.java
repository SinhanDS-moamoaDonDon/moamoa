package don.us.alarm;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<AlarmEntity, Integer>{
	List<AlarmEntity> findByMembernoOrderByAlarmdateDesc(int member_no);
}

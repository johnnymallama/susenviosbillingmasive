package co.com.surenvios.billingmasive.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.com.surenvios.librarycommon.database.entity.Ejecucion;

@Transactional
@Repository("ejecucionRepository")
public interface EjecucionRepository extends CrudRepository<Ejecucion, Integer> {
	
	@Query(nativeQuery = true, value = "SELECT count(1) FROM DBO.FE_EJECUCION fe where fe.fecha_fin IS NULL")
	public Integer findThreadExecuting();

}

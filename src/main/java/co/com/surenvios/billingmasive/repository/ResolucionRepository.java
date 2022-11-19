package co.com.surenvios.billingmasive.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.com.surenvios.librarycommon.database.entity.Resolucion;

@Transactional
@Repository("resolucionRepository")
public interface ResolucionRepository extends CrudRepository<Resolucion, String> {

	@Query("SELECT a FROM Resolucion a WHERE a.estado = 1")
	public Resolucion findResolucionActive();

	@Procedure(name = "Resolucion.ProximaConsecutivoResolucion")
	public Integer findProximoConsecutivo(@Param("pi_Numero_Resolucion") String numeroResolucion);

	@Query(nativeQuery = true, value = "SELECT a.* FROM FE_RESOLUCION a WHERE a.prefijo = SUBSTRING(?1,0,LEN(a.prefijo)+1) and CAST(SUBSTRING(?1,LEN(a.prefijo) + 1 ,LEN(?1)) as Integer) BETWEEN a.consecutivo_inicial and a.consecutivo_final AND a.estado = 1")
	public Resolucion findResolucionNumber(String numberDocument);

}

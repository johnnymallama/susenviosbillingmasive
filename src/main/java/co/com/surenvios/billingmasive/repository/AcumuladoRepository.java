package co.com.surenvios.billingmasive.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.com.surenvios.librarycommon.database.entity.Acumulado;

@Transactional
@Repository("acumuladoRepository")
public interface AcumuladoRepository extends CrudRepository<Acumulado, Integer> {
	
	@Query("SELECT a FROM Acumulado a WHERE a.procesar = 0 AND (a.numeroDocumento IS NULL OR a.numeroDocumento = '')")
	public List<Acumulado> findDocumentoProcess();
	
	@Query("SELECT a FROM Acumulado a WHERE a.procesar = 0 AND a.estadoDocumento = 3 AND a.numeroDocumento IS NOT NULL")
	public List<Acumulado> findDocumentoReprocess();

}

package co.com.surenvios.billingmasive.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.com.surenvios.librarycommon.database.entity.NumeracionNcNd;

@Transactional
@Repository("numeracionNcNdRepository")
public interface NumeracionNcNdRepository extends CrudRepository<NumeracionNcNd, String> {

	@Query("SELECT a FROM NumeracionNcNd a WHERE a.estado = 1")
	public List<NumeracionNcNd> findAllActive();

	@Procedure(name = "NumeracionNcNd.ProximaConsecutivoNumeracion")
	public Integer findProximoConsecutivo(@Param("pi_Tipo_Documento") String tipoDocumento, @Param("pi_Origen") String origen);

	@Query(nativeQuery = true, value = "SELECT a.* FROM FE_NUMERACION_NC_ND a WHERE a.prefijo = SUBSTRING(?1,0,LEN(a.prefijo)+1) AND a.Origen = ?2")
	public NumeracionNcNd findNumeracionNcNdNumberDocument(String numberDocument, String origen);

	@Query("SELECT a FROM NumeracionNcNd a WHERE a.estado = 1 AND a.origen = ?1")
	public List<NumeracionNcNd> findAllActiveByOrigen(String origen);

}

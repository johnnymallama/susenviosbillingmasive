package co.com.surenvios.billingmasive.repository;

import co.com.surenvios.librarycommon.database.entity.IdT040009;
import co.com.surenvios.librarycommon.database.entity.T040009;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;

@Transactional
@Repository("t0400009Repository")
public interface T0400009Repository extends CrudRepository<T040009, IdT040009> {

    @Modifying
    @Query(value = "UPDATE T0400009 set Cod_Regional_FExterna = ?1, Cons_FExterna = ?2, Fec_FExterna = ?3, Pref_Factura = ?4 where Transportadora = ?5 and Nguia = ?6 and Otro = ?7", nativeQuery = true)
    void updateT04009 (int codRegionalFExterna, int consFExterna, Date fecFExterna, String prefFactura, String transportadora, String numeroGuia, int otro);

}

package co.com.surenvios.billingmasive.repository;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.com.surenvios.librarycommon.database.entity.AcumuladoEstado;

@Transactional
@Repository("acumuladoEstadoRepository")
public interface AcumuladoEstadoRepository extends CrudRepository<AcumuladoEstado, String> {

}

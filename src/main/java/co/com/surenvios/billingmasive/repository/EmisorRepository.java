package co.com.surenvios.billingmasive.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.com.surenvios.librarycommon.database.view.Emisor;


@Repository("emisorRepository")
public interface EmisorRepository extends CrudRepository<Emisor, String> {

    @Query("SELECT a FROM Emisor a WHERE a.origen = ?1")
    public Emisor findEmisorByOrigen(String origen);

}

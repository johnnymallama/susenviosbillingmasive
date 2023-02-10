package co.com.surenvios.billingmasive.repository;

import co.com.surenvios.librarycommon.database.view.DataNote;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("dataNoteRepository")
public interface DataNoteRepository extends CrudRepository<DataNote, String> {

    @Query("SELECT a FROM DataNote a WHERE a.identificador = ?1")
    public DataNote findDataNoteByIdentity(Integer identity);

}

package job.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;

import java.util.List;

@ApplicationScoped
public class ControlloDAO {
    @Inject
    Jdbi jdbi;
    public String countMySql(){
        String select="SELECT count(*)  FROM wtpmov00f " ;
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(select);
            return query.mapTo(String.class);
        }).toString();
    }

    public String countAS400(){
        String select="SELECT count(*)  FROM wtpmov00f " ;
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(select);
            return query.mapTo(String.class);
        }).toString();
    }
}

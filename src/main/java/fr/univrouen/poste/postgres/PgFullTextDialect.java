package fr.univrouen.poste.postgres;

import org.hibernate.dialect.PostgreSQL9Dialect;

public class PgFullTextDialect extends PostgreSQL9Dialect {

    public PgFullTextDialect() {
        registerFunction("fts", new PgFullTextFunction());
        registerFunction("ts_rank", new PgFullTextRankFunction());
    }
    
}

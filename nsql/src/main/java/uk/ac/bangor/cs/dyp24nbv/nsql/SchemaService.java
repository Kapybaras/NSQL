package uk.ac.bangor.cs.dyp24nbv.nsql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    private final JdbcTemplate jdbcTemplate;
    private String cachedSchema;
    @Autowired
    public SchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String generateSchemaPrompt() {
    	if (cachedSchema != null) return cachedSchema;
    	
        StringBuilder schema = new StringBuilder();

        // 1. Get all tables in the current database
        List<String> tables = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM information_schema.tables WHERE table_schema = DATABASE()", 
            String.class
        );

        for (String tableName : tables) {
            schema.append("Table: ").append(tableName).append("\nColumns: ");

            // 2. Get columns and types for each table
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, DATA_TYPE FROM information_schema.columns " +
                "WHERE table_name = ? AND table_schema = DATABASE()", 
                tableName
            );

            String columnDetails = columns.stream()
                .map(col -> col.get("COLUMN_NAME") + " (" + col.get("DATA_TYPE") + ")")
                .collect(Collectors.joining(", "));

            schema.append(columnDetails).append("\n\n");
        }

        this.cachedSchema = schema.toString();
        return this.cachedSchema;
    }
}
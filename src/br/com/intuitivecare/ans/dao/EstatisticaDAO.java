package br.com.intuitivecare.ans.dao;

import br.com.intuitivecare.ans.util.DatabaseConnection;
import java.sql.*;

public class EstatisticaDAO {

    public int buscarOperadorasAcimaDaMedia() {
        String sql = "WITH media_geral AS (SELECT AVG(valor_despesa) as m FROM despesas) " +
                     "SELECT COUNT(*) as total FROM (" +
                     "SELECT registro_ans FROM despesas, media_geral " +
                     "WHERE valor_despesa > media_geral.m " +
                     "GROUP BY registro_ans HAVING COUNT(DISTINCT trimestre) >= 2) as sub";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao consultar banco: " + e.getMessage());
        }
        return 0;
    }
}

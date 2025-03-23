package org.proj3.currency_exchange.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.proj3.currency_exchange.util.PropertiesUtil;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final String URL_KEY = "db.url";
    private static final String MAX_POOL_SIZE_KEY = "db.MaximumPoolSize";
    private static final String IDLE_TIME_OUT_KEY = "db.IdleTimeout";
    private static final String POOL_NAME_KEY = "db.PoolName";

    public static DataSource getDataSource() {
        SQLiteDataSource sqliteDataSource = new SQLiteDataSource();
        sqliteDataSource.setUrl(PropertiesUtil.get(URL_KEY));

        HikariConfig config = new HikariConfig();
        config.setDataSource(sqliteDataSource);

        config.setMaximumPoolSize(Integer.parseInt(PropertiesUtil.get(MAX_POOL_SIZE_KEY)));
        config.setIdleTimeout(Integer.parseInt(PropertiesUtil.get(IDLE_TIME_OUT_KEY)));
        config.setPoolName(PropertiesUtil.get(POOL_NAME_KEY));

        return new HikariDataSource(config);
    }
}

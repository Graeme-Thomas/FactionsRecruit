# Database Connection Troubleshooting Guide

## Fixed Issues

### ✅ Character Encoding Error (utf8mb4)
**Error**: `Unsupported character encoding 'utf8mb4'`

**Cause**: MySQL Connector/J 8.x and 9.x changed how character encoding is handled. The old `characterEncoding` property is no longer supported.

**Solution**: The plugin now automatically adds proper connection parameters to the JDBC URL:
- `?useSSL=false` - Disables SSL (enable for production)
- `&allowPublicKeyRetrieval=true` - Required for MySQL 8.x authentication
- `&serverTimezone=UTC` - Required for MySQL 8.x timezone handling

These parameters are **automatically added** when using individual host/port/database parameters.

---

## Configuration Methods

### Method 1: Individual Parameters (Recommended for most users)

In `config.yml`:
```yaml
database:
  connection-string: ""  # Leave empty
  host: "localhost"
  port: 3306
  database: "factions_recruitment"
  username: "your_username"
  password: "your_password"
```

**The plugin automatically builds a safe JDBC URL with all required parameters.**

### Method 2: Connection String (Advanced users)

In `config.yml`:
```yaml
database:
  connection-string: "jdbc:mysql://localhost:3306/factions_recruitment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  username: "your_username"
  password: "your_password"
```

**Use this if you need custom connection parameters or special configurations.**

---

## Common Connection Issues

### Issue 1: "Access denied for user"
**Symptoms**: `Access denied for user 'username'@'host'`

**Solutions**:
1. Verify username and password in config.yml
2. Check MySQL user permissions:
   ```sql
   GRANT ALL PRIVILEGES ON factions_recruitment.* TO 'username'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. If connecting remotely, ensure user has remote access:
   ```sql
   GRANT ALL PRIVILEGES ON factions_recruitment.* TO 'username'@'%';
   ```

### Issue 2: "Communications link failure"
**Symptoms**: `Communications link failure`, `Connection refused`

**Solutions**:
1. Verify MySQL server is running:
   ```bash
   systemctl status mysql
   # or
   service mysql status
   ```
2. Check MySQL port (default 3306) is open:
   ```bash
   netstat -tlnp | grep 3306
   ```
3. If using firewall, allow MySQL port:
   ```bash
   ufw allow 3306/tcp
   # or
   firewall-cmd --add-port=3306/tcp --permanent
   ```
4. Verify MySQL bind address in `/etc/mysql/my.cnf`:
   ```ini
   bind-address = 0.0.0.0  # Allow remote connections
   ```

### Issue 3: "Unknown database"
**Symptoms**: `Unknown database 'factions_recruitment'`

**Solution**: Create the database:
```sql
CREATE DATABASE factions_recruitment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Issue 4: "Public Key Retrieval is not allowed"
**Symptoms**: `Public Key Retrieval is not allowed`

**Solutions**:
1. **Method 1** (Automatic): Use individual parameters - plugin adds `allowPublicKeyRetrieval=true` automatically
2. **Method 2** (Manual): Add to connection string:
   ```yaml
   connection-string: "jdbc:mysql://host:port/db?allowPublicKeyRetrieval=true"
   ```
3. **Method 3** (MySQL config): Disable caching_sha2_password:
   ```sql
   ALTER USER 'username'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
   ```

### Issue 5: "The server time zone value is unrecognized"
**Symptoms**: `The server time zone value ... is unrecognized or represents more than one time zone`

**Solution**: Automatically handled - plugin adds `serverTimezone=UTC` to JDBC URL.

If you need a specific timezone:
```yaml
connection-string: "jdbc:mysql://host:port/db?serverTimezone=America/New_York"
```

### Issue 6: SSL Connection Errors
**Symptoms**: `SSL connection error`, `unable to find valid certification path`

**Solutions**:
1. **Disable SSL** (for local/trusted networks):
   ```yaml
   connection-string: "jdbc:mysql://host:port/db?useSSL=false"
   ```
   This is the default behavior.

2. **Enable SSL** (for production/remote):
   ```yaml
   connection-string: "jdbc:mysql://host:port/db?useSSL=true&requireSSL=true&verifyServerCertificate=true"
   ```

---

## Remote Database Configuration

### For Remote MySQL (e.g., PebbleHost, Aternos, etc.)

**Format**:
```yaml
database:
  connection-string: "jdbc:mysql://remote.host.com:3306/database_name?useSSL=true&serverTimezone=UTC"
  username: "remote_username"
  password: "remote_password"
```

**Example (PebbleHost-style)**:
```yaml
database:
  connection-string: "jdbc:mysql://na03-sql.pebblehost.com:3306/customer_1234567_database?useSSL=false&allowPublicKeyRetrieval=true"
  username: "customer_1234567_user"
  password: "your_password_here"
```

**Important Notes**:
- Always use `useSSL=false` for shared hosting unless explicitly required
- Add `allowPublicKeyRetrieval=true` for MySQL 8.x hosts
- Verify the exact hostname and port from your hosting provider

---

## Testing Database Connection

### From Server Console
When plugin loads, check console for:
```
[FactionsRecruit] Initializing database connection pool...
[FactionsRecruit] Using individual parameters for database configuration
[FactionsRecruit] Database connection configured: username@host:port/database
[FactionsRecruit] Database connection pool established successfully
[FactionsRecruit] Database initialization completed
```

### Manual Test Query
Create test file `test-connection.sql`:
```sql
SELECT 1;
```

Run from command line:
```bash
mysql -h hostname -P 3306 -u username -p database_name < test-connection.sql
```

If this works, your credentials are correct.

---

## Connection String Parameters Reference

### Essential Parameters
```
useSSL=false                    # Disable SSL (use true for production)
allowPublicKeyRetrieval=true    # Required for MySQL 8.x
serverTimezone=UTC              # Required for MySQL 8.x
```

### Performance Parameters (in config.yml properties section)
```yaml
properties:
  cachePrepStmts: true                # Cache prepared statements
  prepStmtCacheSize: 250              # Cache size
  prepStmtCacheSqlLimit: 2048         # SQL length limit
  useServerPrepStmts: true            # Use server-side prep statements
  rewriteBatchedStatements: true      # Optimize batch inserts
```

### Security Parameters
```
useSSL=true                     # Enable SSL/TLS
requireSSL=true                 # Require SSL/TLS
verifyServerCertificate=true    # Verify SSL certificate
```

### Charset Parameters (rarely needed)
```
connectionCollation=utf8mb4_unicode_ci    # Modern charset
```

---

## HikariCP Connection Pool Settings

### Understanding Pool Settings

**maximum-pool-size: 10**
- Maximum number of connections in pool
- Higher = more concurrent queries, more memory
- Recommended: 10-20 for most servers

**minimum-idle: 2**
- Minimum idle connections to maintain
- Lower = less memory, more connection overhead
- Recommended: 2-5

**max-lifetime: 1800000** (30 minutes)
- Maximum lifetime of connection
- Prevents stale connections
- Recommended: 1800000 (30 min) to 3600000 (1 hour)

**connection-timeout: 30000** (30 seconds)
- Timeout when getting connection from pool
- Increase for slow networks
- Recommended: 30000 (30 sec)

**idle-timeout: 600000** (10 minutes)
- Timeout for idle connections
- Connections idle longer are closed
- Recommended: 600000 (10 min)

**leak-detection-threshold: 60000** (1 minute)
- Detects connection leaks
- 0 = disabled
- Recommended: 60000 (1 min) for debugging, 0 for production

### Tuning for Different Server Sizes

**Small Server (1-20 players)**:
```yaml
pool:
  maximum-pool-size: 5
  minimum-idle: 1
  connection-timeout: 30000
```

**Medium Server (20-100 players)**:
```yaml
pool:
  maximum-pool-size: 10
  minimum-idle: 2
  connection-timeout: 30000
```

**Large Server (100+ players)**:
```yaml
pool:
  maximum-pool-size: 20
  minimum-idle: 5
  connection-timeout: 30000
```

---

## Debug Mode

### Enable Debug Logging

In `config.yml`:
```yaml
debug:
  database: true
  queries: true
```

Then check `logs/latest.log` for detailed connection information.

### Check MySQL Logs

MySQL error log location:
- Ubuntu/Debian: `/var/log/mysql/error.log`
- CentOS/RHEL: `/var/log/mysqld.log`
- Windows: `C:\ProgramData\MySQL\MySQL Server 8.0\Data\*.err`

Check for connection attempts:
```bash
tail -f /var/log/mysql/error.log | grep "Access denied\|Connection"
```

---

## Still Having Issues?

### Collect Diagnostics

1. **Plugin version**: Check `plugins/FactionsRecruit/` for version
2. **MySQL version**: Run `mysql --version` or `SELECT VERSION();`
3. **Java version**: Run `java -version`
4. **Full error log**: Copy from `logs/latest.log`
5. **Config (sanitized)**: Copy config.yml (remove passwords!)

### Quick Checklist

- [ ] MySQL server is running
- [ ] Database exists
- [ ] User has permissions on database
- [ ] Username and password are correct
- [ ] Host and port are correct
- [ ] Firewall allows MySQL port
- [ ] MySQL allows remote connections (if needed)
- [ ] Connection string has proper parameters
- [ ] No syntax errors in config.yml

---

## Summary of Changes

### What Was Fixed
✅ Removed incompatible `characterEncoding: utf8mb4` property
✅ Added automatic connection parameters for MySQL 8.x/9.x
✅ Connection string format properly documented
✅ Backward compatibility maintained for older configs

### Recommended Configuration
```yaml
database:
  connection-string: ""  # Leave empty for automatic configuration
  host: "localhost"
  port: 3306
  database: "factions_recruitment"
  username: "minecraft"
  password: "your_secure_password"
```

The plugin will automatically build:
```
jdbc:mysql://localhost:3306/factions_recruitment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

This configuration works with:
- MySQL 5.7+
- MySQL 8.x
- MariaDB 10.x+
- MySQL Connector/J 8.x and 9.x
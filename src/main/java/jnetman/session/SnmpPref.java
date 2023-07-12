package jnetman.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/***
 * Snmp协议相关配置参数
 */
@ConfigurationProperties(prefix = "snmp.config")
@Component
public class SnmpPref {

    public static String v3User;
    
    public static String password;
    
    public static String privacyDES;
    
    public static int port;
    
    public static int trapsPort;
    
    public static int timeout;
    
    public static int maxRetries;
    
    public static boolean isSnmp4JLogEnabled;


    @Value("${snmp.config.v3User}")
    public void setV3User(String v3User) {
        SnmpPref.v3User = v3User;
    }

    @Value("${snmp.config.password}")
    public void setPassword(String password) {
        SnmpPref.password = password;
    }

    @Value("${snmp.config.privacyDES}")
    public void setPrivacyDES(String privacyDES) {
        SnmpPref.privacyDES = privacyDES;
    }

    @Value("${snmp.config.port}")
    public void setPort(int port) {
        SnmpPref.port = port;
    }

    @Value("${snmp.config.trapsPort}")
    public void setTrapsPort(int trapsPort) {
        SnmpPref.trapsPort = trapsPort;
    }

    @Value("${snmp.config.timeout}")
    public void setTimeout(int timeout) {
        SnmpPref.timeout = timeout;
    }

    @Value("${snmp.config.maxRetries}")
    public void setMaxRetries(int maxRetries) {
        SnmpPref.maxRetries = maxRetries;
    }

    @Value("${snmp.config.isSnmp4JLogEnabled}")
    public void setIsSnmp4JLogEnabled(boolean isSnmp4JLogEnabled) {
        SnmpPref.isSnmp4JLogEnabled = isSnmp4JLogEnabled;
    }

    public static String getUser() {
        return v3User;
    }

    public static String getPassword() {
        return password;
    }

    public static String getPrivacyDES()
    {
        return privacyDES;
    }

    public static int getPort() {
        return port;
    }

    public static int getTrapsPort() {
        return trapsPort;
    }

    public static int getTimeout() {
        return timeout;
    }

    public static int getMaxRetries() {
        return maxRetries;
    }

    public static boolean isSnmp4jLogEnabled() {
        return isSnmp4JLogEnabled;
    }
}

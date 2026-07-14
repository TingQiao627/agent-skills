package com.security.service;

import com.security.entity.LoginLog;
import com.security.repository.LoginLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全监控服务
 * 实现异地登录检测、IP归属地查询、登录日志记录
 */
@Service
public class SecurityMonitorService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityMonitorService.class);
    
    // 用于判断异地登录的距离阈值（公里）
    private static final int DISTANCE_THRESHOLD = 500;
    
    // IP归属地查询API（可配置）
    private static final String IP_LOCATION_API = "http://ip-api.com/json/";
    
    @Autowired
    private LoginLogRepository loginLogRepository;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    /**
     * 记录登录日志
     * @param userId 用户ID
     * @param ip IP地址
     * @param device 设备信息
     * @param status 登录状态
     * @param location 归属地（可选）
     * @return 登录日志实体
     */
    public LoginLog recordLogin(Long userId, String ip, String device, String status, String location) {
        LoginLog loginLog = new LoginLog(userId, ip, status);
        loginLog.setDevice(device);
        loginLog.setLocation(location != null ? location : getLocationByIp(ip));
        loginLog.setLoginTime(LocalDateTime.now());
        
        return loginLogRepository.save(loginLog);
    }
    
    /**
     * 记录登录成功日志
     */
    public LoginLog recordSuccessLogin(Long userId, String ip, String device) {
        return recordLogin(userId, ip, device, "SUCCESS", null);
    }
    
    /**
     * 记录登录失败日志
     */
    public LoginLog recordFailedLogin(Long userId, String ip, String device, String reason) {
        LoginLog loginLog = recordLogin(userId, ip, device, "FAILED", null);
        loginLog.setFailureReason(reason);
        return loginLogRepository.save(loginLog);
    }
    
    /**
     * 记录账户锁定日志
     */
    public LoginLog recordLockedLogin(Long userId, String ip) {
        return recordLogin(userId, ip, null, "LOCKED", null);
    }
    
    /**
     * 检测是否为异地登录
     * @param userId 用户ID
     * @param currentIp 当前登录IP
     * @return 异地登录检测结果
     */
    public AnomalyLoginResult checkAnomalyLogin(Long userId, String currentIp) {
        // 获取最近一次成功登录记录
        LoginLog lastLogin = loginLogRepository.findLatestSuccessLogin(userId);
        
        if (lastLogin == null) {
            // 首次登录，不判断异地
            return new AnomalyLoginResult(false, null, null, 0);
        }
        
        // 获取当前IP归属地
        LocationInfo currentLocation = getLocationInfo(currentIp);
        
        // 获取上次登录IP归属地
        LocationInfo lastLocation = getLocationInfo(lastLogin.getIp());
        
        if (currentLocation == null || lastLocation == null) {
            return new AnomalyLoginResult(false, lastLogin.getLocation(), null, 0);
        }
        
        // 计算距离
        double distance = calculateDistance(
            lastLocation.getLatitude(), lastLocation.getLongitude(),
            currentLocation.getLatitude(), currentLocation.getLongitude()
        );
        
        boolean isAnomaly = distance > DISTANCE_THRESHOLD;
        
        // 如果是异地登录，触发告警
        if (isAnomaly) {
            triggerAnomalyAlert(userId, lastLogin, currentIp, currentLocation, distance);
        }
        
        return new AnomalyLoginResult(
            isAnomaly, 
            lastLogin.getLocation(), 
            currentLocation.getCity() + ", " + currentLocation.getCountry(),
            distance
        );
    }
    
    /**
     * 查询IP归属地
     * @param ip IP地址
     * @return 归属地字符串
     */
    public String getLocationByIp(String ip) {
        LocationInfo locationInfo = getLocationInfo(ip);
        if (locationInfo != null) {
            return locationInfo.getCity() + ", " + locationInfo.getCountry();
        }
        return "Unknown";
    }
    
    /**
     * 获取IP详细信息
     */
    private LocationInfo getLocationInfo(String ip) {
        // 跳过本地IP
        if (isLocalIp(ip)) {
            return new LocationInfo("Local", "Local", 0, 0);
        }
        
        try {
            if (restTemplate != null) {
                String url = IP_LOCATION_API + ip;
                Map<String, Object> response = restTemplate.getForObject(url, HashMap.class);
                
                if (response != null && "success".equals(response.get("status"))) {
                    String city = (String) response.get("city");
                    String country = (String) response.get("country");
                    Double lat = (Double) response.get("lat");
                    Double lon = (Double) response.get("lon");
                    
                    return new LocationInfo(
                        city != null ? city : "Unknown",
                        country != null ? country : "Unknown",
                        lat != null ? lat : 0,
                        lon != null ? lon : 0
                    );
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get location for IP: {}, error: {}", ip, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 判断是否为本地IP
     */
    private boolean isLocalIp(String ip) {
        return ip == null || ip.startsWith("127.") || ip.startsWith("192.168.") 
            || ip.startsWith("10.") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }
    
    /**
     * 计算两个坐标之间的距离（公里）
     * 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // 地球半径（公里）
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return earthRadius * c;
    }
    
    /**
     * 触发异地登录告警
     * 可扩展：发送邮件、短信、推送通知等
     */
    private void triggerAnomalyAlert(Long userId, LoginLog lastLogin, String currentIp, 
                                     LocationInfo currentLocation, double distance) {
        logger.warn("Anomaly login detected for user {}. Last login: {} ({}), Current: {} ({}). Distance: {} km",
            userId, lastLogin.getIp(), lastLogin.getLocation(), 
            currentIp, currentLocation.getCity(), String.format("%.0f", distance));
        
        // TODO: 集成邮件通知服务
        // emailService.sendAnomalyAlert(userId, lastLogin, currentIp, currentLocation);
        
        // TODO: 集成短信通知服务
        // smsService.sendAnomalyAlert(userId);
    }
    
    /**
     * 获取用户登录历史
     */
    public List<LoginLog> getLoginHistory(Long userId) {
        return loginLogRepository.findByUserIdOrderByLoginTimeDesc(userId);
    }
    
    /**
     * 获取用户最近的登录记录
     */
    public List<LoginLog> getRecentLogins(Long userId) {
        return loginLogRepository.findTop10ByUserIdOrderByLoginTimeDesc(userId);
    }
    
    /**
     * 位置信息内部类
     */
    private static class LocationInfo {
        private final String city;
        private final String country;
        private final double latitude;
        private final double longitude;
        
        public LocationInfo(String city, String country, double latitude, double longitude) {
            this.city = city;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public String getCity() { return city; }
        public String getCountry() { return country; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
    
    /**
     * 异地登录检测结果
     */
    public static class AnomalyLoginResult {
        private final boolean isAnomaly;
        private final String lastLocation;
        private final String currentLocation;
        private final double distanceKm;
        
        public AnomalyLoginResult(boolean isAnomaly, String lastLocation, 
                                  String currentLocation, double distanceKm) {
            this.isAnomaly = isAnomaly;
            this.lastLocation = lastLocation;
            this.currentLocation = currentLocation;
            this.distanceKm = distanceKm;
        }
        
        public boolean isAnomaly() { return isAnomaly; }
        public String getLastLocation() { return lastLocation; }
        public String getCurrentLocation() { return currentLocation; }
        public double getDistanceKm() { return distanceKm; }
    }
}
package com.springboot.Job.util;

import com.springboot.Job.model.AdminLoginBean;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

public class SecurityUtil {
    
    public static final String USER_SESSION = "user";
    public static final String USER_ID_SESSION = "userId";
    public static final String OWNER_SESSION = "owner";
    public static final String OWNER_ID_SESSION = "ownerId";
    public static final String ADMIN_SESSION = "loginadm";
    public static final String ADMIN_NAME_SESSION = "adminName";
    public static final String ROLE_CONFLICT_FLAG = "roleConflictDetected";
    
    // Check if user is logged in
    public static boolean isUserLoggedIn(HttpSession session) {
        return session.getAttribute(USER_ID_SESSION) != null;
    }
    
    // Check if owner is logged in
    public static boolean isOwnerLoggedIn(HttpSession session) {
        return session.getAttribute(OWNER_ID_SESSION) != null;
    }
    
    // Check if admin is logged in
    public static boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute(ADMIN_SESSION) != null;
    }
    
    // Get current user ID
    public static Optional<Integer> getCurrentUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(USER_ID_SESSION);
        return Optional.ofNullable(userId);
    }
    
    // Get current owner ID
    public static Optional<Integer> getCurrentOwnerId(HttpSession session) {
        Integer ownerId = (Integer) session.getAttribute(OWNER_ID_SESSION);
        return Optional.ofNullable(ownerId);
    }
    
    // Get current admin
    public static Optional<AdminLoginBean> getCurrentAdmin(HttpSession session) {
        AdminLoginBean admin = (AdminLoginBean) session.getAttribute(ADMIN_SESSION);
        return Optional.ofNullable(admin);
    }
    
    // Check if ONLY user is logged in (no other roles)
    public static boolean isOnlyUserLoggedIn(HttpSession session) {
        return isUserLoggedIn(session) && !isOwnerLoggedIn(session) && !isAdminLoggedIn(session);
    }
    
    // Check if ONLY owner is logged in (no other roles)
    public static boolean isOnlyOwnerLoggedIn(HttpSession session) {
        return isOwnerLoggedIn(session) && !isUserLoggedIn(session) && !isAdminLoggedIn(session);
    }
    
    // Check if ONLY admin is logged in (no other roles)
    public static boolean isOnlyAdminLoggedIn(HttpSession session) {
        return isAdminLoggedIn(session) && !isUserLoggedIn(session) && !isOwnerLoggedIn(session);
    }
    
    // Validate user access to user endpoints
    public static boolean hasUserAccess(HttpSession session) {
        return isOnlyUserLoggedIn(session);
    }
    
    // Validate owner access to owner endpoints
    public static boolean hasOwnerAccess(HttpSession session) {
        return isOnlyOwnerLoggedIn(session);
    }
    
    // Validate admin access to admin endpoints
    public static boolean hasAdminAccess(HttpSession session) {
        return isOnlyAdminLoggedIn(session);
    }
    
    // Detect mixed roles
    public static boolean hasMixedRoles(HttpSession session) {
        int activeRoles = 0;
        if (isUserLoggedIn(session)) activeRoles++;
        if (isOwnerLoggedIn(session)) activeRoles++;
        if (isAdminLoggedIn(session)) activeRoles++;
        return activeRoles > 1;
    }
    
    // NEW: Set role conflict flag
    public static void setRoleConflict(HttpSession session, boolean conflict) {
        session.setAttribute(ROLE_CONFLICT_FLAG, conflict);
    }
    
    // NEW: Check if role conflict was detected
    public static boolean hasRoleConflict(HttpSession session) {
        return session.getAttribute(ROLE_CONFLICT_FLAG) != null && 
               (Boolean) session.getAttribute(ROLE_CONFLICT_FLAG);
    }
    
    // Clear all sessions (logout from all roles)
    public static void clearAllSessions(HttpSession session) {
        session.removeAttribute(USER_SESSION);
        session.removeAttribute(USER_ID_SESSION);
        session.removeAttribute(OWNER_SESSION);
        session.removeAttribute(OWNER_ID_SESSION);
        session.removeAttribute(ADMIN_SESSION);
        session.removeAttribute(ADMIN_NAME_SESSION);
        session.removeAttribute(ROLE_CONFLICT_FLAG); // Clear conflict flag too
    }
}
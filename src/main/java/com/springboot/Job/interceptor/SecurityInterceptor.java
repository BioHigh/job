package com.springboot.Job.interceptor;

import com.springboot.Job.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
	        throws Exception {
	    
	    HttpSession session = request.getSession();
	    String requestURI = request.getRequestURI();
	    
	    System.out.println("Security Interceptor checking: " + requestURI);
	    
	    // NEW: First check if there was a previous role conflict
	    if (SecurityUtil.hasRoleConflict(session)) {
	        System.out.println("Previous role conflict detected - forcing logout");
	        SecurityUtil.clearAllSessions(session);
	        response.sendRedirect("/index?error=role_conflict");
	        return false;
	    }
	    
	    // Check for mixed roles
	    if (SecurityUtil.hasMixedRoles(session)) {
	        System.out.println("Mixed roles detected - setting conflict flag and clearing sessions");
	        SecurityUtil.setRoleConflict(session, true); // SET THE FLAG
	        SecurityUtil.clearAllSessions(session);
	        response.sendRedirect("/index?error=mixed_roles");
	        return false;
	    }
	    
	    // Check user endpoints
	    if (requestURI.startsWith("/user/") && !requestURI.equals("/user/login") && 
	        !requestURI.equals("/user/register") && !requestURI.equals("/user/logout")) {
	        
	        if (!SecurityUtil.hasUserAccess(session)) {
	            System.out.println("Blocked user access - wrong role or not logged in");
	            SecurityUtil.clearAllSessions(session);
	            response.sendRedirect("/user/login");
	            return false;
	        }
	    }
	    
	    // Check owner endpoints
	    if (requestURI.startsWith("/owner/") && !requestURI.equals("/owner/login") && 
	        !requestURI.equals("/owner/register") && !requestURI.equals("/owner/logout")) {
	        
	        if (!SecurityUtil.hasOwnerAccess(session)) {
	            System.out.println("Blocked owner access - wrong role or not logged in");
	            SecurityUtil.clearAllSessions(session);
	            response.sendRedirect("/owner/login");
	            return false;
	        }
	    }
	    
	    // Check admin endpoints
	    if (requestURI.startsWith("/admin/") && !requestURI.equals("/admin") && 
	        !requestURI.equals("/admin/logout")) {
	        
	        if (!SecurityUtil.hasAdminAccess(session)) {
	            System.out.println("Blocked admin access - wrong role or not logged in");
	            SecurityUtil.clearAllSessions(session);
	            response.sendRedirect("/admin");
	            return false;
	        }
	    }
	    
	    return true;
	}
}
package com.springboot.Job.interceptor;

import com.springboot.Job.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession();
        String requestURI = request.getRequestURI();

        System.out.println("Security Interceptor checking: " + requestURI);

        // ---- Handle Role Conflicts ----
        if (SecurityUtil.hasRoleConflict(session)) {
            System.out.println("Previous role conflict detected - forcing logout");
            SecurityUtil.clearAllSessions(session);
            response.sendRedirect("/index?error=role_conflict");
            return false;
        }

        if (SecurityUtil.hasMixedRoles(session)) {
            System.out.println("Mixed roles detected - setting conflict flag and clearing sessions");
            SecurityUtil.setRoleConflict(session, true);
            SecurityUtil.clearAllSessions(session);
            response.sendRedirect("/index?error=mixed_roles");
            return false;
        }

        // ==============================
        // USER ENDPOINTS
        // ==============================

        List<String> publicUserEndpoints = Arrays.asList(
                "/user/login",
                "/user/register",
                "/user/logout",
                "/user/by-category",
                "/user/guestview_companies",
                "/user/about-us",
                "/user/contact-us",
                "/user/aboutus",
                "/user/contactus"
        );

        // Also allow these dynamic patterns
        boolean isPublicUserEndpoint =
                publicUserEndpoints.stream().anyMatch(requestURI::equals) ||
                requestURI.startsWith("/user/company") ||
                requestURI.startsWith("/user/job/detail");

        if (requestURI.startsWith("/user/") && !isPublicUserEndpoint) {
            if (!SecurityUtil.hasUserAccess(session)) {
                System.out.println("Blocked user access - wrong role or not logged in");
                SecurityUtil.clearAllSessions(session);
                response.sendRedirect("/user/login");
                return false;
            }
        }

        // ==============================
        // OWNER ENDPOINTS
        // ==============================
        
        // Public owner endpoints (allowed without authentication)
        List<String> publicOwnerEndpoints = Arrays.asList(
                "/owner/login",
                "/owner/logout",
                "/owner/about-us",
                "/owner/contact-us",
                "/owner/aboutus",
                "/owner/contactus",
                // Add forgot password routes
                "/owner/ownerforgotpassword",
                "/owner/forgot-password",
                "/owner/verify-otp",
                "/owner/resend-otp",
                "/owner/reset-password"
                // "/owner/register" is NOT included here, so it will be blocked
        );

        // Check if this is a public owner endpoint
        boolean isPublicOwnerEndpoint = publicOwnerEndpoints.stream().anyMatch(requestURI::equals);

        // Block direct access to /owner/register
        if (requestURI.equals("/owner/register")) {
            System.out.println("Direct access to /owner/register blocked");
            response.sendRedirect("/request/owner?error=use_proper_registration_flow");
            return false;
        }

        // For all other owner endpoints that are NOT public
        if (requestURI.startsWith("/owner/") && !isPublicOwnerEndpoint) {
            if (!SecurityUtil.hasOwnerAccess(session)) {
                System.out.println("Blocked owner access - wrong role or not logged in for: " + requestURI);
                SecurityUtil.clearAllSessions(session);
                response.sendRedirect("/owner/login");
                return false;
            }
        }

        // ==============================
        // ADMIN ENDPOINTS
        // ==============================
        if (requestURI.startsWith("/admin/") &&
            !requestURI.equals("/admin") &&
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
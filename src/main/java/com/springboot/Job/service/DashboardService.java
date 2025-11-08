package com.springboot.Job.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.springboot.Job.repository.JobPVByAdmRepository;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private JobPVByAdmRepository jobPVByAdmRepository;

    // =================== DASHBOARD STATS METHODS ====================
    public Map<String, Object> getDashboardStats(String month, String year) {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        // Handle "ALL" selection
        boolean showAllData = "ALL".equals(year);
        boolean showAllMonths = "ALL".equals(month);
        
        if (showAllData) {
            // Get all-time statistics (entire database)
            stats.put("jobSeekerCount", jobPVByAdmRepository.countAllJobSeekers());
            stats.put("employerCount", jobPVByAdmRepository.countAllEmployers());
            stats.put("activeJobCount", jobPVByAdmRepository.countAllActiveJobs());
            stats.put("applicationCount", jobPVByAdmRepository.countAllApplications());
            
            // No previous month comparison for "ALL" data
            stats.put("previousMonthJobSeekerCount", null);
            stats.put("previousMonthEmployerCount", null);
            stats.put("previousMonthJobCount", null);
            stats.put("previousMonthApplicationCount", null);
            
        } else {
            // Handle year and month filtering
            int targetYear = getTargetYear(year);
            
            if (showAllMonths) {
                // Get data for entire year
                stats.put("jobSeekerCount", jobPVByAdmRepository.countJobSeekersForYear(targetYear));
                stats.put("employerCount", jobPVByAdmRepository.countEmployersForYear(targetYear));
                stats.put("activeJobCount", jobPVByAdmRepository.countActiveJobsForYear(targetYear));
                stats.put("applicationCount", jobPVByAdmRepository.countApplicationsForYear(targetYear));
                
                // Compare with previous year
                stats.put("previousMonthJobSeekerCount", jobPVByAdmRepository.countJobSeekersForYear(targetYear - 1));
                stats.put("previousMonthEmployerCount", jobPVByAdmRepository.countEmployersForYear(targetYear - 1));
                stats.put("previousMonthJobCount", jobPVByAdmRepository.countActiveJobsForYear(targetYear - 1));
                stats.put("previousMonthApplicationCount", jobPVByAdmRepository.countApplicationsForYear(targetYear - 1));
                
            } else {
                // Original month-specific logic
                YearMonth targetYearMonth = getTargetYearMonth(month, targetYear);
                YearMonth previousYearMonth = targetYearMonth.minusMonths(1);
                
                stats.put("jobSeekerCount", jobPVByAdmRepository.countJobSeekersForMonth(targetYearMonth));
                stats.put("employerCount", jobPVByAdmRepository.countEmployersForMonth(targetYearMonth));
                stats.put("activeJobCount", jobPVByAdmRepository.countActiveJobsForMonth(targetYearMonth));
                stats.put("applicationCount", jobPVByAdmRepository.countApplicationsForMonth(targetYearMonth));
                
                stats.put("previousMonthJobSeekerCount", jobPVByAdmRepository.countJobSeekersForMonth(previousYearMonth));
                stats.put("previousMonthEmployerCount", jobPVByAdmRepository.countEmployersForMonth(previousYearMonth));
                stats.put("previousMonthJobCount", jobPVByAdmRepository.countActiveJobsForMonth(previousYearMonth));
                stats.put("previousMonthApplicationCount", jobPVByAdmRepository.countApplicationsForMonth(previousYearMonth));
            }
        }
        
        // Add selection information
        stats.put("selectedYear", year);
        stats.put("selectedMonth", month);
        stats.put("availableMonths", getAvailableMonths());
        stats.put("availableYears", getAvailableYears());
        
        return stats;
    }

    private int getTargetYear(String year) {
        if (year == null || year.isEmpty() || "current".equals(year) || "ALL".equals(year)) {
            return LocalDate.now().getYear();
        } else {
            try {
                return Integer.parseInt(year);
            } catch (Exception e) {
                return LocalDate.now().getYear();
            }
        }
    }

    private YearMonth getTargetYearMonth(String month, int year) {
        if (month == null || month.isEmpty() || "current".equals(month) || "ALL".equals(month)) {
            return YearMonth.now();
        } else {
            try {
                // Handle month as string name (JANUARY, FEBRUARY, etc.)
                Month monthEnum = Month.valueOf(month.toUpperCase());
                return YearMonth.of(year, monthEnum);
            } catch (IllegalArgumentException e) {
                // If month is numeric (01, 02, etc.)
                try {
                    int monthNumber = Integer.parseInt(month);
                    return YearMonth.of(year, monthNumber);
                } catch (NumberFormatException ex) {
                    return YearMonth.now();
                }
            }
        }
    }

    private Map<String, String> getAvailableMonths() {
        Map<String, String> availableMonths = new LinkedHashMap<>();
        
        // Add "ALL" option first
        availableMonths.put("ALL", "All Months");
        
        // Add individual months
        for (Month month : Month.values()) {
            String monthKey = month.name();
            String monthDisplayName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            availableMonths.put(monthKey, monthDisplayName);
        }
        
        return availableMonths;
    }

    private List<String> getAvailableYears() {
        int currentYear = java.time.Year.now().getValue();
        List<String> years = new ArrayList<>();
        
        // Add "ALL" option first
        years.add("ALL");
        
        // Always show next year as the first option after ALL
        int startYear = currentYear + 1;
        
        for (int i = startYear; i >= startYear - 5; i--) {
            years.add(String.valueOf(i));
        }
        return years;
    }

    // =================== PAGINATION UTILITY METHODS ====================
    
    /**
     * Generates pagination numbers with ellipsis for smart pagination display
     * 
     * @param currentPage the current active page
     * @param totalPages the total number of pages
     * @param maxVisiblePages maximum number of page buttons to show (excluding ellipsis)
     * @return list of page numbers and ellipsis as objects
     */
    public List<Object> generatePagination(int currentPage, int totalPages, int maxVisiblePages) {
        List<Object> pages = new ArrayList<>();
        
        if (totalPages <= 1) {
            return pages;
        }
        
        int half = maxVisiblePages / 2;
        int start = Math.max(1, currentPage - half);
        int end = Math.min(totalPages, start + maxVisiblePages - 1);
        
        // Adjust start if we're near the end
        start = Math.max(1, end - maxVisiblePages + 1);
        
        // Add first page and ellipsis if needed
        if (start > 1) {
            pages.add(1);
            if (start > 2) {
                pages.add("...");
            }
        }
        
        // Add page numbers
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        
        // Add ellipsis and last page if needed
        if (end < totalPages) {
            if (end < totalPages - 1) {
                pages.add("...");
            }
            pages.add(totalPages);
        }
        
        return pages;
    }
    
    /**
     * Generates pagination with default 5 visible pages
     */
    public List<Object> generatePagination(int currentPage, int totalPages) {
        return generatePagination(currentPage, totalPages, 5);
    }
    
    /**
     * Calculates the starting index for pagination queries
     */
    public int calculateOffset(int page, int size) {
        return (page - 1) * size;
    }
    
    /**
     * Calculates total pages based on total items and page size
     */
    public int calculateTotalPages(long totalItems, int pageSize) {
        return (int) Math.ceil((double) totalItems / pageSize);
    }
    
    /**
     * Validates page parameters to ensure they are within valid ranges
     */
    public int validatePage(int page, int totalPages) {
        if (page < 1) return 1;
        if (totalPages > 0 && page > totalPages) return totalPages;
        return page;
    }
}
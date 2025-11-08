package com.springboot.Job.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.springboot.Job.model.OwnerRequest;
import com.springboot.Job.model.Location;
import com.springboot.Job.model.Township;
import com.springboot.Job.repository.OwnerRequestRepository;
import com.springboot.Job.repository.LocationRepository;
import com.springboot.Job.repository.TownshipRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Controller
@RequestMapping("/request")
public class OwnerRequestController {

    private final OwnerRequestRepository ownerRequestRepository;
    private final LocationRepository locationRepository;
    private final TownshipRepository townshipRepository;
    private final ObjectMapper objectMapper;

    public OwnerRequestController(OwnerRequestRepository ownerRequestRepository,
                                 LocationRepository locationRepository,
                                 TownshipRepository townshipRepository,
                                 ObjectMapper objectMapper) {
        this.ownerRequestRepository = ownerRequestRepository;
        this.locationRepository = locationRepository;
        this.townshipRepository = townshipRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/owner")
    public String showRequestForm(Model model) {
        model.addAttribute("ownerRequest", new OwnerRequest());
        model.addAttribute("locations", locationRepository.findAll());

        Map<Integer, List<String>> townshipsByCity = new HashMap<>();
        try {
            List<Township> allTownships = townshipRepository.findAllWithCityNames();
            for (Township t : allTownships) {
                townshipsByCity
                    .computeIfAbsent(t.getCityId(), k -> new ArrayList<>())
                    .add(t.getTownshipName());
            }
            model.addAttribute("townshipsJson", objectMapper.writeValueAsString(townshipsByCity));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("townshipsJson", "{}");
        }

        return "owner/request-form";
    }

    @PostMapping("/owner")
    public String submitRequest(
            @RequestParam String companyName,
            @RequestParam String gmail,
            @RequestParam String companyPhone,
            @RequestParam String city,
            @RequestParam(required = false) String township,
            @RequestParam String description,
            Model model) {

        try {
            if (ownerRequestRepository.findByEmail(gmail).isPresent()) {
                model.addAttribute("error", "Email already used.");
                return showRequestForm(model);
            }

            if (ownerRequestRepository.findByPhone(companyPhone).isPresent()) {
                model.addAttribute("error", "Phone number already used.");
                return showRequestForm(model);
            }

            Location location = locationRepository.findById(Integer.parseInt(city)).orElse(null);
            String cityName = location != null ? location.getCityName() : city;

            OwnerRequest req = new OwnerRequest();
            req.setCompanyName(companyName);
            req.setGmail(gmail);
            req.setCompanyPhone(companyPhone);
            req.setCity(cityName);
            req.setTownship(township != null ? township : "");
            req.setDescription(description);
            req.setAuthKey(UUID.randomUUID().toString());

            boolean success = ownerRequestRepository.createRequest(req);
            model.addAttribute(success ? "success" : "error",
                success ? "Request submitted successfully!" : "Failed to submit request.");

        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }

        return showRequestForm(model);
    }
}
